package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Sell the held stack, or every sellable item in the inventory. */
public final class SellCommand extends ForgeCommand {
    public SellCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "sell";
    }

    @Override
    public String description() {
        return "Sell the held item stack, or all sellable items.";
    }

    @Override
    public String usage() {
        return "/sell [hand|all]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        String mode = args.length == 0 ? "hand" : args[0].toLowerCase(Locale.ROOT);
        switch (mode) {
            case "hand" -> sellHand(player);
            case "all" -> sellAll(player);
            default -> Text.usage(sender, usage());
        }
    }

    private void sellHand(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        double value = plugin.economy().worthOf(held);
        if (value <= 0) {
            throw new CommandRegistry.CommandFailure("That item isn't sellable.");
        }
        player.getInventory().setItemInMainHand(null);
        plugin.economy().add(player.getUniqueId(), value);
        Text.ok(player, "Sold for <green>" + plugin.economy().format(value) + "</green>.");
    }

    private void sellAll(Player player) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        double total = 0;
        int stacks = 0;
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            double value = plugin.economy().worthOf(item);
            if (value > 0) {
                total += value;
                stacks++;
                contents[slot] = null;
            }
        }
        if (stacks == 0) {
            throw new CommandRegistry.CommandFailure("Nothing sellable in your inventory.");
        }
        player.getInventory().setStorageContents(contents);
        plugin.economy().add(player.getUniqueId(), total);
        Text.ok(player, "Sold <white>" + stacks + "</white> stack(s) for <green>"
                + plugin.economy().format(total) + "</green>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return com.forge.core.util.Players.filter(List.of("hand", "all"), args);
        }
        return List.of();
    }
}
