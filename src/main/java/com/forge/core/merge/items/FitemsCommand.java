package com.forge.core.merge.items;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * {@code /fitems} — the merged forge-items root command.
 * Subcommands: reload | give <id> [player] [amount] | list | menu |
 * edit <id> | create <id> | delete <id>.
 */
public final class FitemsCommand extends ForgeCommand {
    public FitemsCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "fitems";
    }

    @Override
    public List<String> aliases() {
        return List.of("forgeitems");
    }

    @Override
    public String description() {
        return "Custom items: give, browse and edit.";
    }

    @Override
    public String usage() {
        return "/fitems <reload|give|list|menu|edit|create|delete>";
    }

    private ItemsModule items() {
        return ItemsSetup.module();
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        ItemsModule items = items();
        if (args.length == 0) {
            throw new CommandRegistry.CommandFailure("Unknown subcommand.");
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> reload(sender, items);
            case "list" -> list(sender, items);
            case "give" -> give(sender, items, args);
            case "menu" -> menu(sender, items);
            case "edit" -> edit(sender, items, args);
            case "create" -> create(sender, items, args);
            case "delete" -> delete(sender, items, args);
            default -> throw new CommandRegistry.CommandFailure("Unknown subcommand.");
        }
    }

    private void checkPerm(CommandSender sender, String node) {
        if (!sender.hasPermission(node)) {
            throw new CommandRegistry.CommandFailure("You don't have permission to do that.");
        }
    }

    private Player requirePlayer(CommandSender sender) {
        Player player = asPlayer(sender);
        if (player == null) {
            throw new CommandRegistry.CommandFailure("Only players can use that.");
        }
        return player;
    }

    private void reload(CommandSender sender, ItemsModule items) {
        checkPerm(sender, "forgecore.fitems.admin");
        int count = items.reloadItems();
        sender.sendMessage(items.prefixed("messages.reloaded", "count", String.valueOf(count)));
    }

    private void list(CommandSender sender, ItemsModule items) {
        checkPerm(sender, "forgecore.fitems.list");
        var ids = new ArrayList<>(items.registry().ids());
        ids.sort(String::compareTo);
        sender.sendMessage(items.prefixed("messages.item-list-header", "count", String.valueOf(ids.size())));
        for (String id : ids) {
            CustomItem item = items.registry().get(id);
            sender.sendMessage(items.prefixed("messages.item-list-entry",
                    "id", id, "name", items.plainName(item)));
        }
    }

    private void give(CommandSender sender, ItemsModule items, String[] args) {
        checkPerm(sender, "forgecore.fitems.give");
        if (args.length < 2) {
            throw new CommandRegistry.CommandFailure(
                    "Missing arguments.", "/fitems give <id> [player] [amount]");
        }
        CustomItem item = items.registry().get(args[1]);
        if (item == null) {
            sender.sendMessage(items.prefixed("messages.unknown-item", "id", args[1]));
            return;
        }
        Player target;
        int amount = 1;
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                try {
                    amount = Integer.parseInt(args[2]);
                    target = requirePlayer(sender);
                } catch (NumberFormatException e) {
                    throw new CommandRegistry.CommandFailure(
                            "Player not found: " + args[2]);
                }
            } else if (args.length >= 4) {
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    throw new CommandRegistry.CommandFailure(
                            "Invalid amount: " + args[3]);
                }
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            throw new CommandRegistry.CommandFailure(
                    "Specify a player when running from console.");
        }
        amount = Math.max(1, Math.min(64, amount));
        ItemStack stack = items.registry().build(item, amount);
        var leftover = target.getInventory().addItem(stack);
        for (ItemStack rest : leftover.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), rest);
        }
        String name = items.plainName(item);
        sender.sendMessage(items.prefixed("messages.item-given",
                "amount", String.valueOf(amount), "name", name, "player", target.getName()));
        if (!sender.equals(target)) {
            target.sendMessage(items.prefixed("messages.item-received",
                    "amount", String.valueOf(amount), "name", name));
        }
    }

    private void menu(CommandSender sender, ItemsModule items) {
        Player player = requirePlayer(sender);
        checkPerm(sender, "forgecore.fitems.menu");
        items.browser().open(player, 0);
    }

    private void edit(CommandSender sender, ItemsModule items, String[] args) {
        Player player = requirePlayer(sender);
        checkPerm(sender, "forgecore.fitems.edit");
        if (args.length < 2) {
            throw new CommandRegistry.CommandFailure(
                    "Missing arguments.", "/fitems edit <id>");
        }
        CustomItem item = items.registry().get(args[1]);
        if (item == null) {
            sender.sendMessage(items.prefixed("messages.unknown-item", "id", args[1]));
            return;
        }
        items.editor().openItem(player, item.id());
    }

    private void create(CommandSender sender, ItemsModule items, String[] args) {
        Player player = requirePlayer(sender);
        checkPerm(sender, "forgecore.fitems.edit");
        if (args.length < 2 || !args[1].matches("[a-z0-9_]+")) {
            throw new CommandRegistry.CommandFailure(
                    "Missing arguments.", "/fitems create <id>");
        }
        String id = args[1].toLowerCase(Locale.ROOT);
        if (items.registry().get(id) != null) {
            sender.sendMessage(items.prefixedOr("messages.editor-id-taken",
                    "<red>An item with id <white><id> <red>already exists.", "id", id));
            return;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType().isAir()) {
            sender.sendMessage(items.prefixedOr("messages.editor-empty-hand",
                    "<red>Hold an item in your main hand first."));
            return;
        }
        if (!items.registry().createFromHeld(id, held)) {
            sender.sendMessage(items.prefixedOr("messages.editor-create-failed",
                    "<red>Could not create item <white><id><red>.", "id", id));
            return;
        }
        items.reloadItems();
        items.editor().openItem(player, id);
    }

    private void delete(CommandSender sender, ItemsModule items, String[] args) {
        checkPerm(sender, "forgecore.fitems.edit");
        if (args.length < 2) {
            throw new CommandRegistry.CommandFailure(
                    "Missing arguments.", "/fitems delete <id>");
        }
        CustomItem item = items.registry().get(args[1]);
        if (item == null) {
            sender.sendMessage(items.prefixed("messages.unknown-item", "id", args[1]));
            return;
        }
        if (items.registry().deleteItem(item.id())) {
            items.reloadItems();
            sender.sendMessage(items.prefixedOr("messages.editor-deleted",
                    "<red>Deleted item <white><id><red>.", "id", item.id()));
        } else {
            sender.sendMessage(items.prefixedOr("messages.editor-delete-failed",
                    "<red>Could not delete <white><id><red>.", "id", item.id()));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        ItemsModule items = ItemsSetup.module();
        if (items == null) {
            return List.of();
        }
        if (args.length == 1) {
            return filter(List.of("reload", "give", "list", "menu", "edit", "create", "delete"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return filter(new ArrayList<>(items.registry().ids()), args[1]);
        }
        if (args.length == 2
                && (args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("delete"))) {
            return filter(new ArrayList<>(items.registry().ids()), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            return filter(names, args[2]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String low = prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(low)) {
                out.add(option);
            }
        }
        return out;
    }
}
