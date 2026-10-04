package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsb.flight.FlightChargeManager;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Manage flight charges: add/take/set/show, convert exp levels or money
 * into charges, or refill to the configured maximum (for money).
 */
public final class FlightchargeCommand extends ForgeCommand {
    private static final List<String> SUBS =
            List.of("add", "take", "set", "show", "expcharge", "moneycharge", "recharge");

    public FlightchargeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "flightcharge";
    }

    @Override
    public String description() {
        return "Manage flight charges.";
    }

    @Override
    public String usage() {
        return "/flightcharge <add|take|set|show|expcharge|moneycharge|recharge> <player> [amount]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        Player target = Players.find(sender, args[1]);
        if (target == null) {
            return;
        }
        if (sender instanceof Player self
                && !self.getUniqueId().equals(target.getUniqueId())
                && !sender.hasPermission("forgecore.flightcharge.others")) {
            throw new CommandRegistry.CommandFailure("You don't have permission to manage other players' charges.");
        }
        FlightChargeManager flight = SystemsBSetup.flight();
        UUID uuid = target.getUniqueId();
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "add", "take", "set" -> {
                if (args.length < 3) {
                    Text.usage(sender, usage());
                    return;
                }
                double amount = parseAmount(args[2]);
                switch (sub) {
                    case "add" -> flight.add(uuid, amount);
                    case "take" -> flight.add(uuid, -amount);
                    case "set" -> flight.set(uuid, amount);
                    default -> {
                    }
                }
                Text.ok(sender, Text.escape(target.getName()) + " now has <white>"
                        + flight.display(uuid) + "</white> flight charges.");
            }
            case "show" -> Text.send(sender, Text.escape(target.getName()) + " has <white>"
                    + flight.display(uuid) + "</white> flight charges.");
            case "expcharge" -> {
                int levels = target.getLevel();
                if (levels <= 0) {
                    throw new CommandRegistry.CommandFailure(
                            Text.escape(target.getName()) + " has no experience levels.");
                }
                double rate = plugin.getConfig().getDouble("flightcharge.exp-rate", 10.0);
                double charges = levels * rate;
                target.setLevel(0);
                flight.add(uuid, charges);
                Text.ok(sender, "Converted " + levels + " levels into <white>"
                        + String.format(Locale.ROOT, "%,.0f", charges) + "</white> flight charges.");
            }
            case "moneycharge" -> {
                if (args.length < 3) {
                    Text.usage(sender, usage());
                    return;
                }
                double amount = parseAmount(args[2]);
                if (!plugin.economy().take(uuid, amount)) {
                    throw new CommandRegistry.CommandFailure(
                            Text.escape(target.getName()) + " cannot afford "
                                    + plugin.economy().format(amount) + ".");
                }
                double rate = plugin.getConfig().getDouble("flightcharge.money-rate", 1.0);
                flight.add(uuid, amount * rate);
                Text.ok(sender, "Bought <white>"
                        + String.format(Locale.ROOT, "%,.0f", amount * rate)
                        + "</white> flight charges for " + plugin.economy().format(amount) + ".");
            }
            case "recharge" -> {
                double missing = flight.max() - flight.get(uuid);
                if (missing <= 0.0) {
                    throw new CommandRegistry.CommandFailure(
                            Text.escape(target.getName()) + "'s charges are already full.");
                }
                double costPer = plugin.getConfig().getDouble("flightcharge.recharge-cost", 0.1);
                double cost = missing * costPer;
                if (!plugin.economy().take(uuid, cost)) {
                    throw new CommandRegistry.CommandFailure(
                            Text.escape(target.getName()) + " cannot afford the "
                                    + plugin.economy().format(cost) + " recharge.");
                }
                flight.set(uuid, flight.max());
                Text.ok(sender, "Recharged to full (<white>" + flight.display(uuid)
                        + "</white>) for " + plugin.economy().format(cost) + ".");
            }
            default -> Text.usage(sender, usage());
        }
    }

    private double parseAmount(String raw) {
        double value;
        try {
            value = Double.parseDouble(raw);
        } catch (NumberFormatException exception) {
            throw new CommandRegistry.CommandFailure("Not a number: " + Text.escape(raw) + ".");
        }
        if (value < 0) {
            throw new CommandRegistry.CommandFailure("Amount must not be negative.");
        }
        return value;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(SUBS, args);
        }
        if (args.length == 2) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
