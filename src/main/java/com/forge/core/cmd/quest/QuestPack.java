package com.forge.core.cmd.quest;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Quest pack registrar.
 *
 * <p>Commands: quest, dailies, weeklies.
 */
public final class QuestPack {
    private QuestPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new QuestCommand(plugin));
        commands.add(new DailiesCommand(plugin));
        commands.add(new WeekliesCommand(plugin));
        return commands;
    }
}
