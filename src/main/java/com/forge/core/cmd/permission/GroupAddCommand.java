package com.forge.core.cmd.permission;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.permission.Group;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;

/** Add a player to a group. */
public final class GroupAddCommand extends ForgeCommand {
    public GroupAddCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "groupadd";
    }

    @Override
    public String description() {
        return "Add a player to a group.";
    }

    @Override
    public String usage() {
        return "/groupadd <player> <group>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length != 2) {
            Text.usage(sender, usage());
            return;
        }
        UUID uuid = PermUtil.uuidOf(sender, args[0]);
        if (uuid == null) {
            return;
        }
        Group group = plugin.permissions().groups().getGroup(args[1]);
        if (group == null) {
            Text.error(sender, "Unknown group: <white>" + Text.escape(args[1]) + "</white>.");
            return;
        }
        plugin.permissions().addGroup(uuid, group.name());
        Text.ok(sender, "Added <white>" + Text.escape(PermUtil.displayName(uuid))
                + "</white> to group <white>" + Text.escape(group.name()) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        if (args.length == 2) {
            return Players.filter(new ArrayList<>(plugin.permissions().groups().groupNames()), args);
        }
        return List.of();
    }
}
