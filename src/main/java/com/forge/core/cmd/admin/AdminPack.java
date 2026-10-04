package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin/world pack registrar. Add one {@code new XxxCommand(plugin)} per command.
 *
 * <p>Commands in this pack: gm, setmotd, spawner, spawnmob, tps, fixchunk,
 * lfix, unloadchunks, groundclean, replaceblock, scan, search, se, blockinfo,
 * blocknbt, entityinfo, entitynbt, ifoffline, ifonline, migratedatabase,
 * importfrom, importoldusers, reload, usermeta, give, giveall, blockcycling,
 * silentchest, clear, invlist, invload, invremove, invsave.
 */
public final class AdminPack {
    private AdminPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        AdminSetup.init(plugin);
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new GmCommand(plugin));
        commands.add(new SetMotdCommand(plugin));
        commands.add(new SpawnerCommand(plugin));
        commands.add(new SpawnMobCommand(plugin));
        commands.add(new TpsCommand(plugin));
        commands.add(new FixChunkCommand(plugin));
        commands.add(new LfixCommand(plugin));
        commands.add(new UnloadChunksCommand(plugin));
        commands.add(new GroundCleanCommand(plugin));
        commands.add(new ReplaceBlockCommand(plugin));
        commands.add(new ScanCommand(plugin));
        commands.add(new SearchCommand(plugin));
        commands.add(new SeCommand(plugin));
        commands.add(new BlockInfoCommand(plugin));
        commands.add(new BlockNbtCommand(plugin));
        commands.add(new EntityInfoCommand(plugin));
        commands.add(new EntityNbtCommand(plugin));
        commands.add(new IfOfflineCommand(plugin));
        commands.add(new IfOnlineCommand(plugin));
        commands.add(new MigrateDatabaseCommand(plugin));
        commands.add(new ImportFromCommand(plugin));
        commands.add(new ImportOldUsersCommand(plugin));
        commands.add(new ReloadCommand(plugin));
        commands.add(new UserMetaCommand(plugin));
        commands.add(new GiveCommand(plugin));
        commands.add(new GiveAllCommand(plugin));
        commands.add(new BlockCyclingCommand(plugin));
        commands.add(new SilentChestCommand(plugin));
        commands.add(new ClearCommand(plugin));
        commands.add(new InvListCommand(plugin));
        commands.add(new InvLoadCommand(plugin));
        commands.add(new InvRemoveCommand(plugin));
        commands.add(new InvSaveCommand(plugin));
        commands.add(new BackupCommand(plugin));
        return commands;
    }
}
