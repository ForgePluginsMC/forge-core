package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Toggle silence for a player. A silenced player cannot send any outgoing
 * message: chat is blocked by the mute listener, and /msg and /reply check
 * {@code MuteManager#isSilenced} on the player pack's side.
 */
public final class SilenceCommand extends ForgeCommand {
    public SilenceCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "silence";
    }

    @Override
    public String description() {
        return "Toggle total silence for a player (blocks all outgoing messages).";
    }

    @Override
    public String usage() {
        return "/silence <player>";
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
        boolean now = !plugin.mutes().isSilenced(target);
        plugin.mutes().setSilenced(target, now);
        if (now) {
            Text.ok(sender, "<white>" + Text.escape(target.getName()) + "</white> is now silenced.");
            Text.error(target, "You have been silenced. You cannot send any messages.");
        } else {
            Text.ok(sender, "<white>" + Text.escape(target.getName()) + "</white> is no longer silenced.");
            Text.send(target, "You are no longer silenced.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
