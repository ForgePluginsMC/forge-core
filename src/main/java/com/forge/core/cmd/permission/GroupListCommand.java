package com.forge.core.cmd.permission;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.command.CommandSender;

/** List groups, or a player's groups with primary marked. */
public final class GroupListCommand extends ForgeCommand {
    public GroupListCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "grouplist";
    }

    @Override
    public String description() {
        return "List groups, or a player's groups.";
    }

    @Override
    public String usage() {
        return "/grouplist [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            Text.usage(sender, usage());
            return;
        }
        if (args.length == 0) {
            Set<String> names = plugin.permissions().groups().groupNames();
            Text.send(sender, "<gold><bold>Groups (" + names.size() + ")</bold></gold>");
            for (String name : names) {
                var group = plugin.permissions().groups().getGroup(name);
                int weight = group == null ? 0 : group.weight();
                Text.send(sender, "  <white>" + Text.escape(name) + "</white> <gray>(weight "
                        + weight + ")</gray>");
            }
            return;
        }
        UUID uuid = PermUtil.uuidOf(sender, args[0]);
        if (uuid == null) {
            return;
        }
        Set<String> memberships = plugin.permissions().userGroups(uuid);
        String primary = plugin.permissions().primaryGroup(uuid);
        Text.send(sender, "<gold><bold>Groups of " + Text.escape(PermUtil.displayName(uuid))
                + "</bold></gold>");
        for (String name : memberships) {
            String marker = name.equals(primary) ? " <yellow>[primary]</yellow>" : "";
            Text.send(sender, "  <white>" + Text.escape(name) + "</white>" + marker);
        }
        if (memberships.isEmpty()) {
            Text.send(sender, "  <gray>(none)</gray>");
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
