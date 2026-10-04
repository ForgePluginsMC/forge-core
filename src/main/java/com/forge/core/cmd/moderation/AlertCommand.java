package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/** Broadcast a styled alert to the whole server. */
public final class AlertCommand extends ForgeCommand {
    public AlertCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "alert";
    }

    @Override
    public String description() {
        return "Broadcast a styled alert to everyone.";
    }

    @Override
    public String usage() {
        return "/alert <message...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Bukkit.broadcast(Text.of("<red><bold>Alert:</bold></red> <white>" + Text.escape(String.join(" ", args))));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
