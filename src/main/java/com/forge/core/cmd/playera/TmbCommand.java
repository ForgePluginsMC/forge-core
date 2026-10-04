package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle whether hostile mobs target you. */
public final class TmbCommand extends PlayerACommand {
    public TmbCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tmb";
    }

    @Override
    public String description() {
        return "Toggle whether mobs target you.";
    }

    @Override
    public String usage() {
        return "/tmb";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        boolean on = PlayerAState.tmb.toggle(player);
        Text.ok(sender, "Mobs will " + (on ? "<red>ignore</red><green>" : "<green>target</green>") + " you now.");
    }
}
