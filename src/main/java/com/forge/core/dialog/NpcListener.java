package com.forge.core.dialog;

import com.forge.core.ForgeCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Opens an NPC's dialog when a player right-clicks it.
 */
@NullMarked
public final class NpcListener implements Listener {
    private final ForgeCore plugin;

    public NpcListener(ForgeCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        NpcManager npcs = plugin.npcs();
        @Nullable String npcId = npcs.npcIdFor(event.getRightClicked());
        if (npcId == null) {
            return;
        }
        event.setCancelled(true);
        NpcManager.Npc npc = npcs.get(npcId);
        if (npc == null || npc.dialogId() == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!plugin.dialogs().show(npc.dialogId(), player)) {
            plugin.getLogger().warning("NPC '" + npcId + "' references missing dialog '"
                    + npc.dialogId() + "'");
        }
    }
}
