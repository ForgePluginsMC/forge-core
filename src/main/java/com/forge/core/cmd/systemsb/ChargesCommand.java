package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Show flight charges (yours, or another player's). */
public final class ChargesCommand extends ForgeCommand {
    public ChargesCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "charges";
    }

    @Override
    public String description() {
        return "Show flight charges.";
    }

    @Override
    public String usage() {
        return "/charges [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target;
        if (args.length == 0) {
            target = asPlayer(sender);
            if (target == null) {
                Text.usage(sender, usage());
                return;
            }
        } else {
            if (!sender.hasPermission("forgecore.charges.others")) {
                throw new CommandRegistry.CommandFailure(
                        "You don't have permission to check other players' charges.");
            }
            target = Players.find(sender, args[0]);
            if (target == null) {
                return;
            }
        }
        Text.send(sender, Text.escape(target.getName()) + " has <white>"
                + SystemsBSetup.flight().display(target.getUniqueId()) + "</white> flight charges.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
