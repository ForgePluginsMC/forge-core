package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

/** Toggle a server-wide disabled enchantment (blocks enchanting tables and anvils). */
public final class DisableenchantCommand extends PlayerACommand {
    public DisableenchantCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "disableenchant";
    }

    @Override
    public String description() {
        return "Toggle a server-wide disabled enchantment.";
    }

    @Override
    public String usage() {
        return "/disableenchant <enchantment>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        Enchantment enchantment = Enchants.find(args[0]);
        if (enchantment == null) {
            Text.error(sender, "Unknown enchantment <white>" + Text.escape(args[0]) + "</white>.");
            return;
        }
        boolean nowDisabled = PlayerAState.disabledEnchants.toggle(enchantment);
        Text.ok(sender, "<white>" + Text.escape(enchantment.getKey().toString()) + "</white> is now "
                + (nowDisabled ? "<red>disabled</red><green>." : "<green>enabled</green>."));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Enchants.keys(), args);
        }
        return List.of();
    }
}
