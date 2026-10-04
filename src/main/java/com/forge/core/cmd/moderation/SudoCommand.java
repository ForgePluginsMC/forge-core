package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Force a player to run a command, or to say something in chat when the
 * arguments start with {@code c }.
 */
public final class SudoCommand extends ForgeCommand {
    public SudoCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "sudo";
    }

    @Override
    public String description() {
        return "Force a player to run a command (or say text with 'c ').";
    }

    @Override
    public String usage() {
        return "/sudo <player> <command...>  (use 'c <text>' to make them chat)";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        if (args[1].equalsIgnoreCase("c")) {
            if (args.length < 3) {
                Text.usage(sender, usage());
                return;
            }
            target.chat(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
            Text.ok(sender, "Made <white>" + Text.escape(target.getName()) + "</white> say something.");
        } else {
            target.performCommand(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            Text.ok(sender, "Made <white>" + Text.escape(target.getName()) + "</white> run a command.");
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
