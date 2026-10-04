package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /clearender — clear an ender chest (yours, or another player's with permission). */
public final class ClearenderCommand extends ForgeCommand {
    public ClearenderCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "clearender";
    }

    @Override
    public String description() {
        return "Clear an ender chest.";
    }

    @Override
    public String usage() {
        return "/clearender [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            Text.usage(sender, usage());
            return;
        }
        Player target;
        if (args.length == 1) {
            if (!sender.hasPermission("forgecore.clearender.others")) {
                Text.error(sender, "You don't have permission to do that.");
                return;
            }
            target = Players.find(sender, args[0]);
            if (target == null) {
                return;
            }
        } else {
            target = asPlayer(sender);
            if (target == null) {
                Text.error(sender, "Only players can use that command.");
                return;
            }
        }
        target.getEnderChest().clear();
        if (target.equals(asPlayer(sender))) {
            Text.ok(sender, "Your ender chest was cleared.");
        } else {
            Text.ok(sender, "Cleared <white>" + Text.escape(target.getName()) + "</white>'s ender chest.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("forgecore.clearender.others")) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
