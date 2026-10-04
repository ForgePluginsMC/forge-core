package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /actionbarmsg — send an action-bar message to a player or everyone (*). */
public final class ActionbarmsgCommand extends ForgeCommand {
    public ActionbarmsgCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "actionbarmsg";
    }

    @Override
    public String description() {
        return "Send an action-bar message to a player or everyone (*).";
    }

    @Override
    public String usage() {
        return "/actionbarmsg <player|*> <message...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        Component message = Text.of(String.join(" ", argsRange(args, 1)));
        if (args[0].equals("*")) {
            int count = 0;
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                online.sendActionBar(message);
                count++;
            }
            Text.ok(sender, "Action-bar message sent to <white>" + count + "</white> players.");
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        target.sendActionBar(message);
        Text.ok(sender, "Action-bar message sent to <white>" + Text.escape(target.getName()) + "</white>.");
    }

    private static String[] argsRange(String[] args, int from) {
        String[] out = new String[args.length - from];
        System.arraycopy(args, from, out, 0, out.length);
        return out;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> names = new java.util.ArrayList<>(Players.onlineNames());
            names.add("*");
            return Players.filter(names, args);
        }
        return List.of();
    }
}
