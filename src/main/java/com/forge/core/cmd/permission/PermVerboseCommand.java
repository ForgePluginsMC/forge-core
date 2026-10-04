package com.forge.core.cmd.permission;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle real-time permission check logging for the sender. */
public final class PermVerboseCommand extends ForgeCommand {
    public PermVerboseCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "permverbose";
    }

    @Override
    public String description() {
        return "Toggle live permission check logging.";
    }

    @Override
    public String usage() {
        return "/permverbose";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player player) {
            boolean on = plugin.permissions().toggleVerbose(player.getUniqueId());
            Text.ok(sender, "Verbose logging " + (on ? "<green>enabled" : "<red>disabled") + "<green>.");
        } else {
            boolean on = plugin.permissions().toggleVerboseConsole();
            Text.ok(sender, "Console verbose logging " + (on ? "<green>enabled" : "<red>disabled") + "<green>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
