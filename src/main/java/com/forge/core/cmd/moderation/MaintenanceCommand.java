package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/** Toggle maintenance mode. */
public final class MaintenanceCommand extends ForgeCommand {
    public MaintenanceCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "maintenance";
    }

    @Override
    public String description() {
        return "Toggle maintenance mode.";
    }

    @Override
    public String usage() {
        return "/maintenance";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        boolean now = ModerationSetup.maintenance().toggle();
        if (now) {
            Text.broadcast("<red>Maintenance mode <bold>enabled</bold><red> by <white>" + Text.escape(sender.getName()) + "</white>.");
        } else {
            Text.broadcast("<green>Maintenance mode <bold>disabled</bold><green> by <white>" + Text.escape(sender.getName()) + "</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
