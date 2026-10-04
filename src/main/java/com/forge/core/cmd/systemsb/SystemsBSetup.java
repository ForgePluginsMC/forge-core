package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsb.bossbar.BossBarManager;
import com.forge.core.cmd.systemsb.bungee.BungeeManager;
import com.forge.core.cmd.systemsb.counter.CounterManager;
import com.forge.core.cmd.systemsb.elevator.ElevatorManager;
import com.forge.core.cmd.systemsb.flight.FlightChargeManager;
import com.forge.core.cmd.systemsb.rank.RankManager;
import com.forge.core.cmd.systemsb.schedule.ScheduleManager;
import com.forge.core.cmd.systemsb.tablist.TablistManager;

/**
 * Boots every Systems-B manager and exposes them to the pack's commands.
 * Called once at the top of {@link SystemsBPack#commands(ForgeCore)}.
 */
public final class SystemsBSetup {
    private static boolean initialized;
    private static RankManager ranks;
    private static BossBarManager bossbars;
    private static BungeeManager bungee;
    private static TablistManager tablist;
    private static FlightChargeManager flight;
    private static ScheduleManager schedules;
    private static CounterManager counter;

    private SystemsBSetup() {
    }

    public static void init(ForgeCore plugin) {
        if (initialized) {
            return;
        }
        initialized = true;
        ranks = new RankManager(plugin);
        bossbars = new BossBarManager(plugin);
        bungee = new BungeeManager(plugin);
        tablist = new TablistManager(plugin);
        flight = new FlightChargeManager(plugin);
        schedules = new ScheduleManager(plugin);
        counter = new CounterManager(plugin);
        new ElevatorManager(plugin);
    }

    public static RankManager ranks() {
        return ranks;
    }

    public static BossBarManager bossbars() {
        return bossbars;
    }

    public static BungeeManager bungee() {
        return bungee;
    }

    public static TablistManager tablist() {
        return tablist;
    }

    public static FlightChargeManager flight() {
        return flight;
    }

    public static ScheduleManager schedules() {
        return schedules;
    }

    public static CounterManager counter() {
        return counter;
    }
}
