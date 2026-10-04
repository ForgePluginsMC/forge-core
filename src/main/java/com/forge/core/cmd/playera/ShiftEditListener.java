package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Shift-right-click a sign to open the sign editor, for players who opted in
 * with {@code /toggleshiftedit}.
 */
final class ShiftEditListener implements Listener {
    private final ForgeCore plugin;

    ShiftEditListener(ForgeCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }
        if (!plugin.users().get(player).getBoolean("shift-edit", false)) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign sign)) {
            return;
        }
        event.setCancelled(true);
        player.openSign(sign, Side.FRONT);
    }
}
