package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.admin.InventoryStore.SavedInventory;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/** /invlist — list saved inventory snapshots. */
public final class InvListCommand extends ForgeCommand {
    public InvListCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "invlist";
    }

    @Override
    public String description() {
        return "List saved inventory snapshots.";
    }

    @Override
    public String usage() {
        return "/invlist [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        String ownerFilter = null;
        if (args.length >= 1) {
            OfflinePlayer offline = Players.offline(args[0]);
            if (offline == null) {
                Text.error(sender, "Unknown player: " + Text.escape(args[0]));
                return;
            }
            ownerFilter = offline.getUniqueId().toString();
        }
        List<SavedInventory> shown = new ArrayList<>();
        for (SavedInventory saved : AdminSetup.inventories().all()) {
            if (ownerFilter == null || saved.owner().toString().equals(ownerFilter)) {
                shown.add(saved);
            }
        }
        if (shown.isEmpty()) {
            Text.send(sender, "<gray>No saved inventories"
                    + (ownerFilter == null ? "" : " for that player") + ".</gray>");
            return;
        }
        Text.send(sender, "<gold>Saved inventories (" + shown.size() + "):</gold>");
        for (SavedInventory saved : shown) {
            String ownerName = Bukkit.getOfflinePlayer(saved.owner()).getName();
            Text.send(sender, "  <gray>•</gray> <white>" + Text.escape(saved.name()) + "</white>"
                    + " <gray>by</gray> <white>" + Text.escape(ownerName == null ? "?" : ownerName) + "</white>"
                    + " <gray>—</gray> <white>" + saved.itemCount() + "</white> <gray>items,</gray> "
                    + Time.formatDate(saved.savedAt()));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }
}
