package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /tpdeny — deny a pending teleport request. */
public final class TpDenyCommand extends TeleportCommand {
    public TpDenyCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tpdeny";
    }

    @Override
    public java.util.List<String> aliases() {
        return java.util.List.of("tpno");
    }

    @Override
    public String description() {
        return "Deny a pending teleport request.";
    }

    @Override
    public String usage() {
        return "/tpdeny";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        if (!plugin.tpa().deny(player)) {
            throw fail("You have no pending teleport request.");
        }
    }
}
