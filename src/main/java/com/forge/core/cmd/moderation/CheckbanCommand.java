package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.punish.BanManager;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

/** Show the details of a player's active ban. */
public final class CheckbanCommand extends ForgeCommand {
    public CheckbanCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "checkban";
    }

    @Override
    public String description() {
        return "Show a player's ban details.";
    }

    @Override
    public String usage() {
        return "/checkban <player>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        BanManager.BanInfo info = plugin.bans().findByName(args[0]);
        if (info == null) {
            throw new CommandRegistry.CommandFailure("<white>" + Text.escape(args[0]) + "</white> is not banned.");
        }
        String expires = info.permanent()
                ? "<red>Permanent"
                : "<white>" + Time.formatDate(info.until()) + " <gray>(" + Time.format((info.until() - System.currentTimeMillis()) / 1000) + " left)";
        Text.send(sender, "<gold><bold>Ban:</bold></gold> <white>" + Text.escape(info.name()));
        Text.send(sender, "<gray>Reason: <white>" + Text.escape(info.reason()));
        Text.send(sender, "<gray>By: <white>" + Text.escape(info.by()) + " <gray>on <white>" + Time.formatDate(info.createdAt()));
        Text.send(sender, "<gray>Expires: " + expires);
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
