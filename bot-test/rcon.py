#!/usr/bin/env python3
"""Minimal RCON client for test setup."""
import socket
import struct
import sys


def rcon(host, port, password, command):
    s = socket.create_connection((host, port), timeout=10)
    try:
        # auth
        payload = struct.pack("<iii", 0, 3, 0)[:8] + password.encode() + b"\x00\x00"
        # actually: length + request_id + type + payload + 2 null bytes
        body = struct.pack("<ii", 1, 3) + password.encode() + b"\x00\x00"
        s.sendall(struct.pack("<i", len(body)) + body)
        # read auth response
        resp = _read_packet(s)
        if resp[0] == -1:
            raise Exception("rcon auth failed")
        # command
        body = struct.pack("<ii", 2, 2) + command.encode() + b"\x00\x00"
        s.sendall(struct.pack("<i", len(body)) + body)
        # read response(s)
        out = []
        s.settimeout(3)
        try:
            while True:
                rid, rtype, payload = _read_packet(s)
                if rid == 2:
                    out.append(payload)
                if len(payload) < 4000:
                    break
        except socket.timeout:
            pass
        return "".join(out)
    finally:
        s.close()


def _read_packet(s):
    length = struct.unpack("<i", _recv(s, 4))[0]
    data = _recv(s, length)
    rid, rtype = struct.unpack("<ii", data[:8])
    payload = data[8:-2].decode("utf-8", "replace")
    return rid, rtype, payload


def _recv(s, n):
    buf = b""
    while len(buf) < n:
        chunk = s.recv(n - len(buf))
        if not chunk:
            raise ConnectionError("closed")
        buf += chunk
    return buf


if __name__ == "__main__":
    # usage: rcon.py <command...>; password read from server.properties
    props = {}
    for line in open("/home/chris/test-server/server.properties", encoding="utf-8"):
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, v = line.split("=", 1)
            props[k.strip()] = v.strip()
    cmd = " ".join(sys.argv[1:])
    print(rcon("127.0.0.1", int(props.get("rcon.port", "25575")),
                   props["rcon.password"], cmd))
