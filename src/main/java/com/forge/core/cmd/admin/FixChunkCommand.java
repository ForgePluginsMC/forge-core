package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /fixchunk — unload and reload your current chunk from disk. */
public final class FixChunkCommand extends ForgeCommand {
    public FixChunkCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "fixchunk";
    }

    @Override
    public String description() {
        return "Unload and reload your current chunk from disk.";
    }

    @Override
    public String usage() {
        return "/fixchunk";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        World world = player.getWorld();
        Chunk chunk = player.getChunk();
        world.unloadChunk(chunk.getX(), chunk.getZ(), true);
        world.loadChunk(chunk.getX(), chunk.getZ(), true);
        Text.ok(sender, "Chunk <white>" + chunk.getX() + ", " + chunk.getZ()
                + "</white> reloaded from disk.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
