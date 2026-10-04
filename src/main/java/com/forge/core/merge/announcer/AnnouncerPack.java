package com.forge.core.merge.announcer;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.List;

/** Registers the merged forge-announcer commands. Wired in by the parent. */
public final class AnnouncerPack {
    private AnnouncerPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        AnnouncerSetup.init(plugin);
        return List.of(new AnnounceCommand(plugin));
    }
}
