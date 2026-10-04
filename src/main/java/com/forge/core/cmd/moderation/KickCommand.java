package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Kick an online player. */
public final class KickCommand extends ForgeCommand {
    public KickCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "kick";
    }

    @Override
    public String description() {
        return "Kick a player from the server.";
    }

    @Override
    public String usage() {
        return "/kick <player> [reason...]";
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
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Kicked";
        target.kick(Text.of("<red>You were kicked from the server.\n<gray>Reason: <white>" + Text.escape(reason)));
        Text.ok(sender, "Kicked <white>" + Text.escape(target.getName()) + "</white>.");
        Staff.notify("<red><bold>Kick:</bold></red> <white>" + Text.escape(target.getName())
                + "</white> was kicked by <white>" + Text.escape(sender.getName())
                + "</white>: " + Text.escape(reason));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
