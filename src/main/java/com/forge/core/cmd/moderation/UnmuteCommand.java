package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Unmute a muted player. */
public final class UnmuteCommand extends ForgeCommand {
    public UnmuteCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "unmute";
    }

    @Override
    public String description() {
        return "Unmute a muted player.";
    }

    @Override
    public String usage() {
        return "/unmute <player>";
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
        if (!plugin.mutes().isMuted(target)) {
            Text.error(sender, "<white>" + Text.escape(target.getName()) + "</white> is not muted.");
            return;
        }
        plugin.mutes().unmute(target);
        Text.ok(sender, "Unmuted <white>" + Text.escape(target.getName()) + "</white>.");
        Text.send(target, "You have been unmuted.");
        Staff.notify("<yellow><bold>Unmute:</bold></yellow> <white>" + Text.escape(target.getName())
                + "</white> was unmuted by <white>" + Text.escape(sender.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
