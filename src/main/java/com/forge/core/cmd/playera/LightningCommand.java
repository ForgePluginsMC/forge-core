package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Strike lightning at a player (or where you're looking). */
public final class LightningCommand extends PlayerACommand {
    public LightningCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "lightning";
    }

    @Override
    public List<String> aliases() {
        return List.of("strike", "smite2");
    }

    @Override
    public String description() {
        return "Strike lightning at a player or where you look.";
    }

    @Override
    public String usage() {
        return "/lightning [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            var target = player.getTargetBlockExact(100);
            if (target == null) {
                Text.error(sender, "No block in sight within 100 blocks.");
                return;
            }
            player.getWorld().strikeLightning(target.getLocation());
            Text.ok(sender, "Struck lightning.");
            return;
        }
        Player target = requiredTarget(sender, args);
        if (target == null) {
            return;
        }
        target.getWorld().strikeLightning(target.getLocation());
        Text.ok(sender, "Struck lightning at <white>" + Text.escape(target.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
