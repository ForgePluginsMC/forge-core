package com.forge.core.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Teleport requests (/tpa, /tpahere) with expiry and per-player toggles.
 */
public final class TpaManager {
    /** A pending teleport request. {@code here} means "ask target to come to me". */
    public record Request(UUID from, UUID to, boolean here, long expiresAt) {
    }

    private final ForgeCore plugin;
    private final Map<UUID, Request> byTarget = new ConcurrentHashMap<>();

    public TpaManager(ForgeCore plugin) {
        this.plugin = plugin;
    }

    private long timeoutSeconds() {
        return plugin.getConfig().getLong("tpa-timeout-seconds", 60);
    }

    /** True when the player accepts teleport requests. */
    public boolean accepting(Player player) {
        return !plugin.users().get(player).getBoolean("tptoggle", false);
    }

    public void setAccepting(Player player, boolean accepting) {
        plugin.users().get(player).setBoolean("tptoggle", !accepting);
        plugin.users().save(player.getUniqueId());
    }

    /** Send a request. Returns false when the target ignores requests. */
    public boolean request(Player from, Player to, boolean here) {
        boolean bypass = from.hasPermission("forgecore.tpbypass")
                || plugin.users().get(from).getBoolean("tpbypass", false);
        if (!accepting(to) && !bypass) {
            return false;
        }
        long expiresAt = System.currentTimeMillis() + timeoutSeconds() * 1000;
        byTarget.put(to.getUniqueId(), new Request(from.getUniqueId(), to.getUniqueId(), here, expiresAt));
        return true;
    }

    /** Pending request for the target, or null when none/expired. */
    public @Nullable Request pending(Player target) {
        Request request = byTarget.get(target.getUniqueId());
        if (request == null) {
            return null;
        }
        if (request.expiresAt() < System.currentTimeMillis()) {
            byTarget.remove(target.getUniqueId());
            return null;
        }
        return request;
    }

    public boolean hasPending(Player target) {
        return pending(target) != null;
    }

    /** Accept the pending request; teleports the right party. */
    public boolean accept(Player target) {
        Request request = pending(target);
        if (request == null) {
            return false;
        }
        byTarget.remove(target.getUniqueId());
        Player from = plugin.getServer().getPlayer(request.from());
        if (from == null) {
            Text.error(target, "That player is no longer online.");
            return true;
        }
        if (request.here()) {
            target.teleport(from.getLocation());
            Text.send(from, "<gray>" + Text.escape(target.getName()) + " teleported to you.");
        } else {
            from.teleport(target.getLocation());
            Text.send(from, "<gray>Teleported to " + Text.escape(target.getName()) + ".");
        }
        Text.send(target, "<gray>Request accepted.");
        plugin.afk().setActive(from);
        plugin.afk().setActive(target);
        return true;
    }

    /** Deny the pending request. */
    public boolean deny(Player target) {
        Request request = pending(target);
        if (request == null) {
            return false;
        }
        byTarget.remove(target.getUniqueId());
        Player from = plugin.getServer().getPlayer(request.from());
        if (from != null) {
            Text.send(from, "<gray>" + Text.escape(target.getName()) + " denied your request.");
        }
        Text.send(target, "<gray>Request denied.");
        return true;
    }

    /** Cancel requests involving a player who left. */
    public void forget(Player player) {
        byTarget.remove(player.getUniqueId());
        byTarget.entrySet().removeIf(entry -> entry.getValue().from().equals(player.getUniqueId()));
    }
}
