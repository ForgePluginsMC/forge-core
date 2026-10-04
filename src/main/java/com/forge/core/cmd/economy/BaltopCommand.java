package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/** Richest players, 10 per page. */
public final class BaltopCommand extends ForgeCommand {
    private static final int PER_PAGE = 10;

    public BaltopCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "baltop";
    }

    @Override
    public String description() {
        return "Show the richest players.";
    }

    @Override
    public String usage() {
        return "/baltop [page]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                throw new CommandRegistry.CommandFailure("Page must be a number.");
            }
            if (page < 1) {
                throw new CommandRegistry.CommandFailure("Page must be a number.");
            }
        }
        List<Map.Entry<UUID, Double>> top = plugin.economy().top(page * PER_PAGE);
        int pages = Math.max(1, (int) Math.ceil(top.size() / (double) PER_PAGE));
        if (page > pages) {
            throw new CommandRegistry.CommandFailure("There are only " + pages + " page(s).");
        }
        Text.send(sender, "<gold>--- Balance Top <gray>(page " + page + "/" + pages + ")<gold> ---");
        int rank = (page - 1) * PER_PAGE;
        for (Map.Entry<UUID, Double> entry : top.subList((page - 1) * PER_PAGE, top.size())) {
            rank++;
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            if (name == null) {
                name = "Unknown";
            }
            Text.send(sender, "<yellow>" + rank + ". <white>" + Text.escape(name)
                    + "</white> — <green>" + plugin.economy().format(entry.getValue()) + "</green>");
        }
        if (top.isEmpty()) {
            Text.send(sender, "<gray>Nobody has any money yet.");
        }
    }
}
