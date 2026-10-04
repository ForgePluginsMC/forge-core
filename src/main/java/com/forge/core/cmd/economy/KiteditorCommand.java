package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.data.KitManager;
import com.forge.core.data.KitManager.Kit;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/** Create, update, delete or list kits from your inventory. */
public final class KiteditorCommand extends ForgeCommand {
    public KiteditorCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "kiteditor";
    }

    @Override
    public String description() {
        return "Create or edit kits from your inventory.";
    }

    @Override
    public String usage() {
        return "/kiteditor <create|delete|update|list> <name> [cooldown] [cost]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "list" -> list(player);
            case "create" -> create(player, args, false);
            case "update" -> create(player, args, true);
            case "delete" -> delete(player, args);
            default -> Text.usage(sender, usage());
        }
    }

    private void list(Player player) {
        List<String> names = plugin.kits().names();
        if (names.isEmpty()) {
            Text.send(player, "<gray>No kits yet.");
            return;
        }
        StringBuilder out = new StringBuilder("<gold>Kits: <white>");
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                out.append("<gray>, <white>");
            }
            out.append(Text.escape(names.get(i)));
        }
        Text.send(player, out.toString());
    }

    private void create(Player player, String[] args, boolean update) {
        if (args.length < 2) {
            Text.usage(player, usage());
            return;
        }
        String name = args[1].toLowerCase(Locale.ROOT);
        if (!name.matches("[a-z0-9_\\-]+")) {
            throw new CommandRegistry.CommandFailure("Kit names may only use a-z, 0-9, _ and -.");
        }
        @Nullable Kit existing = plugin.kits().get(name);
        if (update && existing == null) {
            throw new CommandRegistry.CommandFailure(
                    "No kit named <white>" + Text.escape(name) + "</white>.");
        }
        long cooldown = existing == null ? 0 : existing.cooldownSeconds();
        double cost = existing == null ? 0 : existing.cost();
        if (args.length > 2) {
            try {
                cooldown = Time.parseSeconds(args[2]);
            } catch (IllegalArgumentException exception) {
                throw new CommandRegistry.CommandFailure(
                        "Bad cooldown <white>" + Text.escape(args[2]) + "</white> — try 10m, 2h, 1d.");
            }
        }
        if (args.length > 3) {
            try {
                cost = Double.parseDouble(args[3]);
            } catch (NumberFormatException ignored) {
                throw new CommandRegistry.CommandFailure(
                        "<white>" + Text.escape(args[3]) + "</white> is not a valid cost.");
            }
            if (cost < 0 || !Double.isFinite(cost)) {
                throw new CommandRegistry.CommandFailure("Cost can't be negative.");
            }
        }
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item != null && !item.getType().isAir()) {
                items.add(item.clone());
            }
        }
        if (items.isEmpty()) {
            throw new CommandRegistry.CommandFailure("Your inventory is empty — nothing to save.");
        }
        plugin.kits().create(name, items, cooldown, cost);
        Text.ok(player, (update ? "Updated" : "Created") + " kit <white>" + Text.escape(name)
                + "</white> with <white>" + items.size() + "</white> item(s).");
    }

    private void delete(Player player, String[] args) {
        if (args.length < 2) {
            Text.usage(player, usage());
            return;
        }
        if (!plugin.kits().delete(args[1])) {
            throw new CommandRegistry.CommandFailure(
                    "No kit named <white>" + Text.escape(args[1]) + "</white>.");
        }
        Text.ok(player, "Deleted kit <white>" + Text.escape(args[1].toLowerCase(Locale.ROOT)) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("create", "delete", "update", "list"), args);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("update"))) {
            return Players.filter(plugin.kits().names(), args);
        }
        return List.of();
    }
}
