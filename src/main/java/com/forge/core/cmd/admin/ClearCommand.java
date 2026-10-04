package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry.CommandFailure;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/** /clear — clear an inventory, optionally only one material. */
public final class ClearCommand extends ForgeCommand {
    public ClearCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "clear";
    }

    @Override
    public List<String> aliases() {
        return List.of("ci");
    }

    @Override
    public String description() {
        return "Clear an inventory, optionally one material.";
    }

    @Override
    public String usage() {
        return "/clear [player] [material]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target;
        Material material = null;
        if (args.length == 0) {
            target = asPlayer(sender);
            if (target == null) {
                Text.usage(sender, usage());
                return;
            }
        } else if (args.length == 1) {
            Player named = Players.findQuiet(args[0]);
            if (named != null) {
                requireOthers(sender);
                target = named;
            } else {
                target = asPlayer(sender);
                if (target == null) {
                    Text.usage(sender, usage());
                    return;
                }
                material = AdminUtil.material(args[0]);
            }
        } else {
            requireOthers(sender);
            target = Players.find(sender, args[0]);
            if (target == null) {
                return;
            }
            material = AdminUtil.material(args[1]);
        }

        int removed = clear(target.getInventory(), material);
        if (material == null) {
            Text.ok(sender, "Cleared <white>" + Text.escape(target.getName()) + "</white>'s inventory.");
        } else {
            Text.ok(sender, "Removed <white>" + removed + "</white> × "
                    + material.name().toLowerCase(Locale.ROOT) + " from <white>"
                    + Text.escape(target.getName()) + "</white>.");
        }
        if (!target.equals(sender)) {
            Text.send(target, "Your inventory was cleared.");
        }
    }

    private void requireOthers(CommandSender sender) {
        if (!sender.hasPermission("forgecore.clear.others")) {
            throw new CommandFailure("You don't have permission to clear other players' inventories.");
        }
    }

    /** Clear the inventory; returns stacks removed when a material is given. */
    private static int clear(PlayerInventory inventory, Material material) {
        if (material == null) {
            inventory.clear();
            return 0;
        }
        int removed = 0;
        ItemStack[] contents = inventory.getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].getType() == material) {
                removed += contents[i].getAmount();
                contents[i] = null;
            }
        }
        inventory.setStorageContents(contents);
        ItemStack[] armor = inventory.getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null && armor[i].getType() == material) {
                removed += armor[i].getAmount();
                armor[i] = null;
            }
        }
        inventory.setArmorContents(armor);
        ItemStack offhand = inventory.getItemInOffHand();
        if (offhand.getType() == material) {
            removed += offhand.getAmount();
            inventory.setItemInOffHand(ItemStack.empty());
        }
        return removed;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> candidates = new java.util.ArrayList<>(Players.onlineNames());
            candidates.addAll(AdminUtil.materialNames());
            return Players.filter(candidates, args);
        }
        if (args.length == 2) {
            return Players.filter(AdminUtil.materialNames(), args);
        }
        return List.of();
    }
}
