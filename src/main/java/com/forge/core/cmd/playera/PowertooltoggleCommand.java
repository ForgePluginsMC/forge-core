package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle all your powertools on/off. */
public final class PowertooltoggleCommand extends PlayerACommand {
    public PowertooltoggleCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "powertooltoggle";
    }

    @Override
    public List<String> aliases() {
        return List.of("pttoggle");
    }

    @Override
    public String description() {
        return "Toggle your powertools on or off.";
    }

    @Override
    public String usage() {
        return "/powertooltoggle";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        boolean now = PlayerAState.powertools().toggle(player);
        Text.ok(sender, "Powertools " + (now ? "enabled" : "disabled") + ".");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
