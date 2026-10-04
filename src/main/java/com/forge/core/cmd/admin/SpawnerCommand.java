package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/** /spawner — set the spawn type of the mob spawner you are looking at. */
public final class SpawnerCommand extends ForgeCommand {
    public SpawnerCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "spawner";
    }

    @Override
    public String description() {
        return "Set the entity type of the spawner you are looking at.";
    }

    @Override
    public String usage() {
        return "/spawner <entity-type>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Player player = (Player) sender;
        EntityType type = AdminUtil.entityType(args[0]);
        Block block = player.getTargetBlockExact(64);
        if (block == null || !(block.getState() instanceof CreatureSpawner spawner)) {
            Text.error(sender, "Look at a mob spawner first.");
            return;
        }
        spawner.setSpawnedType(type);
        spawner.update();
        Text.ok(sender, "Spawner now spawns <white>"
                + type.name().toLowerCase(Locale.ROOT).replace('_', ' ') + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(AdminUtil.entityNames(), args);
        }
        return List.of();
    }
}
