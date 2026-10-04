package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Send an advancement-style toast notification.
 *
 * <p>Paper 26.3 has no direct toast API, so this renders a toast-styled
 * title + subtitle, which is the closest server-side equivalent.
 */
public final class ToastCommand extends ForgeCommand {
    public ToastCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "toast";
    }

    @Override
    public String description() {
        return "Send a toast-style notification to a player.";
    }

    @Override
    public String usage() {
        return "/toast <player> <title...> | <description...>";
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
        String joined = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String[] parts = joined.split("\\|", 2);
        String title = parts[0].trim();
        String desc = parts.length > 1 ? parts[1].trim() : "";
        if (title.isEmpty()) {
            Text.error(sender, "Title cannot be empty.");
            return;
        }
        // Toast-styled: gold bold title like an advancement toast header
        Component titleComp = Text.of("<gold><bold>!</bold></gold> <yellow><bold>"
                + Text.escape(title) + "</bold></yellow>");
        Component subComp = desc.isEmpty() ? Component.empty() : Text.of("<gray>" + Text.escape(desc) + "</gray>");
        target.showTitle(Title.title(titleComp, subComp,
                Title.Times.times(
                        java.time.Duration.ofMillis(250),
                        java.time.Duration.ofMillis(3500),
                        java.time.Duration.ofMillis(750))));
        Text.ok(sender, "Toast sent to <white>" + Text.escape(target.getName()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
