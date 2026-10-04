package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.admin.InventoryStore.SavedInventory;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /invload — restore a saved inventory to yourself or another player. */
public final class InvLoadCommand extends ForgeCommand {
    public InvLoadCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "invload";
    }

    @Override
    public String description() {
        return "Restore a saved inventory.";
    }

    @Override
    public String usage() {
        return "/invload <name> [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        SavedInventory saved = AdminSetup.inventories().get(args[0]);
        if (saved == null) {
            Text.error(sender, "No saved inventory named <white>" + Text.escape(args[0]) + "</white>.");
            return;
        }
        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("forgecore.invload.others")) {
                Text.error(sender, "You don't have permission to load inventories for others.");
                return;
            }
            target = Players.find(sender, args[1]);
            if (target == null) {
                return;
            }
        } else {
            target = asPlayer(sender);
            if (target == null) {
                Text.usage(sender, usage());
                return;
            }
        }
        InventoryStore.apply(target.getInventory(), saved);
        Text.ok(sender, "Loaded inventory <white>" + Text.escape(saved.name()) + "</white> for <white>"
                + Text.escape(target.getName()) + "</white>.");
        if (!target.equals(sender)) {
            Text.send(target, "Your inventory was replaced from snapshot <white>"
                    + Text.escape(saved.name()) + "</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            for (SavedInventory saved : AdminSetup.inventories().all()) {
                names.add(saved.name());
            }
            return Players.filter(names, args);
        }
        if (args.length == 2 && sender.hasPermission("forgecore.invload.others")) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
