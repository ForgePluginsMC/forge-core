package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Refill remaining air to maximum. */
public final class AirCommand extends PlayerACommand {
    public AirCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "air";
    }

    @Override
    public String description() {
        return "Refill your air supply.";
    }

    @Override
    public String usage() {
        return "/air [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target = target(sender, args);
        if (target == null) {
            return;
        }
        target.setRemainingAir(target.getMaximumAir());
        Text.ok(sender, "Air refilled for <white>" + Text.escape(target.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
