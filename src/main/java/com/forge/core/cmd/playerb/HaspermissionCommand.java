package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /haspermission — test whether an online player has a permission node. */
public final class HaspermissionCommand extends ForgeCommand {
    public HaspermissionCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "haspermission";
    }

    @Override
    public String description() {
        return "Test whether a player has a permission node.";
    }

    @Override
    public String usage() {
        return "/haspermission <player> <permission>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length != 2) {
            Text.usage(sender, usage());
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        boolean has = target.hasPermission(args[1]);
        Text.send(sender, "<white>" + Text.escape(target.getName()) + "</white> "
                + (has ? "<green>has</green>" : "<red>does not have</red>")
                + " permission <white>" + Text.escape(args[1]) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
