package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /titlemsg — send a title to a player or everyone (*).
 * Split subtitle from title with " | ": {@code /titlemsg * Hello | welcome}.
 */
public final class TitlemsgCommand extends ForgeCommand {
    public TitlemsgCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "titlemsg";
    }

    @Override
    public String description() {
        return "Send a title to a player or everyone (*); subtitle after \" | \".";
    }

    @Override
    public String usage() {
        return "/titlemsg <player|*> <title> [| <subtitle>]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        String[] parts = String.join(" ", argsRange(args, 1)).split(" \\| ", 2);
        Component title = Text.of(parts[0]);
        Component subtitle = parts.length > 1 ? Text.of(parts[1]) : Component.empty();
        Title full = Title.title(title, subtitle);
        if (args[0].equals("*")) {
            int count = 0;
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                online.showTitle(full);
                count++;
            }
            Text.ok(sender, "Title sent to <white>" + count + "</white> players.");
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        target.showTitle(full);
        Text.ok(sender, "Title sent to <white>" + Text.escape(target.getName()) + "</white>.");
    }

    private static String[] argsRange(String[] args, int from) {
        String[] out = new String[args.length - from];
        System.arraycopy(args, from, out, 0, out.length);
        return out;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> names = new ArrayList<>(Players.onlineNames());
            names.add("*");
            return Players.filter(names, args);
        }
        return List.of();
    }
}
