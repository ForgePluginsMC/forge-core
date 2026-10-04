package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Extinguish a burning player. */
public final class ExtCommand extends PlayerACommand {
    public ExtCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "ext";
    }

    @Override
    public List<String> aliases() {
        return List.of("extinguish");
    }

    @Override
    public String description() {
        return "Extinguish a burning player.";
    }

    @Override
    public String usage() {
        return "/ext [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target = target(sender, args);
        if (target == null) {
            return;
        }
        target.setFireTicks(0);
        Text.ok(sender, "Extinguished <white>" + Text.escape(target.getName()) + "</white>.");
        if (!target.equals(sender)) {
            Text.send(target, "You were extinguished.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
