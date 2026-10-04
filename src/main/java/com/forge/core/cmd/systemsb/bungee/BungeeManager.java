package com.forge.core.cmd.systemsb.bungee;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jspecify.annotations.Nullable;

/**
 * BungeeCord plugin messaging: move players between servers, broadcast
 * across the network, and query the server list.
 */
public final class BungeeManager implements PluginMessageListener {
    private static final String CHANNEL = "BungeeCord";
    private static final String BB_SUB = "ForgeCoreBB";

    private final ForgeCore plugin;
    private volatile List<String> serversCache = List.of();
    private final Set<UUID> serverListWaiters = ConcurrentHashMap.newKeySet();
    private @Nullable UUID serverRequester;
    private long serverRequestTime;

    public BungeeManager(ForgeCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, this);
    }

    /** Any online player can carry a plugin message; null when none are. */
    private @Nullable Player courier() {
        return plugin.getServer().getOnlinePlayers().stream().findFirst().orElse(null);
    }

    /** Connect a player to another server on the network. */
    public void connect(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    /** Connect every online player to another server. */
    public void sendAll(String server) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            connect(player, server);
        }
    }

    /**
     * Broadcast a MiniMessage string to every server on the network.
     * Returns false when nobody is online to carry the message.
     */
    public boolean broadcast(String miniMessage) {
        Player courier = courier();
        if (courier == null) {
            return false;
        }
        byte[] bytes = miniMessage.getBytes(StandardCharsets.UTF_8);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(BB_SUB);
        out.writeShort(bytes.length);
        out.write(bytes);
        courier.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
        return true;
    }

    /** Ask the proxy for the server list; waiters are messaged on response. */
    public void requestServerList(@Nullable UUID waiter) {
        Player courier = courier();
        if (courier == null) {
            return;
        }
        if (waiter != null) {
            serverListWaiters.add(waiter);
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServers");
        courier.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    /** Last server list received from the proxy (may be empty). */
    public List<String> cachedServers() {
        return serversCache;
    }

    /** Ask the proxy which server this player is on; replies directly. */
    public void requestOwnServer(Player player) {
        serverRequester = player.getUniqueId();
        serverRequestTime = System.currentTimeMillis();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServer");
        player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!CHANNEL.equals(channel)) {
            return;
        }
        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String sub = in.readUTF();
            switch (sub) {
                case "GetServers" -> {
                    List<String> servers = new ArrayList<>();
                    for (String name : in.readUTF().split(", ")) {
                        if (!name.isBlank()) {
                            servers.add(name);
                        }
                    }
                    serversCache = List.copyOf(servers);
                    for (UUID waiter : new ArrayList<>(serverListWaiters)) {
                        Player target = plugin.getServer().getPlayer(waiter);
                        if (target != null) {
                            Text.send(target, "Servers: <white>" + Text.escape(String.join(", ", serversCache)));
                        }
                    }
                    serverListWaiters.clear();
                }
                case "GetServer" -> {
                    String server = in.readUTF();
                    UUID requester = serverRequester;
                    if (requester != null && System.currentTimeMillis() - serverRequestTime < 10_000) {
                        Player target = plugin.getServer().getPlayer(requester);
                        if (target != null) {
                            Text.send(target, "You are on <white>" + Text.escape(server));
                        }
                    }
                    serverRequester = null;
                }
                case BB_SUB -> {
                    short length = in.readShort();
                    if (length < 0) {
                        return;
                    }
                    byte[] bytes = new byte[length];
                    in.readFully(bytes);
                    String received = new String(bytes, StandardCharsets.UTF_8);
                    plugin.getServer().broadcast(Text.of("<dark_aqua>[Network]<reset> " + received));
                }
                default -> {
                    // Unknown subchannel; ignore.
                }
            }
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("Bad BungeeCord message: " + exception.getMessage());
        }
    }
}
