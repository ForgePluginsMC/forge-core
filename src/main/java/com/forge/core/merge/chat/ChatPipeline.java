package com.forge.core.merge.chat;

import com.forge.core.cmd.playerb.MsgManager;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * The shared chat pipeline used by the AsyncChatEvent listener and the
 * /g /l one-shot commands. Runs mute, slowmode, anti-spam and word filter
 * checks, resolves @mentions, computes the viewer set per channel and builds
 * the final formatted line.
 *
 * <p>Thread-safe: only reads the immutable {@link ChatSettings} snapshot and
 * concurrent maps; never touches Bukkit API except thread-safe queries.
 */
public final class ChatPipeline {

    public record Result(Function<Audience, Component> line, Set<Audience> viewers, List<Player> pings) {
    }

    private record Mention(MentionParser.Span span, Player target) {
    }

    private record PlayerNamed(Player player) implements MentionParser.Named {
        @Override
        public String name() {
            return player.getName();
        }
    }

    private final ChatManager manager;

    public ChatPipeline(ChatManager manager) {
        this.manager = manager;
    }

    /** Drop per-player chat state (call on quit). */
    public void forget(UUID id) {
        lastChat.remove(id);
    }

    private final Map<UUID, Map<ChatChannel, Long>> lastChat = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Process a chat message. Returns null when the message was blocked, in
     * which case the sender has already been notified.
     */
    public @Nullable Result process(Player sender, ChatChannel channel, @Nullable String rawText) {
        String text = rawText == null ? "" : rawText.trim();
        if (text.isEmpty()) {
            return null;
        }
        var plugin = manager.plugin();
        ChatSettings settings = manager.settings();
        UUID id = sender.getUniqueId();
        ChatManager.PlayerMeta meta = manager.meta(id);

        if (plugin.mutes().isMuted(sender) || plugin.mutes().isSilenced(sender)) {
            sender.sendMessage(Text.of("<red>You cannot chat while muted or silenced."));
            return null;
        }
        if (plugin.mutes().chatMuted() && !sender.hasPermission("forgecore.mutechat.bypass")) {
            sender.sendMessage(Text.of("<red>Chat is currently muted."));
            return null;
        }

        if (channel == ChatChannel.STAFF && !meta.staff()) {
            sender.sendMessage(Text.of("<red>You don't have permission to use the staff channel."));
            return null;
        }

        long slowSeconds = manager.slowmode(channel);
        if (slowSeconds > 0) {
            long elapsed = (System.currentTimeMillis() - lastChatOf(id, channel)) / 1000L;
            if (elapsed < slowSeconds) {
                sender.sendMessage(Text.of("<red>Slow down! Wait <white>" + (slowSeconds - elapsed)
                        + "s</white> before chatting again."));
                return null;
            }
        }

        if (settings.antispamEnabled() && !meta.spamBypass()) {
            SpamFilter.Fail fail = SpamFilter.check(sender, text,
                    settings.antispamMaxMessages(),
                    settings.antispamWindowSeconds(),
                    settings.antispamMaxRepeatChars(),
                    settings.antispamCapsPercent(),
                    settings.antispamCapsMinLength(),
                    settings.antispamBlockLinks(),
                    settings.antispamLinkWhitelist());
            if (fail != null) {
                String message = switch (fail) {
                    case RATE -> "<red>You're sending messages too fast.</red>";
                    case REPEAT -> "<red>Too many repeated characters.</red>";
                    case CAPS -> "<red>Too much caps lock.</red>";
                    case LINK -> "<red>Links are not allowed in chat.</red>";
                };
                sender.sendMessage(Text.of(message));
                return null;
            }
        }

        if (settings.filterEnabled() && !meta.filterBypass()) {
            List<String> words = settings.filterWords();
            boolean hit = false;
            String lower = text.toLowerCase();
            for (String word : words) {
                if (!word.isBlank() && lower.contains(word.toLowerCase())) {
                    hit = true;
                    break;
                }
            }
            if (hit) {
                if (settings.filterAction().equalsIgnoreCase("cancel")) {
                    sender.sendMessage(Text.of("<red>Your message was blocked by the word filter."));
                    return null;
                }
                String replacement = settings.filterReplacement();
                for (String word : words) {
                    if (word.isBlank()) {
                        continue;
                    }
                    text = text.replaceAll("(?i)" + Pattern.quote(word), Matcher.quoteReplacement(replacement));
                }
            }
        }
        markChat(id, channel);

        boolean mentionsOn = settings.mentionsEnabled();
        Set<Audience> viewers = viewersFor(sender, channel, settings);
        // ForgeCore ignores cover public chat too: drop viewers ignoring the sender.
        viewers.removeIf(audience -> audience instanceof Player viewer
                && MsgManager.get().isIgnoring(viewer.getUniqueId(), id));
        List<Player> candidates = new ArrayList<>();
        for (Audience audience : viewers) {
            if (audience instanceof Player player) {
                candidates.add(player);
            }
        }

        List<Mention> hits = new ArrayList<>();
        boolean pingEveryone = false;
        if (mentionsOn) {
            Map<String, PlayerNamed> byName = new HashMap<>();
            PlayerNamed self = new PlayerNamed(sender);
            for (Player candidate : candidates) {
                byName.put(candidate.getName().toLowerCase(), new PlayerNamed(candidate));
            }
            for (MentionParser.Span span : MentionParser.findSpans(text)) {
                if (MentionParser.isBroadcastToken(span.token())) {
                    if (meta.mentionEveryone()) {
                        pingEveryone = true;
                    }
                    continue;
                }
                PlayerNamed resolved = MentionParser.resolve(span.token(), self, byName.values());
                if (resolved != null) {
                    Player target = resolved.player();
                    boolean duplicate = false;
                    for (Mention existing : hits) {
                        if (existing.target().equals(target)) {
                            duplicate = true;
                            break;
                        }
                    }
                    if (!duplicate) {
                        hits.add(new Mention(span, target));
                    }
                }
            }
        }

        Component messageComponent = buildMessage(text, hits, meta.color(), settings);
        Component nameComponent = manager.formatName(sender, nickOf(sender))
                .hoverEvent(HoverEvent.showText(manager.hoverCard(sender, channel)));
        ChatSettings.ChannelFormat channelFormat = settings.channels().get(channel);
        Component tagComponent = channelFormat.tag();
        String format = ChatText.bracesToTags(channelFormat.format());

        Component fallbackLine = Component.text().append(tagComponent).append(nameComponent)
                .append(Component.text(": ")).append(messageComponent).build();
        TagResolver tagR = TagResolver.resolver("tag", Tag.inserting(tagComponent));
        TagResolver nameR = TagResolver.resolver("name", Tag.inserting(nameComponent));
        TagResolver messageR = TagResolver.resolver("message", Tag.inserting(messageComponent));
        Function<Audience, Component> line = viewer -> ChatText.render(format, fallbackLine, tagR, nameR, messageR);

        List<Player> pings = new ArrayList<>();
        for (Mention hit : hits) {
            pings.add(hit.target());
        }
        if (pingEveryone) {
            for (Player candidate : candidates) {
                if (!candidate.getUniqueId().equals(id) && !pings.contains(candidate)) {
                    pings.add(candidate);
                }
            }
        }
        return new Result(line, viewers, pings);
    }

    private String nickOf(Player player) {
        return manager.plugin().users().get(player).nickOrName(player);
    }

    private Set<Audience> viewersFor(Player sender, ChatChannel channel, ChatSettings settings) {
        Set<Audience> viewers = new HashSet<>();
        var plugin = manager.plugin();
        switch (channel) {
            case GLOBAL -> viewers.addAll(plugin.getServer().getOnlinePlayers());
            case LOCAL -> {
                double radius = settings.channels().get(ChatChannel.LOCAL).radius();
                viewers.addAll(sender.getWorld().getNearbyPlayers(sender.getLocation(), radius));
                viewers.add(sender);
            }
            case STAFF -> {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (manager.isStaff(player.getUniqueId())) {
                        viewers.add(player);
                    }
                }
            }
        }
        viewers.add(plugin.getServer().getConsoleSender());
        return viewers;
    }

    private Component buildMessage(String text, List<Mention> mentions, boolean allowColor, ChatSettings settings) {
        String highlight = settings.mentionHighlight();
        List<Mention> ordered = new ArrayList<>(mentions);
        ordered.sort(Comparator.comparingInt(m -> m.span().start()));
        TextComponent.Builder out = Component.text();
        int index = 0;
        for (Mention mention : ordered) {
            int start = mention.span().start();
            int end = mention.span().end();
            if (start > index) {
                out.append(segment(text.substring(index, start), allowColor));
            }
            out.append(ChatText.safe(highlight.replace("{name}", mention.target().getName())));
            index = end;
        }
        if (index < text.length()) {
            out.append(segment(text.substring(index), allowColor));
        }
        return out.build();
    }

    private Component segment(String text, boolean allowColor) {
        return allowColor ? ChatText.safe(text) : Component.text(text);
    }

    private long lastChatOf(UUID id, ChatChannel channel) {
        Map<ChatChannel, Long> map = lastChat.get(id);
        return map == null ? 0L : map.getOrDefault(channel, 0L);
    }

    private void markChat(UUID id, ChatChannel channel) {
        lastChat.computeIfAbsent(id, k -> new java.util.concurrent.ConcurrentHashMap<>())
                .put(channel, System.currentTimeMillis());
    }
}
