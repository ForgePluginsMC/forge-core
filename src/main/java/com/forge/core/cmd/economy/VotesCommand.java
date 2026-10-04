package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** Show a player's vote count. */
public final class VotesCommand extends ForgeCommand {
    public VotesCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "votes";
    }

    @Override
    public String description() {
        return "Show a player's vote count.";
    }

    @Override
    public String usage() {
        return "/votes [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Player player = asPlayer(sender);
            if (player == null) {
                Text.usage(sender, usage());
                return;
            }
            long votes = plugin.users().get(player).getLong("votes", 0);
            Text.send(sender, "Your votes: <white>" + votes + "</white>.");
            return;
        }
        @Nullable OfflinePlayer target = Players.offline(args[0]);
        if (target == null) {
            throw new CommandRegistry.CommandFailure(
                    "Player <white>" + Text.escape(args[0]) + "</white> has never played here.");
        }
        long votes = plugin.users().get(target.getUniqueId()).getLong("votes", 0);
        String targetName = target.getName() == null ? args[0] : target.getName();
        Text.send(sender, "<white>" + Text.escape(targetName) + "</white>'s votes: <white>" + votes + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
