package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle AFK status. */
public final class AfkCommand extends PlayerACommand {
    public AfkCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "afk";
    }

    @Override
    public String description() {
        return "Toggle AFK status.";
    }

    @Override
    public String usage() {
        return "/afk";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        boolean now = !plugin.afk().isAfk(player);
        plugin.afk().setAfk(player, now);
        if (now) {
            Text.send(player, "<gray>You are now AFK.");
        } else {
            Text.send(player, "<gray>You are no longer AFK.");
        }
    }
}
