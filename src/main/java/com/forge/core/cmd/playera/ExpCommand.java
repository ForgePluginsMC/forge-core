package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Give experience. A trailing {@code L} means levels (e.g. {@code /exp 5L});
 * negative amounts take experience away.
 */
public final class ExpCommand extends PlayerACommand {
    public ExpCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "exp";
    }

    @Override
    public String description() {
        return "Give experience (suffix L for levels).";
    }

    @Override
    public String usage() {
        return "/exp <amount>[L] [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        String raw = args[0];
        boolean levels = raw.endsWith("L") || raw.endsWith("l");
        String number = levels ? raw.substring(0, raw.length() - 1) : raw;
        int amount;
        try {
            amount = Integer.parseInt(number);
        } catch (NumberFormatException exception) {
            Text.error(sender, "Amount must be a number, optionally suffixed with L for levels.");
            return;
        }
        Player target = targetAt(sender, args, 1);
        if (target == null) {
            return;
        }
        if (levels) {
            if (amount >= 0) {
                target.giveExpLevels(amount);
            } else {
                target.setLevel(Math.max(0, target.getLevel() + amount));
            }
        } else {
            if (amount >= 0) {
                target.giveExp(amount);
            } else {
                target.setTotalExperience(Math.max(0, target.getTotalExperience() + amount));
            }
        }
        String unit = levels ? "levels" : "XP";
        Text.ok(sender, "Gave <white>" + amount + " " + unit + "</white> to <white>"
                + Text.escape(target.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
