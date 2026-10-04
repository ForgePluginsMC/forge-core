package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.merge.chat.ChatManager;
import com.forge.core.merge.chat.ChatText;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /msg — private message. Blocked when the sender is muted/silenced, and when
 * the target ignores the sender (unless the sender has
 * {@code forgecore.ignore.bypass}). Copies go to social spies.
 *
 * <p>Names use the merged chat system's group formatting; PM line templates
 * come from {@code chat.yml} ({@code pm.format-to/from/spy}).
 */
public final class MsgCommand extends ForgeCommand {
    public MsgCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "msg";
    }

    @Override
    public List<String> aliases() {
        return List.of("tell", "w", "pm");
    }

    @Override
    public String description() {
        return "Send a private message to a player.";
    }

    @Override
    public String usage() {
        return "/msg <player> <message...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        Player fromPlayer = asPlayer(sender);
        String fromName = fromPlayer == null ? "Console"
                : plugin.users().get(fromPlayer).nickOrName(fromPlayer);
        UUID fromUuid = MsgManager.uuidOf(fromPlayer);
        if (fromUuid.equals(target.getUniqueId())) {
            Text.error(sender, "You cannot message yourself.");
            return;
        }
        deliver(plugin, sender, fromName, fromUuid, target, String.join(" ", argsRange(args, 1)));
    }

    private static String[] argsRange(String[] args, int from) {
        String[] out = new String[args.length - from];
        System.arraycopy(args, from, out, 0, out.length);
        return out;
    }

    /**
     * Shared delivery for /msg and /reply: mute/silence/ignore checks, message
     * formatting, social-spy copies and last-messager bookkeeping.
     */
    static void deliver(ForgeCore plugin, CommandSender sender, String fromName, UUID fromUuid,
            Player target, String message) {
        Player fromPlayer = fromUuid.equals(MsgManager.CONSOLE_UUID) ? null
                : plugin.getServer().getPlayer(fromUuid);
        if (fromPlayer != null
                && (plugin.mutes().isMuted(fromPlayer) || plugin.mutes().isSilenced(fromPlayer))) {
            Text.error(sender, "You cannot send private messages while muted or silenced.");
            return;
        }
        if (MsgManager.get().isIgnoring(target.getUniqueId(), fromUuid)
                && !sender.hasPermission("forgecore.ignore.bypass")) {
            Text.error(sender, "That player is ignoring you.");
            return;
        }
        String targetName = plugin.users().get(target).nickOrName(target);
        ChatManager chat = ChatManager.get();
        boolean color = chat != null && fromPlayer != null && chat.meta(fromPlayer.getUniqueId()).color();
        Component body = color ? ChatText.safe(message) : Component.text(message);
        Component fromComp = chat == null
                ? Text.of("<gold>" + Text.escape(fromName) + "</gold>")
                : chat.formatName(fromPlayer, fromName);
        Component toComp = chat == null
                ? Text.of("<gold>" + Text.escape(targetName) + "</gold>")
                : chat.formatName(target, targetName);
        TagResolver senderR = TagResolver.resolver("sender", Tag.inserting(fromComp));
        TagResolver recipientR = TagResolver.resolver("recipient", Tag.inserting(toComp));
        TagResolver messageR = TagResolver.resolver("message", Tag.inserting(body));

        Component legacyTo = Text.of("<gray>[<gold>" + Text.escape(fromName) + "</gold> -> <gold>me</gold>]</gray> "
                + Text.escape(message));
        Component legacyFrom = Text.of("<gray>[<gold>me</gold> -> <gold>" + Text.escape(targetName)
                + "</gold>]</gray> " + Text.escape(message));
        Component legacySpy = Text.of("<gray>[spy] " + Text.escape(fromName) + " -> "
                + Text.escape(targetName) + ": " + Text.escape(message));
        Component toLine = chat == null ? legacyTo
                : ChatText.render(ChatText.bracesToTags(chat.settings().pmFormatTo()), legacyTo, senderR, messageR);
        Component fromLine = chat == null ? legacyFrom
                : ChatText.render(ChatText.bracesToTags(chat.settings().pmFormatFrom()), legacyFrom, recipientR,
                        messageR);
        Component spyLine = chat == null ? legacySpy
                : ChatText.render(ChatText.bracesToTags(chat.settings().pmFormatSpy()), legacySpy, senderR,
                        recipientR, messageR);

        target.sendMessage(toLine);
        if (!(sender instanceof Player senderPlayer && senderPlayer.getUniqueId().equals(target.getUniqueId()))) {
            sender.sendMessage(fromLine);
        }
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.getUniqueId().equals(fromUuid) || online.getUniqueId().equals(target.getUniqueId())) {
                continue;
            }
            if (plugin.mutes().socialSpy(online)) {
                online.sendMessage(spyLine);
            }
        }
        MsgManager.get().setLast(fromUuid, target.getUniqueId());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
