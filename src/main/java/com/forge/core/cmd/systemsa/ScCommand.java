package com.forge.core.cmd.systemsa;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsa.sc.SignCopyManager;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sign copy mode: right-click a sign to copy its lines, right-click another
 * sign to paste them.
 *
 * <p>Usage: /sc
 */
public final class ScCommand extends ForgeCommand {
    public ScCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "sc";
    }

    @Override
    public String description() {
        return "Toggle sign copy/paste mode.";
    }

    @Override
    public String usage() {
        return "/sc";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        boolean on = SignCopyManager.get().toggle(player);
        if (on) {
            Text.ok(sender, "Sign copy mode <white>on</white> — right-click a sign to copy it.");
        } else {
            Text.send(sender, "Sign copy mode <white>off</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
