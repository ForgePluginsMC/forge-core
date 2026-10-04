package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Kick every online player (except the sender) with a reason. */
public final class KickallCommand extends ForgeCommand {
    public KickallCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "kickall";
    }

    @Override
    public String description() {
        return "Kick all online players.";
    }

    @Override
    public String usage() {
        return "/kickall [reason...]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        String reason = args.length > 0 ? String.join(" ", args) : "Kicked";
        List<Player> kicked = new ArrayList<>();
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.equals(sender)) {
                continue;
            }
            kicked.add(online);
        }
        for (Player target : kicked) {
            target.kick(Text.of("<red>You were kicked from the server.\n<gray>Reason: <white>" + Text.escape(reason)));
        }
        Text.ok(sender, "Kicked <white>" + kicked.size() + "</white> players.");
        Staff.notify("<red><bold>Kickall:</bold></red> <white>" + Text.escape(sender.getName())
                + "</white> kicked <white>" + kicked.size() + "</white> players: " + Text.escape(reason));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
