package com.forge.core.merge.chat;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /g — send a one-shot message to global chat. */
public final class GCommand extends ForgeCommand {
    public GCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "g";
    }

    @Override
    public String description() {
        return "Send a one-shot message to global chat.";
    }

    @Override
    public String usage() {
        return "/g <message...>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        ChatManager manager = ChatManager.get();
        if (manager == null) {
            Text.error(sender, "Chat is not ready yet.");
            return;
        }
        sendOneShot(manager, player, ChatChannel.GLOBAL, String.join(" ", args));
    }

    static void sendOneShot(ChatManager manager, Player player, ChatChannel channel, String text) {
        ChatPipeline.Result result = manager.pipeline().process(player, channel, text);
        if (result == null) {
            return;
        }
        for (Audience audience : result.viewers()) {
            audience.sendMessage(result.line().apply(audience));
        }
        manager.pingPlayers(result.pings());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
