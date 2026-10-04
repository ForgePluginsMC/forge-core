package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Moderation pack registrar. Add one {@code new XxxCommand(plugin)} per command.
 *
 * <p>Commands in this pack: ban, tempban, unban, checkban, kick, mute,
 * silence, mutechat, jail, jailedit, unjail, lockip, sudo, smite, socialspy,
 * commandspy, invcheck, inv, vanish, vanishedit, patrol, purge, removeuser,
 * seen, lastonline, whowas, oplist, staffmsg, helpop, alert, broadcast,
 * clearchat, maintenance, maxplayer, saveall, checkaccount.
 */
public final class ModerationPack {
    private ModerationPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        ModerationSetup.init(plugin);
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new BanCommand(plugin));
        commands.add(new TempbanCommand(plugin));
        commands.add(new UnbanCommand(plugin));
        commands.add(new CheckbanCommand(plugin));
        commands.add(new KickCommand(plugin));
        commands.add(new MuteCommand(plugin));
        commands.add(new SilenceCommand(plugin));
        commands.add(new MutechatCommand(plugin));
        commands.add(new JailCommand(plugin));
        commands.add(new JaileditCommand(plugin));
        commands.add(new UnjailCommand(plugin));
        commands.add(new LockipCommand(plugin));
        commands.add(new SudoCommand(plugin));
        commands.add(new SmiteCommand(plugin));
        commands.add(new SocialspyCommand(plugin));
        commands.add(new CommandspyCommand(plugin));
        commands.add(new InvcheckCommand(plugin));
        commands.add(new InvCommand(plugin));
        commands.add(new VanishCommand(plugin));
        commands.add(new VanisheditCommand(plugin));
        commands.add(new PatrolCommand(plugin));
        commands.add(new PurgeCommand(plugin));
        commands.add(new RemoveuserCommand(plugin));
        commands.add(new SeenCommand(plugin));
        commands.add(new LastonlineCommand(plugin));
        commands.add(new WhowasCommand(plugin));
        commands.add(new OplistCommand(plugin));
        commands.add(new StaffmsgCommand(plugin));
        commands.add(new HelpopCommand(plugin));
        commands.add(new AlertCommand(plugin));
        commands.add(new BroadcastCommand(plugin));
        commands.add(new ClearchatCommand(plugin));
        commands.add(new MaintenanceCommand(plugin));
        commands.add(new MaxplayerCommand(plugin));
        commands.add(new SaveallCommand(plugin));
        commands.add(new CheckaccountCommand(plugin));
        commands.add(new BanipCommand(plugin));
        commands.add(new TempbanipCommand(plugin));
        commands.add(new UnbanipCommand(plugin));
        commands.add(new UnmuteCommand(plugin));
        commands.add(new KickallCommand(plugin));
        commands.add(new BanlistCommand(plugin));
        return commands;
    }
}
