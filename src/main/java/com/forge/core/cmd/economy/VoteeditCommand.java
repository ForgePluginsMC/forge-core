package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.data.UserData;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.Nullable;

/** Adjust a player's vote count. */
public final class VoteeditCommand extends ForgeCommand {
    public VoteeditCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "voteedit";
    }

    @Override
    public String description() {
        return "Add, set or take votes from a player.";
    }

    @Override
    public String usage() {
        return "/voteedit <add|set|take> <player> <amount>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length != 3) {
            Text.usage(sender, usage());
            return;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        if (!action.equals("add") && !action.equals("set") && !action.equals("take")) {
            Text.usage(sender, usage());
            return;
        }
        @Nullable OfflinePlayer target = Players.offline(args[1]);
        if (target == null) {
            throw new CommandRegistry.CommandFailure(
                    "Player <white>" + Text.escape(args[1]) + "</white> has never played here.");
        }
        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException ignored) {
            throw new CommandRegistry.CommandFailure(
                    "<white>" + Text.escape(args[2]) + "</white> is not a valid amount.");
        }
        if (amount < 0) {
            throw new CommandRegistry.CommandFailure("Amount can't be negative.");
        }
        UserData data = plugin.users().get(target.getUniqueId());
        long current = data.getLong("votes", 0);
        long result = switch (action) {
            case "add" -> current + amount;
            case "take" -> Math.max(0, current - amount);
            default -> amount;
        };
        data.setLong("votes", result);
        plugin.users().save(target.getUniqueId());
        String targetName = target.getName() == null ? args[1] : target.getName();
        Text.ok(sender, "Set <white>" + Text.escape(targetName) + "</white>'s votes to <white>" + result + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("add", "set", "take"), args);
        }
        if (args.length == 2) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
