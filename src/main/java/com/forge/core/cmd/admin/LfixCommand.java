package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /lfix — resend your current chunk to fix lighting glitches. */
public final class LfixCommand extends ForgeCommand {
    public LfixCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "lfix";
    }

    @Override
    public String description() {
        return "Resend your current chunk (fixes lighting glitches).";
    }

    @Override
    public String usage() {
        return "/lfix";
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
        // Unload without saving, then reload: forces the server to re-send
        // fresh chunk data (including lighting) to everyone nearby.
        world.unloadChunk(chunk.getX(), chunk.getZ(), false);
        world.loadChunk(chunk.getX(), chunk.getZ(), true);
        Text.ok(sender, "Chunk <white>" + chunk.getX() + ", " + chunk.getZ()
                + "</white> re-sent. Lighting should fix itself.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
