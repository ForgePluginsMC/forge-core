package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/** /version — ForgeCore version plus the Bukkit/Paper platform version. */
public final class VersionCommand extends ForgeCommand {
    public VersionCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "version";
    }

    @Override
    public String description() {
        return "Show the ForgeCore and server platform versions.";
    }

    @Override
    public String usage() {
        return "/version";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Text.send(sender, "<gold>ForgeCore</gold> <white>"
                + Text.escape(plugin.getPluginMeta().getVersion()) + "</white>");
        Text.send(sender, "<gray>Platform:</gray> <white>" + Text.escape(Bukkit.getVersion()) + "</white>");
        Text.send(sender, "<gray>Bukkit API:</gray> <white>" + Text.escape(Bukkit.getBukkitVersion()) + "</white>");
    }
}
