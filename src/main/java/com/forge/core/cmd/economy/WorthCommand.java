package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Show the sell price of the held item. */
public final class WorthCommand extends ForgeCommand {
    public WorthCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "worth";
    }

    @Override
    public String description() {
        return "Show the sell price of the held item.";
    }

    @Override
    public String usage() {
        return "/worth";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType().isAir()) {
            throw new CommandRegistry.CommandFailure("Hold an item first.");
        }
        double unit = plugin.economy().worthOf(held.getType());
        if (unit <= 0) {
            throw new CommandRegistry.CommandFailure("That item isn't sellable.");
        }
        double stack = unit * held.getAmount();
        String pretty = held.getType().name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        Text.send(sender, "<white>" + Text.escape(pretty) + "</white>: <green>"
                + plugin.economy().format(unit) + "</green> each, <green>"
                + plugin.economy().format(stack) + "</green> for the stack.");
    }
}
