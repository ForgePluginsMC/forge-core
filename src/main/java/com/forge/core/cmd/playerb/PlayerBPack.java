package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Player social/fun pack registrar. Add one {@code new XxxCommand(plugin)} per command.
 *
 * <p>Commands in this pack: me, msg, reply, ignore, compass, tree, ride, sit,
 * suicide, workbench, ender, walkspeed, time, weather, servertime, colors,
 * colorlimits, ctext, editctext, book, getbook, preview, merchant, recipe,
 * note, dispose, clearender, actionbarmsg, titlemsg, sound, info, stats,
 * statsedit, status, version, placeholders, haspermission, checkperm.
 */
public final class PlayerBPack {
    private PlayerBPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        PlayerBSetup.init(plugin);
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new MeCommand(plugin));
        commands.add(new MsgCommand(plugin));
        commands.add(new ReplyCommand(plugin));
        commands.add(new IgnoreCommand(plugin));
        commands.add(new CompassCommand(plugin));
        commands.add(new TreeCommand(plugin));
        commands.add(new RideCommand(plugin));
        commands.add(new SitCommand(plugin));
        commands.add(new SuicideCommand(plugin));
        commands.add(new WorkbenchCommand(plugin));
        commands.add(new EnderCommand(plugin));
        commands.add(new WalkspeedCommand(plugin));
        commands.add(new TimeCommand(plugin));
        commands.add(new WeatherCommand(plugin));
        commands.add(new ServertimeCommand(plugin));
        commands.add(new ColorsCommand(plugin));
        commands.add(new ColorlimitsCommand(plugin));
        commands.add(new CtextCommand(plugin));
        commands.add(new EditctextCommand(plugin));
        commands.add(new BookCommand(plugin));
        commands.add(new GetbookCommand(plugin));
        commands.add(new PreviewCommand(plugin));
        commands.add(new MerchantCommand(plugin));
        commands.add(new RecipeCommand(plugin));
        commands.add(new NoteCommand(plugin));
        commands.add(new DisposeCommand(plugin));
        commands.add(new ClearenderCommand(plugin));
        commands.add(new ActionbarmsgCommand(plugin));
        commands.add(new TitlemsgCommand(plugin));
        commands.add(new SoundCommand(plugin));
        commands.add(new InfoCommand(plugin));
        commands.add(new StatsCommand(plugin));
        commands.add(new StatseditCommand(plugin));
        commands.add(new StatusCommand(plugin));
        commands.add(new VersionCommand(plugin));
        commands.add(new PlaceholdersCommand(plugin));
        commands.add(new HaspermissionCommand(plugin));
        commands.add(new CheckpermCommand(plugin));
        commands.add(new AnvilCommand(plugin));
        commands.add(new GrindstoneCommand(plugin));
        commands.add(new LoomCommand(plugin));
        commands.add(new SmithingtableCommand(plugin));
        commands.add(new StonecutterCommand(plugin));
        commands.add(new CartographytableCommand(plugin));
        commands.add(new MailCommand(plugin));
        commands.add(new MailallCommand(plugin));
        commands.add(new PaytoggleCommand(plugin));
        commands.add(new MsgtoggleCommand(plugin));
        commands.add(new RealnameCommand(plugin));
        commands.add(new ItemdbCommand(plugin));
        commands.add(new ToastCommand(plugin));
        commands.add(new SaveditemsCommand(plugin));
        return commands;
    }
}
