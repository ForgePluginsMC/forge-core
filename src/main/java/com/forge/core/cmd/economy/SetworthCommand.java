package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Set the sell price of the held item's material. */
public final class SetworthCommand extends ForgeCommand {
    public SetworthCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "setworth";
    }

    @Override
    public String description() {
        return "Set the sell price of the held item's material.";
    }

    @Override
    public String usage() {
        return "/setworth <price>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length != 1) {
            Text.usage(sender, usage());
            return;
        }
        double price;
        try {
            price = Double.parseDouble(args[0]);
        } catch (NumberFormatException ignored) {
            throw new CommandRegistry.CommandFailure(
                    "<white>" + Text.escape(args[0]) + "</white> is not a valid price.");
        }
        if (price < 0 || !Double.isFinite(price)) {
            throw new CommandRegistry.CommandFailure("Price can't be negative.");
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType().isAir()) {
            throw new CommandRegistry.CommandFailure("Hold an item first.");
        }
        plugin.economy().setWorth(held.getType(), price);
        String pretty = held.getType().name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        Text.ok(player, "Set <white>" + Text.escape(pretty) + "</white> worth to <green>"
                + plugin.economy().format(price) + "</green>.");
    }
}
