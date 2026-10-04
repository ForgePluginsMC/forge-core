package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** Show a balance. */
public final class BalanceCommand extends ForgeCommand {
    public BalanceCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "balance";
    }

    @Override
    public List<String> aliases() {
        return List.of("bal");
    }

    @Override
    public String description() {
        return "Show a player's balance.";
    }

    @Override
    public String usage() {
        return "/balance [player]";
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
        if (!sender.hasPermission("forgecore.balance.others")) {
            throw new com.forge.core.command.CommandRegistry.CommandFailure(
                    "You don't have permission to check other players' balances.");
        }
        @Nullable OfflinePlayer target = Players.offline(args[0]);
        if (target == null) {
            throw new com.forge.core.command.CommandRegistry.CommandFailure(
                    "Player <white>" + Text.escape(args[0]) + "</white> has never played here.");
        }
        String targetName = target.getName() == null ? args[0] : target.getName();
        Text.send(sender, "<white>" + Text.escape(targetName) + "</white>'s balance: <green>"
                + plugin.economy().format(plugin.economy().get(target.getUniqueId())) + "</green>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("forgecore.balance.others")) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
