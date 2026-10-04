package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Mute a player: /mute <player> [duration] [reason...]. No duration = forever. */
public final class MuteCommand extends ForgeCommand {
    public MuteCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "mute";
    }

    @Override
    public String description() {
        return "Mute a player (optional duration, forever by default).";
    }

    @Override
    public String usage() {
        return "/mute <player> [duration] [reason...]";
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
        long until = 0L;
        int reasonFrom = 1;
        if (args.length > 1) {
            try {
                long seconds = Time.parseSeconds(args[1]);
                until = System.currentTimeMillis() + seconds * 1000L;
                reasonFrom = 2;
            } catch (IllegalArgumentException ignored) {
                // args[1] is the start of the reason, not a duration.
            }
        }
        String reason = args.length > reasonFrom ? String.join(" ", Arrays.copyOfRange(args, reasonFrom, args.length)) : "Muted";
        // Toggle off when already muted and no new terms were given.
        if (plugin.mutes().isMuted(target) && args.length == 1) {
            plugin.mutes().unmute(target);
            Text.ok(sender, "Unmuted <white>" + Text.escape(target.getName()) + "</white>.");
            Text.send(target, "You have been unmuted.");
            return;
        }
        plugin.mutes().mute(target, until, reason);
        String length = until == 0 ? "forever" : "for " + Time.format((until - System.currentTimeMillis()) / 1000);
        Text.ok(sender, "Muted <white>" + Text.escape(target.getName()) + "</white> " + length + ".");
        Text.error(target, "You have been muted " + length + ": " + Text.escape(reason));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        if (args.length == 2) {
            return Players.filter(List.of("10m", "1h", "1d", "7d"), args);
        }
        return List.of();
    }
}
