#!/usr/bin/env python3
"""Minimal Minecraft 26.3 (protocol 777) bot for testing ForgeCore.

Implements just enough of the protocol to join an offline-mode Paper 26.3
server, stay connected (keepalive), send chat commands, and capture chat.

Packet IDs verified against:
  - void-community/void PacketIdDefinitions.cs @ cb9d74c (generated from the
    official 26.2/26.3 server jar data-generator reports): protocol 777,
    clientbound play keep-alive 0x2D, system-chat 0x7C, serverbound
    chat-command 0x07 / play keep-alive 0x1C.
  - javap on paper-26.3.jar (Mojang mapped):
    ServerboundChatCommandPacket = record(String command) -- chat signing is
      gone in 26.3, the packet is a single String.
    ServerboundHelloPacket = record(String name, UUID profileId).
    ClientboundLoginFinishedPacket = record(GameProfile, UUID sessionId).
    ServerboundLoginAcknowledgedPacket exists (0x03) -- the login->config
      transition now needs an explicit ack.
    ClientboundSystemChatPacket = (Component via TRUSTED_STREAM_CODEC, boolean).

Usage:
    python3 mcbot.py <name> <host> <port> <script.json> <chatlog>
"""

import socket
import struct
import zlib
import json
import threading
import time
import sys
import uuid as uuid_mod

PROTOCOL = 777

# --- packet ids (26.3 / protocol 777) ---
SB_HANDSHAKE = 0x00
SB_LOGIN_START = 0x00
SB_LOGIN_ACK = 0x03
SB_CONFIG_ACK_FINISH = 0x03
SB_CONFIG_KEEPALIVE = 0x04
SB_CONFIG_SELECT_KNOWN_PACKS = 0x07  # packets.json: configuration serverbound minecraft:select_known_packs
SB_PLAY_KEEPALIVE = 0x1C
SB_CHAT_COMMAND = 0x07

CB_LOGIN_DISCONNECT = 0x00
CB_LOGIN_FINISHED = 0x02
CB_SET_COMPRESSION = 0x03
CB_CONFIG_DISCONNECT = 0x02
CB_CONFIG_FINISH = 0x03
CB_CONFIG_KEEPALIVE = 0x04
CB_PLAY_KEEPALIVE = 0x2D
CB_PLAY_DISCONNECT = 0x20
CB_SYSTEM_CHAT = 0x7C


class Buf:
    def __init__(self, data=b""):
        self.d = bytearray(data)
        self.p = 0

    def read(self, n):
        if self.p + n > len(self.d):
            raise EOFError("short read")
        b = bytes(self.d[self.p:self.p + n])
        self.p += n
        return b

    def read_varint(self):
        v = 0
        for i in range(5):
            b = self.read(1)[0]
            v |= (b & 0x7F) << (7 * i)
            if not (b & 0x80):
                if v & (1 << 31):
                    v -= 1 << 32
                return v
        raise ValueError("varint too long")

    def read_string(self, limit=32767):
        n = self.read_varint()
        if n > limit * 4:
            raise ValueError("string too long")
        return self.read(n).decode("utf-8", "replace")

    def read_long(self):
        return struct.unpack(">q", self.read(8))[0]

    def read_bool(self):
        return self.read(1)[0] != 0

    def read_uuid(self):
        return uuid_mod.UUID(bytes=self.read(16))

    def remaining(self):
        return len(self.d) - self.p


def w_varint(v):
    v &= 0xFFFFFFFF
    out = bytearray()
    while True:
        b = v & 0x7F
        v >>= 7
        if v:
            out.append(b | 0x80)
        else:
            out.append(b)
            return bytes(out)


def w_string(s):
    b = s.encode("utf-8")
    return w_varint(len(b)) + b



class NBTReader(Buf):
    """Minimal NBT reader for chat components (standard binary NBT)."""
    def read_nbt_string(self):
        n = struct.unpack(">H", self.read(2))[0]
        return self.read(n).decode("utf-8", "replace")

    def read_nbt_payload(self, tag):
        if tag == 1:
            return self.read(1)[0]
        if tag == 2:
            return struct.unpack(">h", self.read(2))[0]
        if tag == 3:
            return struct.unpack(">i", self.read(4))[0]
        if tag == 4:
            return struct.unpack(">q", self.read(8))[0]
        if tag == 5:
            return struct.unpack(">f", self.read(4))[0]
        if tag == 6:
            return struct.unpack(">d", self.read(8))[0]
        if tag == 7:
            n = struct.unpack(">i", self.read(4))[0]
            return self.read(n)
        if tag == 8:
            return self.read_nbt_string()
        if tag == 9:
            etype = self.read(1)[0]
            n = struct.unpack(">i", self.read(4))[0]
            return [self.read_nbt_payload(etype) for _ in range(n)]
        if tag == 10:
            return self.read_nbt_compound()
        if tag == 11:
            n = struct.unpack(">i", self.read(4))[0]
            return [struct.unpack(">i", self.read(4))[0] for _ in range(n)]
        if tag == 12:
            n = struct.unpack(">i", self.read(4))[0]
            return [struct.unpack(">q", self.read(8))[0] for _ in range(n)]
        raise ValueError("bad nbt tag %d" % tag)

    def read_nbt_compound(self):
        d = {}
        while True:
            t = self.read(1)[0]
            if t == 0:
                return d
            # NB: split into two statements -- Python evaluates the RHS
            # (payload) before the subscript key (name) in d[k] = v!
            name = self.read_nbt_string()
            d[name] = self.read_nbt_payload(t)

    def read_nbt_root(self):
        # 26.3 writes components as BARE tags (type byte + payload, no name).
        # Empty/simple components are StringTags; styled ones are CompoundTags.
        tag = self.read(1)[0]
        return self.read_nbt_payload(tag)


def component_text(comp):
    """Plain text from an NBT-decoded chat component (dict/list/str)."""
    if comp is None:
        return ""
    if isinstance(comp, str):
        return comp
    if isinstance(comp, list):
        return "".join(component_text(c) for c in comp)
    if isinstance(comp, dict):
        t = comp.get("text", "")
        if not isinstance(t, str):
            t = component_text(t)
        extra = comp.get("extra", [])
        if isinstance(extra, list):
            t += "".join(component_text(c) for c in extra)
        if not t and "translate" in comp:
            t = str(comp["translate"])
            args = comp.get("with", [])
            if isinstance(args, list) and args:
                t += "(" + ",".join(component_text(c) for c in args) + ")"
        return t
    return str(comp)

def plain_text(comp):
    """Best-effort plain text extraction from a chat component JSON value."""
    if comp is None:
        return ""
    if isinstance(comp, str):
        return comp
    if isinstance(comp, list):
        return "".join(plain_text(c) for c in comp)
    if isinstance(comp, dict):
        t = comp.get("text", "")
        if "extra" in comp:
            t += "".join(plain_text(c) for c in comp["extra"])
        if not t and "translate" in comp:
            t = comp["translate"]
            if "with" in comp:
                t += "(" + ",".join(plain_text(c) for c in comp["with"]) + ")"
        return t
    return str(comp)


class Bot:
    def __init__(self, name, host, port, chatlog):
        self.name = name
        self.host = host
        self.port = port
        self.uuid = uuid_mod.uuid3(uuid_mod.NAMESPACE_DNS, "OfflinePlayer:" + name)
        self.sock = None
        self.compression = -1
        self.state = "handshake"
        self.alive = True
        self.chat = []          # (timestamp, text, overlay)
        self.packets = []       # (timestamp, state, packet_id, length)
        self.kick_reason = None
        self.play_started = threading.Event()
        self.logf = open(chatlog, "w", encoding="utf-8")
        self.send_lock = threading.Lock()

    def log(self, msg):
        line = "[%s %s] %s" % (time.strftime("%H:%M:%S"), self.name, msg)
        print(line, flush=True)
        self.logf.write(line + "\n")
        self.logf.flush()

    # ---- low level ----
    def _send_raw(self, data):
        if self.compression >= 0:
            if len(data) >= self.compression:
                payload = w_varint(len(data)) + zlib.compress(data)
            else:
                payload = w_varint(0) + data
            frame = w_varint(len(payload)) + payload
        else:
            frame = w_varint(len(data)) + data
        self.sock.sendall(frame)

    def send(self, packet_id, payload=b""):
        with self.send_lock:
            self._send_raw(w_varint(packet_id) + payload)

    def _recv_exact(self, n):
        buf = b""
        while len(buf) < n:
            chunk = self.sock.recv(n - len(buf))
            if not chunk:
                raise ConnectionError("connection closed")
            buf += chunk
        return buf

    def _read_frame(self):
        # read varint length (1-3 bytes per spec)
        length = 0
        for i in range(3):
            b = self._recv_exact(1)[0]
            length |= (b & 0x7F) << (7 * i)
            if not (b & 0x80):
                break
        raw = self._recv_exact(length)
        if self.compression >= 0:
            b = Buf(raw)
            dlen = b.read_varint()
            rest = raw[b.p:]
            if dlen == 0:
                return rest
            return zlib.decompress(rest)
        return raw

    # ---- protocol ----
    def ping(self):
        """Status ping; returns (protocol, version_name, motd_text, online)."""
        s = socket.create_connection((self.host, self.port), timeout=10)
        try:
            s.sendall(w_varint(1 + 2 + len(self.host) + 2 + 1 + 1) +
                      w_varint(SB_HANDSHAKE) + w_varint(PROTOCOL) +
                      w_string(self.host) + struct.pack(">H", self.port) +
                      w_varint(1))
            s.sendall(w_varint(1) + w_varint(0x00))  # status request
            # read response
            length = 0
            for i in range(3):
                b = s.recv(1)[0]
                length |= (b & 0x7F) << (7 * i)
                if not (b & 0x80):
                    break
            raw = b""
            while len(raw) < length:
                raw += s.recv(length - len(raw))
            b = Buf(raw)
            pid = b.read_varint()
            assert pid == 0x00, "unexpected status packet %02x" % pid
            info = json.loads(b.read_string(1 << 20))
            desc = info.get("description", "")
            motd = plain_text(desc)
            return (info["version"]["protocol"], info["version"]["name"], motd,
                    info["players"]["online"], info["players"]["max"])
        finally:
            s.close()

    def _read_packet(self):
        frame = self._read_frame()
        b = Buf(frame)
        pid = b.read_varint()
        self.packets.append((time.time(), self.state, pid, len(frame)))
        return pid, b

    def _expect(self, pid, b, what):
        return b

    def connect(self):
        self.sock = socket.create_connection((self.host, self.port), timeout=30)
        self.sock.settimeout(30)
        # handshake: login
        self.send(SB_HANDSHAKE, w_varint(PROTOCOL) + w_string(self.host) +
                  struct.pack(">H", self.port) + w_varint(2))
        self.state = "login"
        # login start
        self.send(SB_LOGIN_START, w_string(self.name) + self.uuid.bytes)
        self.log("login start sent as %s (%s)" % (self.name, self.uuid))

        self.config_deadline = None
        self.dumped = set()
        while self.alive:
            if self.state == "config" and self.config_deadline and time.time() > self.config_deadline:
                seen = {}
                for _, st, pid2, ln in self.packets:
                    if st == "config":
                        seen[pid2] = seen.get(pid2, 0) + 1
                self.log("CONFIG STALL census: " + ", ".join("%02x x%d" % (p, n) for p, n in sorted(seen.items())))
                self.alive = False
                return False
            pid, b = self._read_packet()
            if self.state == "login":
                if pid == CB_LOGIN_DISCONNECT:
                    self.kick_reason = b.read_string(1 << 20)
                    self.log("KICKED during login: " + plain_text(json.loads(self.kick_reason)))
                    self.alive = False
                    return False
                if pid == CB_SET_COMPRESSION:
                    self.compression = b.read_varint()
                    self.log("compression threshold: %d" % self.compression)
                    continue
                if pid == CB_LOGIN_FINISHED:
                    profile_id = b.read_uuid()
                    profile_name = b.read_string()
                    nprops = b.read_varint()
                    for _ in range(nprops):
                        b.read_string(); b.read_string()
                        if b.read_bool():
                            b.read_string()
                    session = b.read_uuid()
                    self.log("login finished: %s %s session=%s" % (profile_name, profile_id, session))
                    self.send(SB_LOGIN_ACK)
                    self.state = "config"
                    self.config_deadline = time.time() + 15
                    self.log("login acknowledged -> config state")
                    continue
                self.log("login: unhandled packet %02x (%d bytes)" % (pid, len(b.d)))
            elif self.state == "config":
                if pid == CB_CONFIG_DISCONNECT:
                    self.kick_reason = b.read_string(1 << 20)
                    self.log("KICKED during config: " + plain_text(json.loads(self.kick_reason)))
                    self.alive = False
                    return False
                if pid == CB_CONFIG_KEEPALIVE:
                    kid = b.read_long()
                    self.send(SB_CONFIG_KEEPALIVE, struct.pack(">q", kid))
                    continue
                if pid == 0x0F:  # ClientboundSelectKnownPacks
                    n = b.read_varint()
                    packs = [(b.read_string(), b.read_string(), b.read_string()) for _ in range(n)]
                    self.log("select known packs: %s" % (packs,))
                    payload = w_varint(len(packs))
                    for ns, pid_, ver in packs:
                        payload += w_string(ns) + w_string(pid_) + w_string(ver)
                    self.send(SB_CONFIG_SELECT_KNOWN_PACKS, payload)
                    self.log("sent ServerboundSelectKnownPacks")
                    continue
                if pid == CB_CONFIG_FINISH:
                    self.send(SB_CONFIG_ACK_FINISH)
                    self.state = "play"
                    self.log("config finished -> play state")
                    self.play_started.set()
                    break
                if pid not in self.dumped:
                    self.dumped.add(pid)
                    self.log("config %02x first %d bytes: %s" % (pid, min(96, len(b.d)), b.d[:96].hex()))
                continue
        # play read loop in background
        threading.Thread(target=self._play_loop, daemon=True).start()
        return True

    def _play_loop(self):
        import traceback
        try:
            while self.alive:
                pid, b = self._read_packet()
                self._last_pid = pid
                if pid == CB_PLAY_DISCONNECT:
                    self.kick_reason = b.read_string(1 << 20)
                    self.log("KICKED: " + plain_text(json.loads(self.kick_reason)))
                    self.alive = False
                    return
                if pid == CB_PLAY_KEEPALIVE:
                    kid = b.read_long()
                    self.send(SB_PLAY_KEEPALIVE, struct.pack(">q", kid))
                    continue
                if pid == CB_SYSTEM_CHAT:
                    startp = b.p
                    # Robust NBT parse: on any failure, log hex and skip packet.
                    # (Some messages, e.g. Component.empty(), have edge-case encodings.)
                    try:
                        nb = NBTReader(b.d[startp:])
                        comp = nb.read_nbt_root()
                        consumed = len(nb.d) - nb.remaining()
                        if startp + consumed + 1 > len(b.d):
                            raise ValueError("nbt over-read: consumed %d of %d" % (consumed, len(b.d) - startp))
                        b.p = startp + consumed
                        text = component_text(comp)
                    except Exception as e:
                        text = "<nbt-fail %s>" % (e,)
                        if not hasattr(self, 'nbtfail_logged'):
                            self.nbtfail_logged = True
                            self.log("nbt fail hex: " + b.d[startp:].hex()[:400])
                        b.p = len(b.d)
                        overlay = False
                        self.chat.append((time.time(), text, overlay))
                        continue
                    overlay = b.read_bool()
                    self.chat.append((time.time(), text, overlay))
                    if not overlay:
                        self.log("chat: " + text)
                    continue
                # skip the rest (login, declare commands, chunks, ...)
        except Exception as e:
            if self.alive:
                self.log("play loop ended on pid %02x: %r" % (getattr(self, '_last_pid', -1), e))
                self.log(traceback.format_exc()[-800:])
            self.alive = False

    # ---- actions ----
    def command(self, cmd, wait=1.0):
        """Send a chat command (without leading slash) and wait."""
        if not cmd.startswith("/"):
            cmd = "/" + cmd
        self.send(SB_CHAT_COMMAND, w_string(cmd[1:]))
        self.log(">>> " + cmd)
        time.sleep(wait)

    def say(self, msg, wait=0.5):
        self.log(">>> " + msg)
        time.sleep(wait)

    def wait_chat(self, needle, timeout=8.0):
        """Wait until a chat message containing needle arrives."""
        end = time.time() + timeout
        while time.time() < end:
            for _, text, overlay in self.chat:
                if not overlay and needle in text:
                    return text
            time.sleep(0.2)
        return None

    def close(self):
        self.alive = False
        try:
            self.sock.close()
        except Exception:
            pass
        self.logf.close()


def run_script(name, host, port, script_path, chatlog):
    bot = Bot(name, host, port, chatlog)
    try:
        proto, ver, motd, online, maxp = bot.ping()
        bot.log("ping: protocol=%d version=%s motd=%r players=%d/%d" % (proto, ver, motd, online, maxp))
    except Exception as e:
        bot.log("ping failed: %r" % e)
        return bot
    if not bot.connect():
        bot.close()
        return bot
    if not bot.play_started.wait(30):
        bot.log("never reached play state!")
        bot.close()
        return bot
    time.sleep(2.0)  # let join settle
    try:
        script = json.load(open(script_path, encoding="utf-8"))
    except Exception as e:
        bot.log("no script: %r" % e)
        script = []
    for step in script:
        if not bot.alive:
            break
        action = step.get("do", "cmd")
        if action == "cmd":
            bot.command(step["cmd"], step.get("wait", 1.2))
        elif action == "sleep":
            time.sleep(step.get("s", 2.0))
        elif action == "expect":
            got = bot.wait_chat(step["text"], step.get("timeout", 8.0))
            bot.log("expect %r -> %s" % (step["text"], "OK" if got else "TIMEOUT"))
    time.sleep(2.0)
    # packet census
    seen = {}
    for _, st, pid, _ in bot.packets:
        seen[(st, pid)] = seen.get((st, pid), 0) + 1
    bot.log("packet census: " + ", ".join("%s:%02x x%d" % (st, pid, n) for (st, pid), n in sorted(seen.items())))
    bot.close()
    return bot


if __name__ == "__main__":
    if len(sys.argv) != 6:
        print("usage: mcbot.py <name> <host> <port> <script.json> <chatlog>")
        sys.exit(2)
    run_script(sys.argv[1], sys.argv[2], int(sys.argv[3]), sys.argv[4], sys.argv[5])
