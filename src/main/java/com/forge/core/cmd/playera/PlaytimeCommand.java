package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.data.UserData;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Show total playtime (persisted plus the current session). */
public final class PlaytimeCommand extends PlayerACommand {
    public PlaytimeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "playtime";
    }

    @Override
    public String description() {
        return "Show total playtime.";
    }

    @Override
    public String usage() {
        return "/playtime [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target = target(sender, args);
        if (target == null) {
            return;
        }
        UserData data = plugin.users().get(target);
        long total = data.playtimeSeconds();
        long start = data.getLong("session-start", 0);
        if (start > 0) {
            total += (System.currentTimeMillis() - start) / 1000;
        }
        Text.send(sender, "<gray>Total playtime for <white>" + Text.escape(target.getName())
                + "</white>: <green>" + Time.format(total) + "</green>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
