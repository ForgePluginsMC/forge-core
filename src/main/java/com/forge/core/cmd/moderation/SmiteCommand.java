package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Strike lightning — at a player, or where you are looking. Real damage. */
public final class SmiteCommand extends ForgeCommand {
    public SmiteCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "smite";
    }

    @Override
    public String description() {
        return "Strike lightning at a player (or where you look).";
    }

    @Override
    public String usage() {
        return "/smite [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Location strikeAt;
        String targetName;
        if (args.length >= 1) {
            Player target = Players.find(sender, args[0]);
            if (target == null) {
                return;
            }
            strikeAt = target.getLocation();
            targetName = target.getName();
        } else {
            Player player = asPlayer(sender);
            if (player == null) {
                Text.error(sender, "Only players can use that command.");
                return;
            }
            Block block = player.getTargetBlockExact(200);
            if (block == null) {
                throw new CommandRegistry.CommandFailure("No block in sight.");
            }
            strikeAt = block.getLocation();
            targetName = "that spot";
        }
        strikeAt.getWorld().strikeLightning(strikeAt);
        Text.ok(sender, "Smote <white>" + Text.escape(targetName) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
