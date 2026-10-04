package com.forge.core.merge.chat;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/** /slowmode — set per-channel slowmode in seconds (0 disables). */
public final class SlowmodeCommand extends ForgeCommand {
    public SlowmodeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "slowmode";
    }

    @Override
    public String description() {
        return "Set a channel's slowmode delay in seconds.";
    }

    @Override
    public String usage() {
        return "/slowmode <global|local|staff> <seconds>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        ChatChannel channel = ChatChannel.fromString(args[0]);
        if (channel == null) {
            Text.error(sender, "Unknown channel. Use <white>global</white>, <white>local</white> or <white>staff</white>.");
            return;
        }
        long seconds;
        try {
            seconds = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            Text.error(sender, "Seconds must be a number.");
            return;
        }
        if (seconds < 0) {
            Text.error(sender, "Seconds cannot be negative.");
            return;
        }
        ChatManager manager = ChatManager.get();
        if (manager == null) {
            Text.error(sender, "Chat is not ready yet.");
            return;
        }
        manager.setSlowmode(channel, seconds);
        if (seconds == 0) {
            Text.ok(sender, "Slowmode disabled for <white>" + channel.display() + "</white>.");
        } else {
            Text.ok(sender, "Slowmode for <white>" + channel.display() + "</white>: <white>" + seconds + "s</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("global", "local", "staff"), args);
        }
        return List.of();
    }
}
