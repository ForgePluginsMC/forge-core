package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

/** Save all worlds and all ForgeCore data to disk. */
public final class SaveallCommand extends ForgeCommand {
    public SaveallCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "saveall";
    }

    @Override
    public String description() {
        return "Save all worlds and plugin data.";
    }

    @Override
    public String usage() {
        return "/saveall";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        int worlds = 0;
        for (World world : Bukkit.getWorlds()) {
            world.save();
            worlds++;
        }
        plugin.users().saveAll();
        plugin.warps().save();
        plugin.kits().save();
        plugin.economy().save();
        plugin.jails().save();
        plugin.bans().save();
        plugin.saveConfig();
        Text.ok(sender, "Saved <white>" + worlds + "</white> world(s) and all ForgeCore data.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
