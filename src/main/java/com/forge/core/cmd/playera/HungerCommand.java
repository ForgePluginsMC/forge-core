package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Set food level (0-20). */
public final class HungerCommand extends PlayerACommand {
    public HungerCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "hunger";
    }

    @Override
    public String description() {
        return "Set food level (0-20).";
    }

    @Override
    public String usage() {
        return "/hunger <0-20> [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        int level;
        try {
            level = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            Text.error(sender, "Food level must be a number between 0 and 20.");
            return;
        }
        if (level < 0 || level > 20) {
            Text.error(sender, "Food level must be between 0 and 20.");
            return;
        }
        Player target = targetAt(sender, args, 1);
        if (target == null) {
            return;
        }
        target.setFoodLevel(level);
        Text.ok(sender, "Food level set to <white>" + level + "</white> for <white>"
                + Text.escape(target.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
