package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle vanish for another player. */
public final class VanisheditCommand extends ForgeCommand {
    public VanisheditCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "vanishedit";
    }

    @Override
    public String description() {
        return "Toggle vanish for another player.";
    }

    @Override
    public String usage() {
        return "/vanishedit <player>";
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
        boolean now = ModerationSetup.vanish().toggle(target);
        Text.ok(sender, "<white>" + Text.escape(target.getName()) + "</white> is "
                + (now ? "now vanished." : "no longer vanished."));
        Text.send(target, now ? "You have been vanished." : "You are no longer vanished.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
