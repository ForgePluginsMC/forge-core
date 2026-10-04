package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.punish.BanManager;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

/** Lift a ban by player name. */
public final class UnbanCommand extends ForgeCommand {
    public UnbanCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "unban";
    }

    @Override
    public String description() {
        return "Unban a player.";
    }

    @Override
    public String usage() {
        return "/unban <player>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        BanManager.BanInfo info = plugin.bans().findByName(args[0]);
        if (info == null) {
            throw new CommandRegistry.CommandFailure("No active ban for <white>" + Text.escape(args[0]) + "</white>.");
        }
        plugin.bans().unban(info.uuid());
        Text.ok(sender, "Unbanned <white>" + Text.escape(info.name()) + "</white>.");
        Staff.notify("<green><bold>Unban:</bold></green> <white>" + Text.escape(info.name())
                + "</white> was unbanned by <white>" + Text.escape(sender.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            List<String> names = new ArrayList<>();
            for (BanManager.BanInfo info : plugin.bans().all()) {
                names.add(info.name());
            }
            return Players.filter(names, args);
        }
        return List.of();
    }
}
