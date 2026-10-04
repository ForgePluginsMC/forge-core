package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Redeems ForgeCore cheques on right-click. The cheque's value lives in its
 * {@link org.bukkit.persistence.PersistentDataContainer}; the item is
 * consumed on redeem so a value can never be claimed twice.
 */
public final class ChequeListener implements Listener {
    private final ForgeCore plugin;
    private final NamespacedKey valueKey;

    public ChequeListener(ForgeCore plugin) {
        this.plugin = plugin;
        this.valueKey = new NamespacedKey(plugin, "cheque-value");
    }

    /** Build a cheque item worth {@code amount}. */
    public static ItemStack create(ForgeCore plugin, double amount) {
        ItemStack cheque = new ItemStack(org.bukkit.Material.PAPER);
        ItemMeta meta = cheque.getItemMeta();
        meta.displayName(Text.of("<gold>Cheque <gray>— <green>" + plugin.economy().format(amount) + "</green>"));
        meta.lore(List.of(
                Text.of("<gray>Right-click to redeem"),
                Text.of("<green>" + plugin.economy().format(amount) + "</green>")));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "cheque-value"), PersistentDataType.DOUBLE, amount);
        cheque.setItemMeta(meta);
        return cheque;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() != org.bukkit.Material.PAPER) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(valueKey, PersistentDataType.DOUBLE)) {
            return;
        }
        Double value = meta.getPersistentDataContainer().get(valueKey, PersistentDataType.DOUBLE);
        if (value == null || value <= 0) {
            return;
        }
        Player player = event.getPlayer();
        // Consume one cheque from the hand that was used.
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            ItemStack offhand = player.getInventory().getItemInOffHand();
            offhand.setAmount(offhand.getAmount() - 1);
        } else {
            ItemStack main = player.getInventory().getItemInMainHand();
            main.setAmount(main.getAmount() - 1);
        }
        plugin.economy().add(player.getUniqueId(), value);
        Text.ok(player, "Redeemed cheque for <green>" + plugin.economy().format(value) + "</green>. "
                + "Balance: <green>" + plugin.economy().format(plugin.economy().get(player.getUniqueId())) + "</green>.");
        event.setCancelled(true);
    }
}
