package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Set max health (1-1024). */
public final class MaxhpCommand extends PlayerACommand {
    public MaxhpCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "maxhp";
    }

    @Override
    public String description() {
        return "Set max health (1-1024).";
    }

    @Override
    public String usage() {
        return "/maxhp <amount> [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException exception) {
            Text.error(sender, "Amount must be a number between 1 and 1024.");
            return;
        }
        if (amount < 1 || amount > 1024) {
            Text.error(sender, "Amount must be between 1 and 1024.");
            return;
        }
        Player target = targetAt(sender, args, 1);
        if (target == null) {
            return;
        }
        var attribute = target.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) {
            Text.error(sender, "Could not access the health attribute.");
            return;
        }
        attribute.setBaseValue(amount);
        if (target.getHealth() > amount) {
            target.setHealth(amount);
        }
        Text.ok(sender, "Max health set to <white>" + args[0] + "</white> for <white>"
                + Text.escape(target.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
