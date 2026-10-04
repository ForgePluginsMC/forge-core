package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Set saturation (0-20). */
public final class SaturationCommand extends PlayerACommand {
    public SaturationCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "saturation";
    }

    @Override
    public String description() {
        return "Set saturation (0-20).";
    }

    @Override
    public String usage() {
        return "/saturation <0-20> [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        float level;
        try {
            level = Float.parseFloat(args[0]);
        } catch (NumberFormatException exception) {
            Text.error(sender, "Saturation must be a number between 0 and 20.");
            return;
        }
        if (level < 0 || level > 20) {
            Text.error(sender, "Saturation must be between 0 and 20.");
            return;
        }
        Player target = targetAt(sender, args, 1);
        if (target == null) {
            return;
        }
        target.setSaturation(level);
        Text.ok(sender, "Saturation set to <white>" + args[0] + "</white> for <white>"
                + Text.escape(target.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
