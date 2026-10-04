package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Restore food and saturation. */
public final class FeedCommand extends PlayerACommand {
    public FeedCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "feed";
    }

    @Override
    public String description() {
        return "Restore hunger and saturation.";
    }

    @Override
    public String usage() {
        return "/feed [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target = target(sender, args);
        if (target == null) {
            return;
        }
        target.setFoodLevel(20);
        target.setSaturation(20.0f);
        Text.ok(sender, "Fed <white>" + Text.escape(target.getName()) + "</white>.");
        if (!target.equals(sender)) {
            Text.send(target, "You were fed.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
