package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Show a player's experience: level, total points and progress. */
public final class CheckexpCommand extends PlayerACommand {
    public CheckexpCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "checkexp";
    }

    @Override
    public String description() {
        return "Show experience details.";
    }

    @Override
    public String usage() {
        return "/checkexp [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target = target(sender, args);
        if (target == null) {
            return;
        }
        int percent = Math.round(target.getExp() * 100);
        Text.send(sender, "<gray>Experience for <white>" + Text.escape(target.getName()) + "</white>: "
                + "<green>level " + target.getLevel() + "</green>, "
                + "<green>" + target.getTotalExperience() + " total XP</green> "
                + "<gray>(" + percent + "% to next level).</gray>");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
