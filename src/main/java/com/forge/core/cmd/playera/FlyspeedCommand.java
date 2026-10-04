package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Set flight speed (0-10, mapped to 0.0-1.0). */
public final class FlyspeedCommand extends PlayerACommand {
    public FlyspeedCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "flyspeed";
    }

    @Override
    public String description() {
        return "Set flight speed (0-10).";
    }

    @Override
    public String usage() {
        return "/flyspeed <0-10> [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        double speed;
        try {
            speed = Double.parseDouble(args[0]);
        } catch (NumberFormatException exception) {
            Text.error(sender, "Speed must be a number between 0 and 10.");
            return;
        }
        if (speed < 0 || speed > 10) {
            Text.error(sender, "Speed must be between 0 and 10.");
            return;
        }
        Player target = targetAt(sender, args, 1);
        if (target == null) {
            return;
        }
        target.setFlySpeed((float) (speed / 10.0));
        Text.ok(sender, "Flight speed set to <white>" + args[0] + "</white> for <white>"
                + Text.escape(target.getName()) + "</white>.");
        if (!target.equals(sender)) {
            Text.send(target, "Your flight speed was set to <white>" + Text.escape(args[0]) + "</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), args);
        }
        return playerNames(args);
    }
}
