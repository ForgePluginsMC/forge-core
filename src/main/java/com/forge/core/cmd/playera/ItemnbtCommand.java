package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Inspect the held item's serialized data (NBT-style view). */
public final class ItemnbtCommand extends PlayerACommand {
    private static final int MAX_LINES = 15;
    private static final int MAX_VALUE = 80;

    public ItemnbtCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "itemnbt";
    }

    @Override
    public String description() {
        return "Inspect the held item's data.";
    }

    @Override
    public String usage() {
        return "/itemnbt";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            Text.error(sender, "Hold an item to inspect.");
            return;
        }
        Text.send(sender, "<gold><bold>Item data</bold></gold>");
        Map<String, Object> data = item.serialize();
        int lines = 0;
        for (var entry : data.entrySet()) {
            if (lines >= MAX_LINES) {
                Text.send(sender, "<gray>…and more</gray>");
                break;
            }
            lines++;
            Text.send(sender, "<gray>" + Text.escape(entry.getKey()) + ": <white>"
                    + Text.escape(shorten(String.valueOf(entry.getValue()))) + "</white>");
        }
        var keys = item.getItemMeta().getPersistentDataContainer().getKeys();
        if (!keys.isEmpty()) {
            Text.send(sender, "<gray>Persistent data keys: <white>" + keys.size() + "</white>");
        }
    }

    private static String shorten(String value) {
        return value.length() > MAX_VALUE ? value.substring(0, MAX_VALUE) + "…" : value;
    }
}
