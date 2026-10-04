package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Player self/state pack registrar.
 *
 * <p>Commands in this pack: afk, fly, flyspeed, tfly, god, tgod, heal, feed,
 * hunger, saturation, exp, checkexp, effect, enchant, disableenchant, repair,
 * repaircost, more, hat, head, iteminfo, itemlore, itemname, itemnbt,
 * unbreakable, hideflags, glow, nick, ptime, pweather, ping, maxhp, air,
 * cplaytime, playtime, playtimetop, tmb, toggleshiftedit, toggletotem,
 * tagtoggle, tablistupdate, shakeitoff, cuff.
 */
public final class PlayerAPack {
    private PlayerAPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        PlayerASetup.init(plugin);
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new AfkCommand(plugin));
        commands.add(new FlyCommand(plugin));
        commands.add(new FlyspeedCommand(plugin));
        commands.add(new TflyCommand(plugin));
        commands.add(new GodCommand(plugin));
        commands.add(new TgodCommand(plugin));
        commands.add(new HealCommand(plugin));
        commands.add(new FeedCommand(plugin));
        commands.add(new HungerCommand(plugin));
        commands.add(new SaturationCommand(plugin));
        commands.add(new ExpCommand(plugin));
        commands.add(new CheckexpCommand(plugin));
        commands.add(new EffectCommand(plugin));
        commands.add(new EnchantCommand(plugin));
        commands.add(new DisableenchantCommand(plugin));
        commands.add(new RepairCommand(plugin));
        commands.add(new RepaircostCommand(plugin));
        commands.add(new MoreCommand(plugin));
        commands.add(new HatCommand(plugin));
        commands.add(new HeadCommand(plugin));
        commands.add(new IteminfoCommand(plugin));
        commands.add(new ItemloreCommand(plugin));
        commands.add(new ItemnameCommand(plugin));
        commands.add(new ItemnbtCommand(plugin));
        commands.add(new UnbreakableCommand(plugin));
        commands.add(new HideflagsCommand(plugin));
        commands.add(new GlowCommand(plugin));
        commands.add(new NickCommand(plugin));
        commands.add(new PtimeCommand(plugin));
        commands.add(new PweatherCommand(plugin));
        commands.add(new PingCommand(plugin));
        commands.add(new MaxhpCommand(plugin));
        commands.add(new AirCommand(plugin));
        commands.add(new CplaytimeCommand(plugin));
        commands.add(new PlaytimeCommand(plugin));
        commands.add(new PlaytimetopCommand(plugin));
        commands.add(new TmbCommand(plugin));
        commands.add(new ToggleshifteditCommand(plugin));
        commands.add(new ToggletotemCommand(plugin));
        commands.add(new TagtoggleCommand(plugin));
        commands.add(new ShakeitoffCommand(plugin));
        commands.add(new CuffCommand(plugin));
        return commands;
    }
}
