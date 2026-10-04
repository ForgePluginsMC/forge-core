package com.forge.core.merge.playtime;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/** Registers the merged forge-playtime commands. Wired in by the parent. */
public final class PlaytimePack {
    private PlaytimePack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        MilestoneManager manager = new MilestoneManager(plugin);
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new PlaytimeRewardsCommand(plugin, manager));
        return commands;
    }
}
