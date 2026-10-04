package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry.CommandFailure;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/** /spawnmob — spawn entities at your target block, or at another player. */
public final class SpawnMobCommand extends ForgeCommand {
    public SpawnMobCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "spawnmob";
    }

    @Override
    public String description() {
        return "Spawn mobs at your target block or at a player.";
    }

    @Override
    public String usage() {
        return "/spawnmob <entity-type> [amount] [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        EntityType type = AdminUtil.entityType(args[0]);
        int amount = args.length >= 2 ? AdminUtil.intInRange(args[1], 1, 50, "Amount") : 1;

        Location location;
        if (args.length >= 3) {
            Player target = Players.find(sender, args[2]);
            if (target == null) {
                return;
            }
            location = target.getLocation();
        } else {
            Player player = asPlayer(sender);
            if (player == null) {
                Text.error(sender, "Console must specify a player: " + usage());
                return;
            }
            Block block = player.getTargetBlockExact(64);
            if (block == null) {
                Text.error(sender, "Look at a block first, or specify a player.");
                return;
            }
            location = block.getRelative(BlockFace.UP).getLocation().add(0.5, 0, 0.5);
        }

        int spawned = 0;
        for (int i = 0; i < amount; i++) {
            try {
                location.getWorld().spawnEntity(location, type);
                spawned++;
            } catch (IllegalArgumentException bad) {
                throw new CommandFailure("Could not spawn " + type.name().toLowerCase(Locale.ROOT) + " here.");
            }
        }
        Text.ok(sender, "Spawned <white>" + spawned + "</white> "
                + type.name().toLowerCase(Locale.ROOT).replace('_', ' ') + ".");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(AdminUtil.entityNames(), args);
        }
        if (args.length == 3) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
