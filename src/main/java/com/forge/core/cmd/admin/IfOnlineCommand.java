package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/** /ifonline — run a command as console, but only if the player is online. */
public final class IfOnlineCommand extends ForgeCommand {
    public IfOnlineCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "ifonline";
    }

    @Override
    public String description() {
        return "Run a command as console if the player is online.";
    }

    @Override
    public String usage() {
        return "/ifonline <player> <command...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        if (Bukkit.getPlayerExact(args[0]) == null) {
            Text.send(sender, "<gray>Condition not met: <white>" + Text.escape(args[0])
                    + "</white> is offline. Command not run.</gray>");
            return;
        }
        String command = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        boolean ok = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        if (ok) {
            Text.ok(sender, "Ran as console: <white>" + Text.escape(command) + "</white>");
        } else {
            Text.error(sender, "Command failed or is unknown: " + Text.escape(command));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
