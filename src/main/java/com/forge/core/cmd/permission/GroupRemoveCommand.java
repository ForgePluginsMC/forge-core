package com.forge.core.cmd.permission;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;

/** Remove a player from a group. */
public final class GroupRemoveCommand extends ForgeCommand {
    public GroupRemoveCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "groupremove";
    }

    @Override
    public String description() {
        return "Remove a player from a group.";
    }

    @Override
    public String usage() {
        return "/groupremove <player> <group>";
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
        if (plugin.permissions().removeGroup(uuid, args[1])) {
            Text.ok(sender, "Removed <white>" + Text.escape(PermUtil.displayName(uuid))
                    + "</white> from group <white>" + Text.escape(args[1]) + "</white>.");
        } else {
            Text.error(sender, "Player is not in that group.");
        }
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
