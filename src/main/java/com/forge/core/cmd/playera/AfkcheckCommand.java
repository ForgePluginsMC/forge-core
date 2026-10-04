package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Query a player's AFK status and how long they've been AFK. */
public final class AfkcheckCommand extends ForgeCommand {
    public AfkcheckCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "afkcheck";
    }

    @Override
    public String description() {
        return "Check a player's AFK status.";
    }

    @Override
    public String usage() {
        return "/afkcheck <player>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        boolean afk = plugin.afk().isAfk(target);
        if (!afk) {
            Text.ok(sender, "<white>" + Text.escape(target.getName()) + "</white> is <green>not AFK</green>.");
            return;
        }
        long since = plugin.afk().afkSinceMillis(target);
        String howLong = since < 0 ? "unknown time"
                : Time.format((System.currentTimeMillis() - since) / 1000L);
        Text.ok(sender, "<white>" + Text.escape(target.getName()) + "</white> is <red>AFK</red> for " + howLong + ".");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
