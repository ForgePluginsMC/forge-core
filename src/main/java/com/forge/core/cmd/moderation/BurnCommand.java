package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Set a player on fire. */
public final class BurnCommand extends ForgeCommand {
    public BurnCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "burn";
    }

    @Override
    public String description() {
        return "Set a player on fire.";
    }

    @Override
    public String usage() {
        return "/burn <player> [seconds]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        int seconds = 5;
        if (args.length >= 2) {
            try {
                seconds = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Text.error(sender, "Seconds must be a number.");
                return;
            }
            if (seconds < 1 || seconds > 300) {
                Text.error(sender, "Seconds must be between 1 and 300.");
                return;
            }
        }
        target.setFireTicks(seconds * 20);
        Text.ok(sender, "Set <white>" + Text.escape(target.getName()) + "</white> on fire for "
                + seconds + "s.");
        Staff.notify("<red><bold>Burn:</bold></red> <white>" + Text.escape(target.getName())
                + "</white> set on fire by <white>" + Text.escape(sender.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
