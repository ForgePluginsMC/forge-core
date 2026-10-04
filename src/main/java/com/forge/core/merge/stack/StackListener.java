package com.forge.core.merge.stack;

import java.util.Set;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/** Wires Bukkit events into the stack managers. */
final class StackListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!StackSetup.settings().enabled()) {
            return;
        }
        Set<CreatureSpawnEvent.SpawnReason> skipped = StackSetup.settings().skipSpawnReasons();
        if (skipped.contains(event.getSpawnReason())) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        if (StackSetup.stacks().tryMergeSpawn(living, event.getSpawnReason())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!StackSetup.settings().enabled()) {
            return;
        }
        if (event.getEntity() instanceof LivingEntity) {
            StackSetup.stacks().onDeath(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (!StackSetup.settings().enabled()) {
            return;
        }
        StackSetup.spawners().onSpawnerSpawn(event);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!StackSetup.settings().enabled()) {
            return;
        }
        StackSetup.spawners().onPlace(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!StackSetup.settings().enabled()) {
            return;
        }
        StackSetup.spawners().onBreak(event);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!StackSetup.settings().enabled()) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            StackSetup.spawners().onEggConvert(event);
        }
    }
}
