package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.List;
import org.bukkit.command.CommandSender;

/** List all active player and IP bans. */
public final class BanlistCommand extends ForgeCommand {
    public BanlistCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "banlist";
    }

    @Override
    public List<String> aliases() {
        return List.of("bans");
    }

    @Override
    public String description() {
        return "List all active bans.";
    }

    @Override
    public String usage() {
        return "/banlist";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        var bans = plugin.bans().all();
        var ipBans = ModerationState.ipBans().all();
        if (bans.isEmpty() && ipBans.isEmpty()) {
            Text.send(sender, "No active bans.");
            return;
        }
        Text.send(sender, "<white>Active bans (" + (bans.size() + ipBans.size()) + "):</white>");
        for (var ban : bans) {
            String expiry = ban.permanent() ? "permanent"
                    : "expires in " + Time.format((ban.until() - System.currentTimeMillis()) / 1000);
            Text.send(sender, "<gray>Player <white>" + Text.escape(ban.name()) + "</white>"
                    + " — " + Text.escape(ban.reason()) + " <dark_gray>(" + expiry + ")</dark_gray></gray>");
        }
        for (var ban : ipBans.values()) {
            String expiry = ban.permanent() ? "permanent"
                    : "expires in " + Time.format((ban.until() - System.currentTimeMillis()) / 1000);
            Text.send(sender, "<gray>IP <white>" + Text.escape(ban.ip()) + "</white>"
                    + " — " + Text.escape(ban.reason()) + " <dark_gray>(" + expiry + ")</dark_gray></gray>");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
