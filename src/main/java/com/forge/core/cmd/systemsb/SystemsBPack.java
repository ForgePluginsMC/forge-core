package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Systems-B pack registrar: server systems.
 *
 * <p>Commands in this pack: rankup, rankdown, rankset, ranklist, rankinfo,
 * bossbarmsg, bbroadcast, server, serverlist, sendall, tablistupdate,
 * flightcharge, charges, schedule, counter, viewrange.
 *
 * <p>Each system owns its manager under
 * {@code com.forge.core.cmd.systemsb.<system>}. Managers register their own
 * listeners and tasks in their constructors. Elevators are sign-based
 * (no command): a sign with [elevator] up/down on the second line.
 */
public final class SystemsBPack {
    private SystemsBPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        SystemsBSetup.init(plugin);
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new RankupCommand(plugin));
        commands.add(new RankdownCommand(plugin));
        commands.add(new RanksetCommand(plugin));
        commands.add(new RanklistCommand(plugin));
        commands.add(new RankinfoCommand(plugin));
        commands.add(new BossbarmsgCommand(plugin));
        commands.add(new ServerCommand(plugin));
        commands.add(new SendallCommand(plugin));
        commands.add(new BbroadcastCommand(plugin));
        commands.add(new ServerlistCommand(plugin));
        commands.add(new TablistupdateCommand(plugin));
        commands.add(new FlightchargeCommand(plugin));
        commands.add(new ChargesCommand(plugin));
        commands.add(new ScheduleCommand(plugin));
        commands.add(new CounterCommand(plugin));
        commands.add(new ViewrangeCommand(plugin));
        return commands;
    }
}
