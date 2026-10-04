package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /walkspeed — set walk speed 0-10 (0.2 is vanilla default, i.e. 2). */
public final class WalkspeedCommand extends ForgeCommand {
    public WalkspeedCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "walkspeed";
    }

    @Override
    public List<String> aliases() {
        return List.of("wspeed");
    }

    @Override
    public String description() {
        return "Set walk speed from 0 to 10 (2 is normal).";
    }

    @Override
    public String usage() {
        return "/walkspeed <0-10> [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1 || args.length > 2) {
            Text.usage(sender, usage());
            return;
        }
        float value;
        try {
            value = Float.parseFloat(args[0]);
        } catch (NumberFormatException bad) {
            Text.error(sender, "<white>" + Text.escape(args[0]) + "</white> is not a number.");
            return;
        }
        if (value < 0 || value > 10) {
            Text.error(sender, "Speed must be between 0 and 10.");
            return;
        }
        Player target;
        if (args.length == 2) {
            if (!sender.hasPermission("forgecore.walkspeed.others")) {
                Text.error(sender, "You don't have permission to do that.");
                return;
            }
            target = Players.find(sender, args[1]);
            if (target == null) {
                return;
            }
        } else {
            target = asPlayer(sender);
            if (target == null) {
                Text.error(sender, "Only players can use that command.");
                return;
            }
        }
        target.setWalkSpeed(Math.min(1.0f, Math.max(0.0f, value / 10.0f)));
        if (target.equals(asPlayer(sender))) {
            Text.ok(sender, "Walk speed set to <white>" + value + "</white>.");
        } else {
            Text.ok(sender, "Set <white>" + Text.escape(target.getName()) + "</white>'s walk speed to <white>"
                    + value + "</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("0", "1", "2", "3", "4", "5"), args);
        }
        if (args.length == 2 && sender.hasPermission("forgecore.walkspeed.others")) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
