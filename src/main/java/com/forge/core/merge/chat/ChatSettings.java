package com.forge.core.merge.chat;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Immutable snapshot of {@code chat.yml}. Rebuilt on the main thread by
 * {@link ChatManager#reload()}; the async chat pipeline only reads this.
 */
public record ChatSettings(
        Map<ChatChannel, ChannelFormat> channels,
        List<GroupFormat> groups,
        List<String> hoverFormat,
        boolean mentionsEnabled,
        String mentionSound,
        float mentionVolume,
        float mentionPitch,
        String mentionHighlight,
        boolean antispamEnabled,
        int antispamMaxMessages,
        int antispamWindowSeconds,
        int antispamMaxRepeatChars,
        int antispamCapsPercent,
        int antispamCapsMinLength,
        boolean antispamBlockLinks,
        List<String> antispamLinkWhitelist,
        boolean filterEnabled,
        String filterAction,
        String filterReplacement,
        List<String> filterWords,
        String pmFormatTo,
        String pmFormatFrom,
        String pmFormatSpy) {

    public record ChannelFormat(Component tag, String format, double radius) {
    }

    static ChatSettings load(YamlConfiguration yml) {
        Map<ChatChannel, ChannelFormat> channels = new EnumMap<>(ChatChannel.class);
        for (ChatChannel channel : ChatChannel.values()) {
            String base = "channels." + channel.key() + ".";
            Component tag = ChatText.safe(yml.getString(base + "tag", defaultTag(channel)));
            String format = yml.getString(base + "format", "{tag}{name}<gray>:</gray> {message}");
            double radius = yml.getDouble(base + "radius", 50.0);
            channels.put(channel, new ChannelFormat(tag, format, radius));
        }
        List<GroupFormat> groups = new ArrayList<>();
        for (Map<?, ?> entry : yml.getMapList("groups")) {
            groups.add(GroupFormat.load(entry));
        }
        if (groups.isEmpty()) {
            groups.add(GroupFormat.fallback());
        }
        List<String> hover = yml.getStringList("hover-format");
        if (hover.isEmpty()) {
            hover = List.of("<white>{name}</white>", "<gray>Group: {group}</gray>", "<gray>Channel: {channel}</gray>");
        }
        return new ChatSettings(
                Map.copyOf(channels),
                List.copyOf(groups),
                List.copyOf(hover),
                yml.getBoolean("mentions.enabled", true),
                yml.getString("mentions.sound", "entity.experience_orb_pickup"),
                (float) yml.getDouble("mentions.volume", 1.0),
                (float) yml.getDouble("mentions.pitch", 1.6),
                yml.getString("mentions.highlight", "<yellow><bold>@{name}</bold></yellow>"),
                yml.getBoolean("antispam.enabled", true),
                yml.getInt("antispam.max-messages", 4),
                yml.getInt("antispam.window-seconds", 5),
                yml.getInt("antispam.max-repeat-chars", 5),
                yml.getInt("antispam.caps-percent", 70),
                yml.getInt("antispam.caps-min-length", 8),
                yml.getBoolean("antispam.block-links", true),
                List.copyOf(yml.getStringList("antispam.link-whitelist")),
                yml.getBoolean("filter.enabled", true),
                yml.getString("filter.action", "replace"),
                yml.getString("filter.replacement", "***"),
                List.copyOf(yml.getStringList("filter.words")),
                yml.getString("pm.format-to", "<gray>[<red>PM</red>]</gray> {sender}: {message}"),
                yml.getString("pm.format-from", "<gray>[<red>PM</red>]</gray> to {recipient}: {message}"),
                yml.getString("pm.format-spy", "<dark_gray>[SPY] {sender} -> {recipient}: {message}</dark_gray>"));
    }

    private static String defaultTag(ChatChannel channel) {
        return switch (channel) {
            case GLOBAL -> "<gray>[G]</gray> ";
            case LOCAL -> "<yellow>[L]</yellow> ";
            case STAFF -> "<red>[Staff]</red> ";
        };
    }
}
