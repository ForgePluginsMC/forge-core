package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /suicide — kill yourself. */
public final class SuicideCommand extends ForgeCommand {
    public SuicideCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "suicide";
    }

    @Override
    public String description() {
        return "Kill yourself.";
    }

    @Override
    public String usage() {
        return "/suicide";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        SitCommand.standUp(player, false);
        player.damage(1000.0);
    }
}
