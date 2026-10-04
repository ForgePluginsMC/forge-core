package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.ArrayList;
import java.util.List;

/**
 * Economy pack registrar. Add one {@code new XxxCommand(plugin)} per command.
 *
 * <p>Commands in this pack: balance, baltop, money, cheque, sell, worth,
 * worthlist, setworth, generateworth, condense, uncondense, kit, kiteditor,
 * kitcdreset, votes, votetop, voteedit.
 */
public final class EconomyPack {
    private EconomyPack() {
    }

    public static List<ForgeCommand> commands(ForgeCore plugin) {
        EconomySetup.init(plugin);
        List<ForgeCommand> commands = new ArrayList<>();
        commands.add(new BalanceCommand(plugin));
        commands.add(new BaltopCommand(plugin));
        commands.add(new MoneyCommand(plugin));
        commands.add(new ChequeCommand(plugin));
        commands.add(new SellCommand(plugin));
        commands.add(new WorthCommand(plugin));
        commands.add(new WorthlistCommand(plugin));
        commands.add(new SetworthCommand(plugin));
        commands.add(new GenerateworthCommand(plugin));
        commands.add(new CondenseCommand(plugin));
        commands.add(new UncondenseCommand(plugin));
        commands.add(new KitCommand(plugin));
        commands.add(new KiteditorCommand(plugin));
        commands.add(new KitcdresetCommand(plugin));
        commands.add(new VotesCommand(plugin));
        commands.add(new VotetopCommand(plugin));
        commands.add(new VoteeditCommand(plugin));
        return commands;
    }
}
