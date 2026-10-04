package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /ender — open an ender chest; with a player and permission, open theirs. */
public final class EnderCommand extends ForgeCommand {
    public EnderCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "ender";
    }

    @Override
    public List<String> aliases() {
        return List.of("enderchest", "ec");
    }

    @Override
    public String description() {
        return "Open an ender chest (yours, or another player's).";
    }

    @Override
    public String usage() {
        return "/ender [player]";
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
        if (args.length == 0) {
            player.openInventory(player.getEnderChest());
            return;
        }
        if (!sender.hasPermission("forgecore.ender.others")) {
            Text.error(sender, "You don't have permission to do that.");
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        player.openInventory(target.getEnderChest());
        Text.ok(sender, "Opened <white>" + Text.escape(target.getName()) + "</white>'s ender chest.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("forgecore.ender.others")) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
