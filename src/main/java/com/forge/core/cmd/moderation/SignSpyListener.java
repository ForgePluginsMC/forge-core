package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.jspecify.annotations.NullMarked;

/** Monitors sign creation; staff with signspy enabled see who/where/what. */
@NullMarked
public final class SignSpyListener implements Listener {
    private final ForgeCore plugin;
    private final Set<UUID> spies = ConcurrentHashMap.newKeySet();

    public SignSpyListener(ForgeCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public boolean toggle(UUID uuid) {
        if (spies.remove(uuid)) {
            return false;
        }
        spies.add(uuid);
        return true;
    }

    public boolean isSpying(UUID uuid) {
        return spies.contains(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (spies.isEmpty()) {
            return;
        }
        Block block = event.getBlock();
        StringBuilder lines = new StringBuilder();
        var plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText();
        for (Component line : event.lines()) {
            String text = plain.serialize(line);
            if (!text.isBlank()) {
                if (lines.length() > 0) {
                    lines.append(" / ");
                }
                lines.append(text);
            }
        }
        String loc = block.getWorld().getName() + " " + block.getX() + "," + block.getY() + "," + block.getZ();
        String msg = "<dark_gray>[SignSpy]</dark_gray> <white>" + Text.escape(event.getPlayer().getName())
                + "</white> at <gray>" + Text.escape(loc) + "</gray>: <yellow>"
                + Text.escape(lines.toString()) + "</yellow>";
        for (UUID uuid : spies) {
            org.bukkit.entity.Player spy = plugin.getServer().getPlayer(uuid);
            if (spy != null && spy.hasPermission("forgecore.signspy")) {
                Text.send(spy, msg);
            }
        }
        // Log to console as well
        if (block.getState() instanceof Sign) {
            plugin.getLogger().info("[SignSpy] " + event.getPlayer().getName() + " at " + loc
                    + ": " + lines);
        }
    }
}
