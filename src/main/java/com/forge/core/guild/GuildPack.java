package com.forge.core.guild;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Guild pack registrar. Add one {@code new XxxCommand(plugin)} per command.
 *
 * <p>Commands in this pack: guild, guildchat, claim, unclaim, claimlist, claimmap.
 */
public final class GuildPack {
    private GuildPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        GuildSetup.init(plugin);
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new GuildCommand(plugin));
        commands.add(new GuildchatCommand(plugin));
        commands.add(new ClaimCommand(plugin));
        commands.add(new UnclaimCommand(plugin));
        commands.add(new ClaimlistCommand(plugin));
        commands.add(new ClaimmapCommand(plugin));
        return commands;
    }
}
