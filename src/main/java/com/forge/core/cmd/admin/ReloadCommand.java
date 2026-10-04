package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 * /reload — reload ForgeCore's config.yml. Data files (userdata, warps,
 * kits, economy, bans, jails) are loaded at startup and saved continuously;
 * they intentionally do not reload at runtime, so this command is honest
 * about what it does.
 */
public final class ReloadCommand extends ForgeCommand {
    public ReloadCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String description() {
        return "Reload ForgeCore's config.yml.";
    }

    @Override
    public String usage() {
        return "/reload";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        plugin.reloadConfig();
        Text.ok(sender, "config.yml reloaded.");
        Text.send(sender, "<gray>Note: data files (userdata, warps, kits, economy, …) "
                + "load at startup and save continuously — they reload on restart.</gray>");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
