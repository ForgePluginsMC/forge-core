package com.forge.core.merge.chat;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /ch — show or set your default chat channel. */
public final class ChCommand extends ForgeCommand {
    public ChCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "ch";
    }

    @Override
    public List<String> aliases() {
        return List.of("channel");
    }

    @Override
    public String description() {
        return "Show or set your default chat channel.";
    }

    @Override
    public String usage() {
        return "/ch [global|local|staff]";
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
        ChatManager manager = ChatManager.get();
        if (manager == null) {
            Text.error(sender, "Chat is not ready yet.");
            return;
        }
        if (args.length == 0) {
            ChatChannel current = manager.channelOf(player);
            Text.send(sender, "Current channel: <white>" + current.display()
                    + "</white>. Use <white>/ch <global|local|staff></white> to switch.");
            return;
        }
        ChatChannel channel = ChatChannel.fromString(args[0]);
        if (channel == null) {
            Text.error(sender, "Unknown channel. Use <white>global</white>, <white>local</white> or <white>staff</white>.");
            return;
        }
        if (channel == ChatChannel.STAFF && !manager.isStaff(player.getUniqueId())) {
            Text.error(sender, "You don't have permission to use the staff channel.");
            return;
        }
        manager.setChannel(player, channel);
        Text.ok(sender, "Chat channel set to <white>" + channel.display() + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("global", "local", "staff"), args);
        }
        return List.of();
    }
}
