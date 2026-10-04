package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Show connection ping. */
public final class PingCommand extends PlayerACommand {
    public PingCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public String description() {
        return "Show connection ping.";
    }

    @Override
    public String usage() {
        return "/ping [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target = target(sender, args);
        if (target == null) {
            return;
        }
        Text.send(sender, "<gray>Ping for <white>" + Text.escape(target.getName()) + "</white>: <green>"
                + target.getPing() + "ms</green>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
