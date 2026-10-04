package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /tpaccept — accept a pending teleport request. */
public final class TpAcceptCommand extends TeleportCommand {
    public TpAcceptCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tpaccept";
    }

    @Override
    public java.util.List<String> aliases() {
        return java.util.List.of("tpyes");
    }

    @Override
    public String description() {
        return "Accept a pending teleport request.";
    }

    @Override
    public String usage() {
        return "/tpaccept";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (!plugin.tpa().accept(player)) {
            throw fail("You have no pending teleport request.");
        }
    }
}
