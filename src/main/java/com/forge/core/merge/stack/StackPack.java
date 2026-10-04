package com.forge.core.merge.stack;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Stacking merge registrar (from the standalone forge-stack plugin).
 *
 * <p>Commands in this pack: stack (alias fstack).
 *
 * <p>Managers register their own listeners and tasks in their constructors
 * (booted via {@link StackSetup#init(ForgeCore)}).
 */
public final class StackPack {
    private StackPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        StackSetup.init(plugin);
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new StackCommand(plugin));
        return commands;
    }
}
