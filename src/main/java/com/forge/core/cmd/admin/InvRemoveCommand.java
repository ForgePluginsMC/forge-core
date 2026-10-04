package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.admin.InventoryStore.SavedInventory;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

/** /invremove — delete a saved inventory snapshot. */
public final class InvRemoveCommand extends ForgeCommand {
    public InvRemoveCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "invremove";
    }

    @Override
    public String description() {
        return "Delete a saved inventory snapshot.";
    }

    @Override
    public String usage() {
        return "/invremove <name>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        if (AdminSetup.inventories().remove(args[0])) {
            Text.ok(sender, "Deleted saved inventory <white>" + Text.escape(args[0]) + "</white>.");
        } else {
            Text.error(sender, "No saved inventory named <white>" + Text.escape(args[0]) + "</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            List<String> names = new ArrayList<>();
            for (SavedInventory saved : AdminSetup.inventories().all()) {
                names.add(saved.name());
            }
            return Players.filter(names, args);
        }
        return List.of();
    }
}
