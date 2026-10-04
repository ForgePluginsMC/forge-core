package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/** Open a read-only copy of another player's inventory. */
public final class InvcheckCommand extends ForgeCommand {
    public InvcheckCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "invcheck";
    }

    @Override
    public String description() {
        return "View another player's inventory (read-only).";
    }

    @Override
    public String usage() {
        return "/invcheck <player>";
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
        Inventory view = Bukkit.createInventory(new ReadOnlyHolder(),
                45, Text.of("<gray>Inventory: <white>" + Text.escape(target.getName())));
        ItemStack[] storage = target.getInventory().getStorageContents();
        for (int slot = 0; slot < storage.length && slot < 36; slot++) {
            view.setItem(slot, storage[slot]);
        }
        ItemStack[] armor = target.getInventory().getArmorContents();
        for (int slot = 0; slot < armor.length && slot < 4; slot++) {
            view.setItem(36 + slot, armor[slot]);
        }
        view.setItem(40, target.getInventory().getItemInOffHand());
        InvViewGuard.watch(view);
        viewer.openInventory(view);
        Text.send(sender, "Viewing <white>" + Text.escape(target.getName()) + "</white>'s inventory (read-only).");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        return List.of();
    }

    /** Marker holder so the guard can recognise read-only views. */
    private static final class ReadOnlyHolder implements InventoryHolder {
        @Override
        public @Nullable Inventory getInventory() {
            return null;
        }
    }
}
