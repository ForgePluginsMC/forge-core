package com.forge.core.cmd.permission;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.permission.Group;
import com.forge.core.permission.PermissionEntry;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.CommandSender;

/**
 * Group administration: create, delete, permissions, parents, prefix/suffix,
 * weight and inspection.
 */
public final class GroupCommand extends ForgeCommand {
    public GroupCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "group";
    }

    @Override
    public String description() {
        return "Manage permission groups.";
    }

    @Override
    public String usage() {
        return "/group <create|delete|setperm|unsetperm|addparent|removeparent|setprefix|setsuffix|setweight|info> ...";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "create" -> create(sender, args[1]);
            case "delete" -> delete(sender, args[1]);
            case "setperm" -> {
                requireArgs(sender, args, 3, "/group setperm <group> <node>");
                setPerm(sender, args[1], args[2]);
            }
            case "unsetperm" -> {
                requireArgs(sender, args, 3, "/group unsetperm <group> <node>");
                unsetPerm(sender, args[1], args[2]);
            }
            case "addparent" -> {
                requireArgs(sender, args, 3, "/group addparent <group> <parent>");
                parent(sender, args[1], args[2], true);
            }
            case "removeparent" -> {
                requireArgs(sender, args, 3, "/group removeparent <group> <parent>");
                parent(sender, args[1], args[2], false);
            }
            case "setprefix" -> {
                requireArgs(sender, args, 3, "/group setprefix <group> <prefix>");
                meta(sender, args[1], args[2], true);
            }
            case "setsuffix" -> {
                requireArgs(sender, args, 3, "/group setsuffix <group> <suffix>");
                meta(sender, args[1], args[2], false);
            }
            case "setweight" -> {
                requireArgs(sender, args, 3, "/group setweight <group> <weight>");
                weight(sender, args[1], args[2]);
            }
            case "info" -> info(sender, args[1]);
            default -> Text.usage(sender, usage());
        }
    }

    private void requireArgs(CommandSender sender, String[] args, int want, String usage) {
        if (args.length < want) {
            throw new CommandRegistry.CommandFailure("Not enough arguments.", usage);
        }
    }

    private Group require(CommandSender sender, String name) {
        Group group = plugin.permissions().groups().getGroup(name);
        if (group == null) {
            throw new CommandRegistry.CommandFailure(
                    "Unknown group: <white>" + Text.escape(name) + "</white>.");
        }
        return group;
    }

    private void create(CommandSender sender, String name) {
        if (plugin.permissions().groups().createGroup(name)) {
            Text.ok(sender, "Created group <white>" + Text.escape(name.toLowerCase(Locale.ROOT)) + "</white>.");
        } else {
            Text.error(sender, "Group already exists.");
        }
    }

    private void delete(CommandSender sender, String name) {
        if (plugin.permissions().groups().deleteGroup(name)) {
            Text.ok(sender, "Deleted group <white>" + Text.escape(name) + "</white>.");
        } else {
            Text.error(sender, "Unknown group: <white>" + Text.escape(name) + "</white>.");
        }
    }

    private void setPerm(CommandSender sender, String groupName, String rawNode) {
        Group group = require(sender, groupName);
        PermissionEntry entry = PermissionEntry.parse(rawNode);
        group.setPermission(new PermissionEntry(entry.node(), entry.value(), Map.of(), 0));
        plugin.permissions().markDirty();
        Text.ok(sender, "Set <white>" + Text.escape(entry.display()) + "</white> on group <white>"
                + Text.escape(group.name()) + "</white>.");
    }

    private void unsetPerm(CommandSender sender, String groupName, String node) {
        Group group = require(sender, groupName);
        if (group.removePermission(node)) {
            plugin.permissions().markDirty();
            Text.ok(sender, "Removed <white>" + Text.escape(node) + "</white> from group <white>"
                    + Text.escape(group.name()) + "</white>.");
        } else {
            Text.error(sender, "Group does not have that node.");
        }
    }

    private void parent(CommandSender sender, String groupName, String parentName, boolean add) {
        Group group = require(sender, groupName);
        Group parent = require(sender, parentName);
        if (group.name().equals(parent.name())) {
            Text.error(sender, "A group cannot inherit from itself.");
            return;
        }
        if (add) {
            if (!group.parents().add(parent.name())) {
                Text.error(sender, "Already a parent.");
                return;
            }
            Text.ok(sender, "Group <white>" + Text.escape(group.name())
                    + "</white> now inherits <white>" + Text.escape(parent.name()) + "</white>.");
        } else {
            if (!group.parents().remove(parent.name())) {
                Text.error(sender, "Not a parent.");
                return;
            }
            Text.ok(sender, "Removed inheritance.");
        }
        plugin.permissions().markDirty();
    }

    private void meta(CommandSender sender, String groupName, String value, boolean prefix) {
        Group group = require(sender, groupName);
        if (prefix) {
            group.prefix(value);
        } else {
            group.suffix(value);
        }
        plugin.permissions().markDirty();
        Text.ok(sender, (prefix ? "Prefix" : "Suffix") + " set.");
    }

    private void weight(CommandSender sender, String groupName, String raw) {
        Group group = require(sender, groupName);
        int weight;
        try {
            weight = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            Text.error(sender, "Weight must be a number.");
            return;
        }
        group.weight(weight);
        plugin.permissions().markDirty();
        Text.ok(sender, "Weight set to <white>" + weight + "</white>.");
    }

    private void info(CommandSender sender, String groupName) {
        Group group = require(sender, groupName);
        Text.send(sender, "<gold><bold>Group " + Text.escape(group.name()) + "</bold></gold>");
        Text.send(sender, "<gray>Weight:</gray> <white>" + group.weight() + "</white>");
        Text.send(sender, "<gray>Prefix:</gray> <white>" + Text.escape(group.prefix()) + "</white>");
        Text.send(sender, "<gray>Suffix:</gray> <white>" + Text.escape(group.suffix()) + "</white>");
        Text.send(sender, "<gray>Parents:</gray> <white>"
                + Text.escape(String.join(", ", group.parents())) + "</white>");
        Text.send(sender, "<gray>Permissions (" + group.permissions().size() + "):</gray>");
        for (PermissionEntry entry : group.permissions()) {
            Text.send(sender, "  <white>" + Text.escape(entry.display()) + "</white>");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> subs = List.of("create", "delete", "setperm", "unsetperm", "addparent",
                "removeparent", "setprefix", "setsuffix", "setweight", "info");
        if (args.length == 1) {
            return com.forge.core.util.Players.filter(subs, args);
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("create")) {
            return com.forge.core.util.Players.filter(
                    new java.util.ArrayList<>(plugin.permissions().groups().groupNames()), args);
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("addparent")
                || args[0].equalsIgnoreCase("removeparent"))) {
            return com.forge.core.util.Players.filter(
                    new java.util.ArrayList<>(plugin.permissions().groups().groupNames()), args);
        }
        return List.of();
    }
}
