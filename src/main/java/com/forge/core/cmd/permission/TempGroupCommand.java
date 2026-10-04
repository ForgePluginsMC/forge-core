package com.forge.core.cmd.permission;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.permission.Group;
import com.forge.core.permission.PermissionEntry;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.command.CommandSender;

/** Grant a player a temporary group membership. */
public final class TempGroupCommand extends ForgeCommand {
    public TempGroupCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tempgroup";
    }

    @Override
    public String description() {
        return "Give a player a group for a limited time.";
    }

    @Override
    public String usage() {
        return "/tempgroup <player> <group> <duration>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length != 3) {
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
        long seconds;
        try {
            seconds = Time.parseSeconds(args[2]);
        } catch (IllegalArgumentException e) {
            throw new CommandRegistry.CommandFailure("Bad duration. Examples: 30m, 2h, 7d.");
        }
        long expiry = System.currentTimeMillis() + seconds * 1000L;
        plugin.permissions().addGroup(uuid, group.name(), expiry);
        Text.ok(sender, "Gave <white>" + Text.escape(PermUtil.displayName(uuid)) + "</white> group <white>"
                + Text.escape(group.name()) + "</white> for <white>" + Time.format(seconds) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        if (args.length == 2) {
            return Players.filter(new ArrayList<>(plugin.permissions().groups().groupNames()), args);
        }
        if (args.length == 3) {
            return Players.filter(List.of("30m", "1h", "1d", "7d", "30d"), args);
        }
        return List.of();
    }
}
