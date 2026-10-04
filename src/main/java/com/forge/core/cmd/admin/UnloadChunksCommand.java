package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /unloadchunks — force-unload chunks with no players nearby. */
public final class UnloadChunksCommand extends ForgeCommand {
    /** Chunks within this many blocks of a player are kept loaded. */
    private static final int KEEP_RADIUS_BLOCKS = 160;

    public UnloadChunksCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "unloadchunks";
    }

    @Override
    public String description() {
        return "Force-unload chunks with no players nearby (may hitch).";
    }

    @Override
    public String usage() {
        return "/unloadchunks [world]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        World world;
        if (args.length >= 1) {
            world = Bukkit.getWorld(args[0]);
            if (world == null) {
                Text.error(sender, "Unknown world: " + Text.escape(args[0]));
                return;
            }
        } else if (sender instanceof Player player) {
            world = player.getWorld();
        } else {
            Text.usage(sender, usage());
            return;
        }

        Text.send(sender, "<yellow>Unloading idle chunks in <white>" + Text.escape(world.getName())
                + "</white> — this may cause a brief hitch…");
        List<Player> players = new ArrayList<>(world.getPlayers());
        int unloaded = 0;
        for (Chunk chunk : world.getLoadedChunks()) {
            int centerX = (chunk.getX() << 4) + 8;
            int centerZ = (chunk.getZ() << 4) + 8;
            boolean nearby = false;
            for (Player player : players) {
                if (Math.abs(player.getLocation().getX() - centerX) < KEEP_RADIUS_BLOCKS
                        && Math.abs(player.getLocation().getZ() - centerZ) < KEEP_RADIUS_BLOCKS) {
                    nearby = true;
                    break;
                }
            }
            if (!nearby && world.unloadChunk(chunk.getX(), chunk.getZ(), true)) {
                unloaded++;
            }
        }
        Text.ok(sender, "Unloaded <white>" + unloaded + "</white> idle chunks in <white>"
                + Text.escape(world.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            List<String> worlds = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                worlds.add(world.getName());
            }
            return Players.filter(worlds, args);
        }
        return List.of();
    }
}
