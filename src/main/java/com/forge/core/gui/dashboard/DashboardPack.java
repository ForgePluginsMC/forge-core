package com.forge.core.gui.dashboard;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard pack registrar.
 *
 * <p>Commands: dashboard.
 */
public final class DashboardPack {
    private DashboardPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new DashboardCommand(plugin));
        return commands;
    }
}
