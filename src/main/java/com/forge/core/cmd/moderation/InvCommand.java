package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Open another player's live inventory (edits apply for real). */
public final class InvCommand extends ForgeCommand {
    public InvCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "inv";
    }

    @Override
    public String description() {
        return "Open another player's live inventory.";
    }

    @Override
    public String usage() {
        return "/inv <player>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Player viewer = asPlayer(sender);
        if (viewer == null) {
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        viewer.openInventory(target.getInventory());
        Text.send(sender, "Opened <white>" + Text.escape(target.getName()) + "</white>'s inventory.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
