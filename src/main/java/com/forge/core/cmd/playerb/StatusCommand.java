package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.lang.management.ManagementFactory;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/** /status — TPS, memory usage, uptime and online count. */
public final class StatusCommand extends ForgeCommand {
    public StatusCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "status";
    }

    @Override
    public String description() {
        return "Show server status: TPS, memory, uptime, players.";
    }

    @Override
    public String usage() {
        return "/status";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        double[] tps = Bukkit.getTPS();
        Runtime runtime = Runtime.getRuntime();
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long maxMb = runtime.maxMemory() / 1024 / 1024;
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        Text.send(sender, "<white>Server status:</white>");
        Text.send(sender, "<gray>TPS (1m/5m/15m):</gray> " + colored(tps[0]) + "<gray> / </gray>"
                + colored(tps[1]) + "<gray> / </gray>" + colored(tps[2]));
        Text.send(sender, "<gray>Memory:</gray> <white>" + usedMb + " MB</white><gray> / </gray><white>"
                + maxMb + " MB</white>");
        Text.send(sender, "<gray>Uptime:</gray> <white>" + Time.format(uptimeSeconds) + "</white>");
        Text.send(sender, "<gray>Players:</gray> <white>" + Bukkit.getOnlinePlayers().size()
                + "</white><gray>/</gray><white>" + Bukkit.getMaxPlayers() + "</white>");
    }

    private static String colored(double tps) {
        String color = tps >= 19.0 ? "green" : tps >= 15.0 ? "yellow" : "red";
        return "<" + color + ">" + String.format(Locale.ROOT, "%.2f", tps) + "</" + color + ">";
    }
}
