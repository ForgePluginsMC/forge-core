package com.forge.core.merge.chat;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Owns {@code plugins/ForgeCore/chat.yml}: channel config, group formats,
 * mentions, anti-spam and word filter. Mute state lives in ForgeCore's
 * {@code MuteManager} — this manager integrates with it instead of
 * duplicating it.
 */
public final class ChatManager {
    private static volatile @Nullable ChatManager instance;

    /** May be null before {@link ChatSetup#init} runs (during early enable). */
    public static @Nullable ChatManager get() {
        return instance;
    }

    /** Cached per-player data. Computed on the main thread at join; read from async chat. */
    public record PlayerMeta(GroupFormat group, boolean staff, boolean spamBypass,
            boolean filterBypass, boolean color, boolean mentionEveryone) {
    }

    private final ForgeCore plugin;
    private final File file;
    private volatile ChatSettings settings;
    private final Map<UUID, PlayerMeta> metas = new ConcurrentHashMap<>();
    private final Map<UUID, Long> joinTimes = new ConcurrentHashMap<>();
    private final Map<ChatChannel, Long> slowmodes = new ConcurrentHashMap<>();
    private final ChatPipeline pipeline;

    public ChatManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "chat.yml");
        this.pipeline = new ChatPipeline(this);
        instance = this;
        reload();
        plugin.getServer().getPluginManager().registerEvents(new ChatListener(this), plugin);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            playerJoined(player);
        }
    }

    public ForgeCore plugin() {
        return plugin;
    }

    public ChatSettings settings() {
        return settings;
    }

    public ChatPipeline pipeline() {
        return pipeline;
    }

    /** Reload chat.yml from disk (main thread). */
    public void reload() {
        ensureDefaults();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        settings = ChatSettings.load(yml);
        slowmodes.clear();
        ConfigurationSection section = yml.getConfigurationSection("slowmode");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ChatChannel channel = ChatChannel.fromString(key);
                if (channel != null) {
                    slowmodes.put(channel, Math.max(0, section.getLong(key)));
                }
            }
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            cacheMeta(player);
        }
    }

    private void ensureDefaults() {
        if (file.exists()) {
            return;
        }
        YamlConfiguration yml = new YamlConfiguration();
        yml.set("channels.global.tag", "<gray>[G]</gray> ");
        yml.set("channels.global.format", "{tag}{name}<gray>:</gray> {message}");
        yml.set("channels.local.tag", "<yellow>[L]</yellow> ");
        yml.set("channels.local.format", "{tag}{name}<gray>:</gray> {message}");
        yml.set("channels.local.radius", 50.0);
        yml.set("channels.staff.tag", "<red>[Staff]</red> ");
        yml.set("channels.staff.format", "{tag}{name}<gray>:</gray> {message}");
        var admin = new java.util.LinkedHashMap<String, Object>();
        admin.put("permission", "forgecore.group.admin");
        admin.put("label", "Admin");
        admin.put("prefix", "<red>[Admin] </red>");
        admin.put("suffix", "");
        admin.put("name-format", "{name}");
        var def = new java.util.LinkedHashMap<String, Object>();
        def.put("permission", "");
        def.put("label", "Default");
        def.put("prefix", "<gray>");
        def.put("suffix", "<reset>");
        def.put("name-format", "{name}");
        yml.set("groups", List.of(admin, def));
        yml.set("hover-format", List.of("<white>{name}</white>", "<gray>Group: {group}</gray>",
                "<gray>Channel: {channel}</gray>"));
        yml.set("mentions.enabled", true);
        yml.set("mentions.sound", "entity.experience_orb_pickup");
        yml.set("mentions.volume", 1.0);
        yml.set("mentions.pitch", 1.6);
        yml.set("mentions.highlight", "<yellow><bold>@{name}</bold></yellow>");
        yml.set("antispam.enabled", true);
        yml.set("antispam.max-messages", 4);
        yml.set("antispam.window-seconds", 5);
        yml.set("antispam.max-repeat-chars", 5);
        yml.set("antispam.caps-percent", 70);
        yml.set("antispam.caps-min-length", 8);
        yml.set("antispam.block-links", true);
        yml.set("antispam.link-whitelist", List.of());
        yml.set("filter.enabled", true);
        yml.set("filter.action", "replace");
        yml.set("filter.replacement", "***");
        yml.set("filter.words", List.of());
        yml.set("slowmode.global", 0L);
        yml.set("slowmode.local", 0L);
        yml.set("slowmode.staff", 0L);
        yml.set("pm.format-to", "<gray>[<red>PM</red>]</gray> {sender}: {message}");
        yml.set("pm.format-from", "<gray>[<red>PM</red>]</gray> to {recipient}: {message}");
        yml.set("pm.format-spy", "<dark_gray>[SPY] {sender} -> {recipient}: {message}</dark_gray>");
        try {
            yml.save(file);
        } catch (java.io.IOException e) {
            plugin.getLogger().warning("Could not write default chat.yml: " + e.getMessage());
        }
    }

    // ---- player state ----

    public void playerJoined(Player player) {
        joinTimes.put(player.getUniqueId(), System.currentTimeMillis());
        cacheMeta(player);
    }

    public void playerQuit(Player player) {
        UUID id = player.getUniqueId();
        joinTimes.remove(id);
        metas.remove(id);
        pipeline.forget(id);
        SpamFilter.forget(id);
    }

    public void cacheMeta(Player player) {
        List<GroupFormat> groups = settings.groups();
        GroupFormat group = groups.get(groups.size() - 1);
        for (GroupFormat candidate : groups) {
            String permission = candidate.permission();
            if (permission.isEmpty() || player.hasPermission(permission)) {
                group = candidate;
                break;
            }
        }
        metas.put(player.getUniqueId(), new PlayerMeta(
                group,
                player.hasPermission("forgecore.staff") || player.isOp(),
                player.hasPermission("forgecore.chat.spam.bypass") || player.isOp(),
                player.hasPermission("forgecore.chat.filter.bypass") || player.isOp(),
                player.hasPermission("forgecore.chat.color"),
                player.hasPermission("forgecore.chat.mention.everyone") || player.isOp()));
    }

    public PlayerMeta meta(UUID id) {
        PlayerMeta meta = metas.get(id);
        if (meta != null) {
            return meta;
        }
        Player player = plugin.getServer().getPlayer(id);
        if (player != null) {
            cacheMeta(player);
            PlayerMeta cached = metas.get(id);
            if (cached != null) {
                return cached;
            }
        }
        List<GroupFormat> groups = settings.groups();
        GroupFormat fallback = groups.get(groups.size() - 1);
        return new PlayerMeta(fallback, false, false, false, false, false);
    }

    public GroupFormat groupOf(UUID id) {
        return meta(id).group();
    }

    public boolean isStaff(UUID id) {
        return meta(id).staff();
    }

    public long sessionSeconds(UUID id) {
        Long joined = joinTimes.get(id);
        return joined == null ? 0L : (System.currentTimeMillis() - joined) / 1000L;
    }

    // ---- channels ----

    /** A player's default chat channel, persisted in their userdata. */
    public ChatChannel channelOf(Player player) {
        ChatChannel channel = ChatChannel.fromString(
                plugin.users().get(player).getString("chat-channel", "global"));
        return channel == null ? ChatChannel.GLOBAL : channel;
    }

    public void setChannel(Player player, ChatChannel channel) {
        plugin.users().get(player).setString("chat-channel", channel.key());
        plugin.users().save(player.getUniqueId());
    }

    // ---- slowmode ----

    public long slowmode(ChatChannel channel) {
        return slowmodes.getOrDefault(channel, 0L);
    }

    public void setSlowmode(ChatChannel channel, long seconds) {
        long value = Math.max(0, seconds);
        slowmodes.put(channel, value);
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        yml.set("slowmode." + channel.key(), value);
        try {
            yml.save(file);
        } catch (java.io.IOException e) {
            plugin.getLogger().warning("Could not save chat.yml slowmode: " + e.getMessage());
        }
    }

    // ---- formatting helpers ----

    /**
     * Display name with group prefix/suffix. {@code displayName} is the
     * nickname (MiniMessage) or plain name.
     */
    public Component formatName(@Nullable Player player, String displayName) {
        GroupFormat group = player == null ? settings.groups().get(settings.groups().size() - 1)
                : groupOf(player.getUniqueId());
        return Component.text()
                .append(group.prefix())
                .append(ChatText.safe(displayName))
                .append(group.suffix())
                .build();
    }

    /** Hover card for a chat name. */
    public Component hoverCard(Player sender, ChatChannel channel) {
        var out = Component.text();
        List<String> lines = settings.hoverFormat();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                out.append(Component.newline());
            }
            String line = lines.get(i)
                    .replace("{name}", sender.getName())
                    .replace("{group}", groupOf(sender.getUniqueId()).label())
                    .replace("{channel}", channel.display())
                    .replace("{session}", formatDuration(sessionSeconds(sender.getUniqueId())));
            out.append(ChatText.safe(line));
        }
        return out.build();
    }

    private static String formatDuration(long totalSeconds) {
        if (totalSeconds < 60) {
            return totalSeconds + "s";
        }
        long minutes = totalSeconds / 60;
        if (minutes < 60) {
            return minutes + "m";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "h";
        }
        return (hours / 24) + "d";
    }

    /** Play the mention ping for targets on the main thread. */
    public void pingPlayers(java.util.List<Player> targets) {
        if (targets.isEmpty()) {
            return;
        }
        List<Player> copy = List.copyOf(targets);
        ChatSettings snap = settings;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Sound sound;
            try {
                sound = Sound.sound(Key.key(snap.mentionSound()), Sound.Source.PLAYER,
                        snap.mentionVolume(), snap.mentionPitch());
            } catch (Exception e) {
                return;
            }
            for (Player player : copy) {
                if (player.isOnline()) {
                    player.playSound(sound);
                }
            }
        });
    }

    /** Find an online player by exact name, falling back to a unique prefix match. */
    public @Nullable Player findPlayer(String input) {
        Player exact = plugin.getServer().getPlayerExact(input);
        if (exact != null) {
            return exact;
        }
        String lower = input.toLowerCase();
        Player prefix = null;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(lower)) {
                if (prefix != null) {
                    return null;
                }
                prefix = player;
            }
        }
        return prefix;
    }
}
