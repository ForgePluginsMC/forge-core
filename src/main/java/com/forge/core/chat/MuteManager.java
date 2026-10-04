package com.forge.core.chat;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Mutes, global chat mute, silence, social spy and command spy.
 * Mutes persist in {@code mutes.yml}; spy lists are session-only.
 */
public final class MuteManager implements Listener {
    private final ForgeCore plugin;
    private final File file;
    /** UUID -> expiry epoch millis; 0 means forever. */
    private final Map<UUID, Long> muted = new ConcurrentHashMap<>();
    private final Map<UUID, String> reasons = new ConcurrentHashMap<>();
    private boolean chatMuted;
    private final Set<UUID> silenced = ConcurrentHashMap.newKeySet();
    private final Set<UUID> socialSpy = ConcurrentHashMap.newKeySet();
    private final Set<UUID> commandSpy = ConcurrentHashMap.newKeySet();

    public MuteManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "mutes.yml");
        load();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> entry : config.getMapList("mutes")) {
            try {
                UUID uuid = UUID.fromString(String.valueOf(entry.get("uuid")));
                Object untilRaw = entry.get("until");
                long until = untilRaw instanceof Number number ? number.longValue() : 0L;
                Object reasonRaw = entry.get("reason");
                if (until == 0 || until > System.currentTimeMillis()) {
                    muted.put(uuid, until);
                    reasons.put(uuid, reasonRaw == null ? "Muted" : String.valueOf(reasonRaw));
                }
            } catch (RuntimeException exception) {
                // Skip bad rows.
            }
        }
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        var rows = new java.util.ArrayList<Map<String, Object>>();
        for (Map.Entry<UUID, Long> entry : muted.entrySet()) {
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("uuid", entry.getKey().toString());
            row.put("until", entry.getValue());
            row.put("reason", reasons.getOrDefault(entry.getKey(), "Muted"));
            rows.add(row);
        }
        config.set("mutes", rows);
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save mutes.yml: " + exception.getMessage());
        }
    }

    /** Mute a player; {@code untilMillis} 0 = forever. */
    public void mute(Player player, long untilMillis, String reason) {
        muted.put(player.getUniqueId(), untilMillis);
        reasons.put(player.getUniqueId(), reason);
        save();
    }

    public void unmute(Player player) {
        muted.remove(player.getUniqueId());
        reasons.remove(player.getUniqueId());
        save();
    }

    /** True while the mute is in force (expired mutes are cleaned up). */
    public boolean isMuted(Player player) {
        Long until = muted.get(player.getUniqueId());
        if (until == null) {
            return false;
        }
        if (until != 0 && until < System.currentTimeMillis()) {
            muted.remove(player.getUniqueId());
            reasons.remove(player.getUniqueId());
            save();
            return false;
        }
        return true;
    }

    public @Nullable String muteReason(Player player) {
        return reasons.get(player.getUniqueId());
    }

    public boolean chatMuted() {
        return chatMuted;
    }

    public void setChatMuted(boolean muted) {
        this.chatMuted = muted;
    }

    public boolean isSilenced(Player player) {
        return silenced.contains(player.getUniqueId());
    }

    public void setSilenced(Player player, boolean silent) {
        if (silent) {
            silenced.add(player.getUniqueId());
        } else {
            silenced.remove(player.getUniqueId());
        }
    }

    public boolean socialSpy(Player player) {
        return socialSpy.contains(player.getUniqueId());
    }

    public void setSocialSpy(Player player, boolean enabled) {
        if (enabled) {
            socialSpy.add(player.getUniqueId());
        } else {
            socialSpy.remove(player.getUniqueId());
        }
    }

    public boolean commandSpy(Player player) {
        return commandSpy.contains(player.getUniqueId());
    }

    public void setCommandSpy(Player player, boolean enabled) {
        if (enabled) {
            commandSpy.add(player.getUniqueId());
        } else {
            commandSpy.remove(player.getUniqueId());
        }
    }

    /** Drop session state (spy lists, silence) when a player leaves. */
    public void forget(Player player) {
        silenced.remove(player.getUniqueId());
        socialSpy.remove(player.getUniqueId());
        commandSpy.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (isMuted(player)) {
            event.setCancelled(true);
            String reason = muteReason(player);
            player.sendMessage(Text.of("<red>You are muted" + (reason == null ? "." : ": " + Text.escape(reason))));
            return;
        }
        if (chatMuted && !player.hasPermission("forgecore.mutechat.bypass")) {
            event.setCancelled(true);
            player.sendMessage(Text.of("<red>Chat is currently muted."));
            return;
        }
        if (isSilenced(player)) {
            event.setCancelled(true);
            player.sendMessage(Text.of("<red>You are silenced."));
            return;
        }
        if (!socialSpy.isEmpty()) {
            String plain = PlainTextComponentSerializer.plainText().serialize(event.message());
            for (UUID spyId : socialSpy) {
                Player spy = plugin.getServer().getPlayer(spyId);
                if (spy != null && !spy.getUniqueId().equals(player.getUniqueId())) {
                    spy.sendMessage(Text.of("<gray>[spy] <white>" + Text.escape(player.getName())
                            + "<gray>: " + Text.escape(plain)));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (commandSpy.isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        for (UUID spyId : commandSpy) {
            Player spy = plugin.getServer().getPlayer(spyId);
            if (spy != null && !spy.getUniqueId().equals(player.getUniqueId())) {
                spy.sendMessage(Text.of("<gray>[cmdspy] <white>" + Text.escape(player.getName())
                        + "<gray>: " + Text.escape(event.getMessage())));
            }
        }
    }
}
