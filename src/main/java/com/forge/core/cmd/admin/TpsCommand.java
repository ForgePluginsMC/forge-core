package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/** /tps — show server ticks per second (1m, 5m, 15m). */
public final class TpsCommand extends ForgeCommand {
    public TpsCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tps";
    }

    @Override
    public String description() {
        return "Show server TPS.";
    }

    @Override
    public String usage() {
        return "/tps";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        double[] tps = Bukkit.getTPS();
        Text.send(sender, "TPS: <white>" + colored(tps[0]) + "</white>, <white>"
                + colored(tps[1]) + "</white>, <white>" + colored(tps[2]) + "</white>"
                + " <gray>(1m, 5m, 15m)");
    }

    private static String colored(double value) {
        double capped = Math.min(value, 20.0);
        String color = capped >= 18.0 ? "<green>" : capped >= 15.0 ? "<yellow>" : "<red>";
        return color + String.format(Locale.ROOT, "%.1f", capped) + "<white>";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
