package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle cuffed state: a cuffed player cannot move. */
public final class CuffCommand extends PlayerACommand {
    public CuffCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "cuff";
    }

    @Override
    public String description() {
        return "Cuff a player so they cannot move.";
    }

    @Override
    public String usage() {
        return "/cuff <player>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target = requiredTarget(sender, args);
        if (target == null) {
            return;
        }
        if (target.equals(sender)) {
            Text.error(sender, "You cannot cuff yourself.");
            return;
        }
        boolean cuffed = PlayerAState.cuff.toggle(target);
        Text.ok(sender, "<white>" + Text.escape(target.getName()) + "</white> has been "
                + (cuffed ? "<red>cuffed</red><green>." : "<green>uncuffed</green>."));
        Text.send(target, cuffed ? "<red>You have been cuffed." : "<green>You have been uncuffed.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
