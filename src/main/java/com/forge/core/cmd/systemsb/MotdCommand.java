package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/** Show the server message of the day. */
public final class MotdCommand extends ForgeCommand {
    public MotdCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "motd";
    }

    @Override
    public String description() {
        return "Show the server message of the day.";
    }

    @Override
    public String usage() {
        return "/motd";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        String motd = plugin.getConfig().getString("motd", null);
        if (motd == null || motd.isBlank()) {
            Text.send(sender, "No MOTD set. An admin can set one with <white>/setmotd</white>.");
            return;
        }
        Text.send(sender, motd);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
