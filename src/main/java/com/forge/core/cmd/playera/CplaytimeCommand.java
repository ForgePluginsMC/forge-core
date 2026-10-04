package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Show this session's playtime. */
public final class CplaytimeCommand extends PlayerACommand {
    public CplaytimeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "cplaytime";
    }

    @Override
    public String description() {
        return "Show this session's playtime.";
    }

    @Override
    public String usage() {
        return "/cplaytime [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target = target(sender, args);
        if (target == null) {
            return;
        }
        long start = plugin.users().get(target).getLong("session-start", 0);
        long seconds = start <= 0 ? 0 : (System.currentTimeMillis() - start) / 1000;
        Text.send(sender, "<gray>Session playtime for <white>" + Text.escape(target.getName())
                + "</white>: <green>" + Time.format(seconds) + "</green>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
