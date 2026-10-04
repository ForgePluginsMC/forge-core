package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** /give — give items to a player (overflow drops at their feet). */
public final class GiveCommand extends ForgeCommand {
    public GiveCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "give";
    }

    @Override
    public String description() {
        return "Give items to a player.";
    }

    @Override
    public String usage() {
        return "/give <player> <item> [amount]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        Player target = Players.find(sender, args[0]);
        if (target == null) {
            return;
        }
        Material material = AdminUtil.material(args[1]);
        int amount = args.length >= 3 ? AdminUtil.intInRange(args[2], 1, 2304, "Amount") : 1;

        int given = give(target, material, amount);
        Text.ok(sender, "Gave <white>" + given + " × "
                + material.name().toLowerCase(Locale.ROOT) + "</white> to <white>"
                + Text.escape(target.getName()) + "</white>.");
        if (!target.equals(sender)) {
            Text.send(target, "You received <white>" + given + " × "
                    + material.name().toLowerCase(Locale.ROOT) + "</white>.");
        }
    }

    /** Give items, dropping overflow naturally. Returns the total given. */
    static int give(Player target, Material material, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            int stackSize = Math.min(remaining, material.getMaxStackSize());
            ItemStack stack = new ItemStack(material, stackSize);
            Map<Integer, ItemStack> leftover = target.getInventory().addItem(stack);
            for (ItemStack extra : leftover.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), extra);
            }
            remaining -= stackSize;
        }
        return amount;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(Players.onlineNames(), args);
        }
        if (args.length == 2) {
            return Players.filter(AdminUtil.materialNames(), args);
        }
        return List.of();
    }
}
