package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;

/** Priced materials, 15 per page. */
public final class WorthlistCommand extends ForgeCommand {
    private static final int PER_PAGE = 15;

    public WorthlistCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "worthlist";
    }

    @Override
    public String description() {
        return "List items that have a sell price.";
    }

    @Override
    public String usage() {
        return "/worthlist [page]";
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
        List<Map.Entry<String, Double>> entries =
                new ArrayList<>(plugin.economy().worthTable().entrySet());
        entries.sort(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER));
        int pages = Math.max(1, (int) Math.ceil(entries.size() / (double) PER_PAGE));
        if (page > pages) {
            throw new CommandRegistry.CommandFailure("There are only " + pages + " page(s).");
        }
        Text.send(sender, "<gold>--- Worth List <gray>(page " + page + "/" + pages + ")<gold> ---");
        int from = (page - 1) * PER_PAGE;
        for (Map.Entry<String, Double> entry : entries.subList(from, Math.min(from + PER_PAGE, entries.size()))) {
            String pretty = entry.getKey().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
            Text.send(sender, "<white>" + Text.escape(pretty) + "</white>: <green>"
                    + plugin.economy().format(entry.getValue()) + "</green>");
        }
        if (entries.isEmpty()) {
            Text.send(sender, "<gray>No prices set. An admin can run /generateworth.");
        }
    }
}
