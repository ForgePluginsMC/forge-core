package com.forge.core.cmd.permission;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Permission pack registrar. Add one {@code new XxxCommand(plugin)} per command.
 *
 * <p>Commands in this pack: perm, permset, permunset, permverbose, group,
 * groupadd, groupremove, grouplist, tempgroup, tempperm, track.
 */
public final class PermissionPack {
    private PermissionPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new PermCommand(plugin));
        commands.add(new PermSetCommand(plugin));
        commands.add(new PermUnsetCommand(plugin));
        commands.add(new PermVerboseCommand(plugin));
        commands.add(new GroupCommand(plugin));
        commands.add(new GroupAddCommand(plugin));
        commands.add(new GroupRemoveCommand(plugin));
        commands.add(new GroupListCommand(plugin));
        commands.add(new TempGroupCommand(plugin));
        commands.add(new TempPermCommand(plugin));
        commands.add(new TrackCommand(plugin));
        return commands;
    }
}
