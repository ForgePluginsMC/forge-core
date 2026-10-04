package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Withdraw money into a redeemable paper cheque. */
public final class ChequeCommand extends ForgeCommand {
    public ChequeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "cheque";
    }

    @Override
    public String description() {
        return "Withdraw money into a paper cheque (right-click to redeem).";
    }

    @Override
    public String usage() {
        return "/cheque <amount>";
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
        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException ignored) {
            throw new CommandRegistry.CommandFailure(
                    "<white>" + Text.escape(args[0]) + "</white> is not a valid amount.");
        }
        if (amount <= 0 || !Double.isFinite(amount)) {
            throw new CommandRegistry.CommandFailure("Amount must be positive.");
        }
        if (!plugin.economy().take(player.getUniqueId(), amount)) {
            throw new CommandRegistry.CommandFailure("You don't have "
                    + plugin.economy().format(amount) + ".");
        }
        ItemStack cheque = ChequeListener.create(plugin, amount);
        Map<Integer, ItemStack> overflow = new HashMap<>(player.getInventory().addItem(cheque));
        for (ItemStack item : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
        Text.ok(player, "Withdrew <green>" + plugin.economy().format(amount)
                + "</green> as a cheque. Right-click it to redeem.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
