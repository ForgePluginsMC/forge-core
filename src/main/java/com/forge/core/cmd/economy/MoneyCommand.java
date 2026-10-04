package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** Balance overview plus pay/give/take/set. */
public final class MoneyCommand extends ForgeCommand {
    public MoneyCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "money";
    }

    @Override
    public String description() {
        return "Check your balance, pay players, or manage balances (admin).";
    }

    @Override
    public String usage() {
        return "/money [pay <player> <amount> | <give|take|set> <player> <amount>]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Player player = asPlayer(sender);
            if (player == null) {
                Text.usage(sender, usage());
                return;
            }
            Text.send(sender, "Your balance: <green>"
                    + plugin.economy().format(plugin.economy().get(player.getUniqueId())) + "</green>.");
            return;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "pay" -> pay(sender, args);
            case "give", "take", "set" -> admin(sender, sub, args);
            default -> Text.usage(sender, usage());
        }
    }

    private void pay(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            throw new CommandRegistry.CommandFailure("Only players can pay each other.");
        }
        if (args.length != 3) {
            Text.usage(sender, "/money pay <player> <amount>");
            return;
        }
        Player target = Players.find(sender, args[1]);
        if (target == null) {
            return;
        }
        if (com.forge.core.cmd.playerb.PaytoggleCommand.blocked(plugin, target)
                && !sender.hasPermission("forgecore.paytoggle.bypass")) {
            throw new CommandRegistry.CommandFailure("That player is not accepting payments.");
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            throw new CommandRegistry.CommandFailure("You can't pay yourself.");
        }
        double amount = parseAmount(args[2]);
        if (!plugin.economy().take(player.getUniqueId(), amount)) {
            throw new CommandRegistry.CommandFailure("You don't have "
                    + plugin.economy().format(amount) + ".");
        }
        plugin.economy().add(target.getUniqueId(), amount);
        Text.ok(player, "Paid <white>" + Text.escape(target.getName()) + "</white> <green>"
                + plugin.economy().format(amount) + "</green>.");
        Text.send(target, "<white>" + Text.escape(player.getName()) + "</white> paid you <green>"
                + plugin.economy().format(amount) + "</green>.");
    }

    private void admin(CommandSender sender, String sub, String[] args) {
        if (!sender.hasPermission("forgecore.money.admin")) {
            throw new CommandRegistry.CommandFailure("You don't have permission to do that.");
        }
        if (args.length != 3) {
            Text.usage(sender, "/money " + sub + " <player> <amount>");
            return;
        }
        @Nullable OfflinePlayer target = Players.offline(args[1]);
        if (target == null) {
            throw new CommandRegistry.CommandFailure(
                    "Player <white>" + Text.escape(args[1]) + "</white> has never played here.");
        }
        double amount = parseAmount(args[2]);
        String targetName = target.getName() == null ? args[1] : target.getName();
        switch (sub) {
            case "give" -> {
                plugin.economy().add(target.getUniqueId(), amount);
                Text.ok(sender, "Gave <white>" + Text.escape(targetName) + "</white> <green>"
                        + plugin.economy().format(amount) + "</green>.");
            }
            case "take" -> {
                if (!plugin.economy().take(target.getUniqueId(), amount)) {
                    throw new CommandRegistry.CommandFailure("<white>" + Text.escape(targetName)
                            + "</white> doesn't have " + plugin.economy().format(amount) + ".");
                }
                Text.ok(sender, "Took <green>" + plugin.economy().format(amount)
                        + "</green> from <white>" + Text.escape(targetName) + "</white>.");
            }
            case "set" -> {
                plugin.economy().set(target.getUniqueId(), amount);
                Text.ok(sender, "Set <white>" + Text.escape(targetName) + "</white>'s balance to <green>"
                        + plugin.economy().format(amount) + "</green>.");
            }
            default -> Text.usage(sender, usage());
        }
    }

    private double parseAmount(String raw) {
        double amount;
        try {
            amount = Double.parseDouble(raw);
        } catch (NumberFormatException ignored) {
            throw new CommandRegistry.CommandFailure(
                    "<white>" + Text.escape(raw) + "</white> is not a valid amount.");
        }
        if (amount <= 0 || !Double.isFinite(amount)) {
            throw new CommandRegistry.CommandFailure("Amount must be positive.");
        }
        return amount;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("pay", "give", "take", "set"), args);
        }
        if (args.length == 2) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
