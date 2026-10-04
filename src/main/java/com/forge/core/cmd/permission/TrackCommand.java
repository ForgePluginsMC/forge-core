package com.forge.core.cmd.permission;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.permission.GroupManager;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.Nullable;

/** Named promotion ladders: create tracks, order groups, promote/demote players. */
public final class TrackCommand extends ForgeCommand {
    public TrackCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "track";
    }

    @Override
    public String description() {
        return "Manage promotion tracks and move players along them.";
    }

    @Override
    public String usage() {
        return "/track <create|delete|add|remove|list|info|promote|demote> ...";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        GroupManager groups = plugin.permissions().groups();
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "create" -> {
                need(args, 2, "/track create <name>");
                if (groups.createTrack(args[1])) {
                    Text.ok(sender, "Created track <white>" + Text.escape(args[1].toLowerCase(Locale.ROOT)) + "</white>.");
                } else {
                    Text.error(sender, "Track already exists.");
                }
            }
            case "delete" -> {
                need(args, 2, "/track delete <name>");
                if (groups.deleteTrack(args[1])) {
                    Text.ok(sender, "Deleted track.");
                } else {
                    Text.error(sender, "Unknown track.");
                }
            }
            case "add" -> {
                need(args, 3, "/track add <track> <group>");
                requireGroup(sender, args[2]);
                if (groups.trackAdd(args[1], args[2])) {
                    Text.ok(sender, "Added <white>" + Text.escape(args[2].toLowerCase(Locale.ROOT))
                            + "</white> to track <white>" + Text.escape(args[1].toLowerCase(Locale.ROOT)) + "</white>.");
                } else {
                    Text.error(sender, "Unknown track/group, or already on the track.");
                }
            }
            case "remove" -> {
                need(args, 3, "/track remove <track> <group>");
                if (groups.trackRemove(args[1], args[2])) {
                    Text.ok(sender, "Removed from track.");
                } else {
                    Text.error(sender, "Unknown track, or group not on it.");
                }
            }
            case "list" -> {
                Text.send(sender, "<gold><bold>Tracks</bold></gold>");
                for (String name : groups.trackNames()) {
                    Text.send(sender, "  <white>" + Text.escape(name) + "</white>");
                }
            }
            case "info" -> {
                need(args, 2, "/track info <track>");
                List<String> track = groups.getTrack(args[1]);
                if (track == null) {
                    Text.error(sender, "Unknown track.");
                    return;
                }
                Text.send(sender, "<gold><bold>Track " + Text.escape(args[1].toLowerCase(Locale.ROOT)) + "</bold></gold>");
                int i = 1;
                for (String group : track) {
                    Text.send(sender, "  <gray>" + i + ".</gray> <white>" + Text.escape(group) + "</white>");
                    i++;
                }
            }
            case "promote" -> {
                need(args, 3, "/track promote <player> <track>");
                move(sender, args[1], args[2], true);
            }
            case "demote" -> {
                need(args, 3, "/track demote <player> <track>");
                move(sender, args[1], args[2], false);
            }
            default -> Text.usage(sender, usage());
        }
    }

    private void need(String[] args, int want, String usage) {
        if (args.length < want) {
            throw new CommandRegistry.CommandFailure("Not enough arguments.", usage);
        }
    }

    private void requireGroup(CommandSender sender, String name) {
        if (plugin.permissions().groups().getGroup(name) == null) {
            throw new CommandRegistry.CommandFailure(
                    "Unknown group: <white>" + Text.escape(name) + "</white>.");
        }
    }

    private @Nullable String currentOnTrack(UUID uuid, List<String> track) {
        String best = null;
        int bestIndex = -1;
        for (String group : plugin.permissions().userGroups(uuid)) {
            int index = track.indexOf(group);
            if (index > bestIndex) {
                bestIndex = index;
                best = group;
            }
        }
        return best;
    }

    private void move(CommandSender sender, String playerName, String trackName, boolean up) {
        GroupManager groups = plugin.permissions().groups();
        List<String> track = groups.getTrack(trackName);
        if (track == null || track.isEmpty()) {
            Text.error(sender, "Unknown or empty track.");
            return;
        }
        UUID uuid = PermUtil.uuidOf(sender, playerName);
        if (uuid == null) {
            return;
        }
        String current = currentOnTrack(uuid, track);
        int index = current == null ? (up ? -1 : 0) : track.indexOf(current);
        int next = up ? index + 1 : index - 1;
        if (next < 0 || next >= track.size()) {
            Text.error(sender, up ? "Already at the top of the track." : "Already at the bottom of the track.");
            return;
        }
        String target = track.get(next);
        if (current != null) {
            plugin.permissions().removeGroup(uuid, current);
        }
        plugin.permissions().addGroup(uuid, target);
        Text.ok(sender, (up ? "Promoted <white>" : "Demoted <white>")
                + Text.escape(PermUtil.displayName(uuid)) + "</white> to <white>"
                + Text.escape(target) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> subs = List.of("create", "delete", "add", "remove", "list", "info", "promote", "demote");
        if (args.length == 1) {
            return Players.filter(subs, args);
        }
        GroupManager groups = plugin.permissions().groups();
        if (args.length == 2 && !args[0].equalsIgnoreCase("create") && !args[0].equalsIgnoreCase("list")) {
            if (args[0].equalsIgnoreCase("promote") || args[0].equalsIgnoreCase("demote")) {
                return Players.filter(Players.onlineNames(), args);
            }
            return Players.filter(new ArrayList<>(groups.trackNames()), args);
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                return Players.filter(new ArrayList<>(groups.groupNames()), args);
            }
            if (args[0].equalsIgnoreCase("promote") || args[0].equalsIgnoreCase("demote")) {
                return Players.filter(new ArrayList<>(groups.trackNames()), args);
            }
        }
        return List.of();
    }
}
