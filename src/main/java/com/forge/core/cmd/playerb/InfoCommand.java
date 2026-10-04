package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.lang.management.ManagementFactory;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/** /info — server info: name, version, online/max players, uptime. */
public final class InfoCommand extends ForgeCommand {
    public InfoCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "info";
    }

    @Override
    public String description() {
        return "Show server information.";
    }

    @Override
    public String usage() {
        return "/info";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        Text.send(sender, "<white>Server info:</white>");
        Text.send(sender, "<gray>Name:</gray> <white>" + Text.escape(Bukkit.getName()) + "</white>");
        Text.send(sender, "<gray>Version:</gray> <white>" + Text.escape(Bukkit.getBukkitVersion()) + "</white>");
        Text.send(sender, "<gray>Players:</gray> <white>" + Bukkit.getOnlinePlayers().size()
                + "</white><gray>/</gray><white>" + Bukkit.getMaxPlayers() + "</white>");
        Text.send(sender, "<gray>Uptime:</gray> <white>" + Time.format(uptimeSeconds) + "</white>");
        Text.send(sender, "<gray>ForgeCore:</gray> <white>"
                + Text.escape(plugin.getPluginMeta().getVersion()) + "</white>");
    }
}
