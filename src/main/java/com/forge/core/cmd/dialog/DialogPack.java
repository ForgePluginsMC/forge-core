package com.forge.core.cmd.dialog;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog pack registrar.
 *
 * <p>Commands: dialog, npc.
 */
public final class DialogPack {
    private DialogPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new DialogCommand(plugin));
        commands.add(new NpcCommand(plugin));
        return commands;
    }
}
