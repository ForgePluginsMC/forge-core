package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /tptoggle — toggle whether you accept teleport requests. */
public final class TpToggleCommand extends TeleportCommand {
    public TpToggleCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tptoggle";
    }

    @Override
    public String description() {
        return "Toggle accepting teleport requests.";
    }

    @Override
    public String usage() {
        return "/tptoggle";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        boolean accepting = plugin.tpa().accepting(player);
        plugin.tpa().setAccepting(player, !accepting);
        Text.send(sender, !accepting
                ? "<gray>You are now <green>accepting</green> teleport requests."
                : "<gray>You are now <red>ignoring</red> teleport requests.");
    }
}
