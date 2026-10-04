package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Save held item stacks server-side for later retrieval.
 * Subcommands: save, get, list, delete.
 */
public final class SaveditemsCommand extends ForgeCommand {
    public SaveditemsCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "saveditems";
    }

    @Override
    public List<String> aliases() {
        return List.of("si");
    }

    @Override
    public String description() {
        return "Save and retrieve named item stacks.";
    }

    @Override
    public String usage() {
        return "/saveditems <save|get|list|delete> [name]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "save" -> save(player, args);
            case "get" -> get(player, args);
            case "list" -> list(sender);
            case "delete", "del", "remove" -> delete(sender, args);
            default -> Text.usage(sender, usage());
        }
    }

    private void save(Player player, String[] args) {
        if (args.length < 2) {
            Text.usage(player, "/saveditems save <name>");
            return;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.isEmpty()) {
            Text.error(player, "Hold an item to save it.");
            return;
        }
        String name = args[1];
        plugin.savedItems().save(name, held);
        Text.ok(player, "Saved held item as <white>" + Text.escape(name) + "</white>.");
    }

    private void get(Player player, String[] args) {
        if (args.length < 2) {
            Text.usage(player, "/saveditems get <name>");
            return;
        }
        ItemStack item = plugin.savedItems().get(args[1]);
        if (item == null) {
            Text.error(player, "No saved item named <white>" + Text.escape(args[1]) + "</white>.");
            return;
        }
        if (player.getInventory().firstEmpty() == -1) {
            Text.error(player, "Your inventory is full.");
            return;
        }
        player.getInventory().addItem(item);
        Text.ok(player, "Retrieved <white>" + Text.escape(args[1]) + "</white>.");
    }

    private void list(CommandSender sender) {
        List<String> names = plugin.savedItems().names();
        if (names.isEmpty()) {
            Text.ok(sender, "No saved items.");
            return;
        }
        Text.send(sender, "<yellow>Saved items (" + names.size() + "):</yellow> "
                + String.join("<gray>, </gray>", names.stream().map(Text::escape).toList()));
    }

    private void delete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, "/saveditems delete <name>");
            return;
        }
        if (plugin.savedItems().delete(args[1])) {
            Text.ok(sender, "Deleted <white>" + Text.escape(args[1]) + "</white>.");
        } else {
            Text.error(sender, "No saved item named <white>" + Text.escape(args[1]) + "</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String last = args[0].toLowerCase(Locale.ROOT);
            return List.of("save", "get", "list", "delete").stream()
                    .filter(s -> s.startsWith(last)).toList();
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("delete"))) {
            String last = args[1].toLowerCase(Locale.ROOT);
            return plugin.savedItems().names().stream()
                    .filter(n -> n.startsWith(last)).toList();
        }
        return List.of();
    }
}
