package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Teleport pack registrar. Add one {@code new XxxCommand(plugin)} per command.
 *
 * <p>Commands in this pack: back, dback, home, homes, sethome, removehome,
 * warp, setwarp, removewarp, editwarp, spawn, setspawn, setfirstspawn, tp,
 * tpa, tpaall, tpaccept, tpahere, tpall, tpallworld, tpbypass, tpdeny, tphere,
 * tppos, tptoggle, jump, top, rtp, setrt, group, near, pos, list, point, launch.
 */
public final class TeleportPack {
    private TeleportPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new BackCommand(plugin));
        commands.add(new DbackCommand(plugin));
        commands.add(new HomeCommand(plugin));
        commands.add(new HomesCommand(plugin));
        commands.add(new SetHomeCommand(plugin));
        commands.add(new RemoveHomeCommand(plugin));
        commands.add(new WarpCommand(plugin));
        commands.add(new SetWarpCommand(plugin));
        commands.add(new RemoveWarpCommand(plugin));
        commands.add(new EditWarpCommand(plugin));
        commands.add(new SpawnCommand(plugin));
        commands.add(new SetSpawnCommand(plugin));
        commands.add(new SetFirstSpawnCommand(plugin));
        commands.add(new TpCommand(plugin));
        commands.add(new TpaCommand(plugin));
        commands.add(new TpaAllCommand(plugin));
        commands.add(new TpAcceptCommand(plugin));
        commands.add(new TpaHereCommand(plugin));
        commands.add(new TpAllCommand(plugin));
        commands.add(new TpAllWorldCommand(plugin));
        commands.add(new TpBypassCommand(plugin));
        commands.add(new TpDenyCommand(plugin));
        commands.add(new TpHereCommand(plugin));
        commands.add(new TpPosCommand(plugin));
        commands.add(new TpToggleCommand(plugin));
        commands.add(new JumpCommand(plugin));
        commands.add(new TopCommand(plugin));
        commands.add(new RtpCommand(plugin));
        commands.add(new SetRtCommand(plugin));
        commands.add(new GroupCommand(plugin));
        commands.add(new NearCommand(plugin));
        commands.add(new PosCommand(plugin));
        commands.add(new ListCommand(plugin));
        commands.add(new PointCommand(plugin));
        commands.add(new LaunchCommand(plugin));
        commands.add(new TpoCommand(plugin));
        commands.add(new TpohereCommand(plugin));
        commands.add(new WorldCommand(plugin));
        return commands;
    }
}
