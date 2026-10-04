package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Implements {@code /blockcycling}: while a player has the toggle on,
 * right-clicking a block cycles it through its variant group (stone types,
 * log types, wool colors, …). Orientation (axis/facing) is preserved.
 */
public final class BlockCycleListener implements Listener {
    private final ForgeCore plugin;

    BlockCycleListener(ForgeCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || CycleGroups.next(block.getType()) == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!plugin.users().get(player).getBoolean("blockcycling", false)) {
            return;
        }
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        cycle(block);
    }

    private static void cycle(Block block) {
        Material next = CycleGroups.next(block.getType());
        if (next == null) {
            return;
        }
        BlockData old = block.getBlockData();
        block.setType(next, false);
        BlockData created = block.getBlockData();
        if (old instanceof Orientable oldO && created instanceof Orientable newO) {
            newO.setAxis(oldO.getAxis());
        } else if (old instanceof Directional oldD && created instanceof Directional newD) {
            newD.setFacing(oldD.getFacing());
        }
        block.setBlockData(created);
    }

    /** Variant groups; each material maps to the next one in its group. */
    static final class CycleGroups {
        private static final Map<Material, Material> NEXT = new LinkedHashMap<>();

        static {
            group("STONE", "GRANITE", "DIORITE", "ANDESITE");
            group("POLISHED_GRANITE", "POLISHED_DIORITE", "POLISHED_ANDESITE");
            group("OAK_LOG", "SPRUCE_LOG", "BIRCH_LOG", "JUNGLE_LOG", "ACACIA_LOG",
                    "DARK_OAK_LOG", "MANGROVE_LOG", "CHERRY_LOG", "BAMBOO_BLOCK");
            group("OAK_PLANKS", "SPRUCE_PLANKS", "BIRCH_PLANKS", "JUNGLE_PLANKS", "ACACIA_PLANKS",
                    "DARK_OAK_PLANKS", "MANGROVE_PLANKS", "CHERRY_PLANKS", "BAMBOO_PLANKS");
            group("STONE_BRICKS", "MOSSY_STONE_BRICKS", "CRACKED_STONE_BRICKS", "CHISELED_STONE_BRICKS");
            group("SANDSTONE", "CHISELED_SANDSTONE", "CUT_SANDSTONE", "SMOOTH_SANDSTONE");
            group("RED_SANDSTONE", "CHISELED_RED_SANDSTONE", "CUT_RED_SANDSTONE", "SMOOTH_RED_SANDSTONE");
            group("QUARTZ_BLOCK", "CHISELED_QUARTZ_BLOCK", "QUARTZ_PILLAR", "SMOOTH_QUARTZ_BLOCK");
            group("PRISMARINE", "PRISMARINE_BRICKS", "DARK_PRISMARINE");
            group("COPPER_BLOCK", "EXPOSED_COPPER", "WEATHERED_COPPER", "OXIDIZED_COPPER");
            group("CUT_COPPER", "EXPOSED_CUT_COPPER", "WEATHERED_CUT_COPPER", "OXIDIZED_CUT_COPPER");
            group("COBBLED_DEEPSLATE", "POLISHED_DEEPSLATE", "DEEPSLATE_BRICKS", "DEEPSLATE_TILES");
            group("WHITE_WOOL", "LIGHT_GRAY_WOOL", "GRAY_WOOL", "BLACK_WOOL", "BROWN_WOOL",
                    "RED_WOOL", "ORANGE_WOOL", "YELLOW_WOOL", "LIME_WOOL", "GREEN_WOOL",
                    "CYAN_WOOL", "LIGHT_BLUE_WOOL", "BLUE_WOOL", "PURPLE_WOOL", "MAGENTA_WOOL", "PINK_WOOL");
            group("WHITE_CONCRETE", "LIGHT_GRAY_CONCRETE", "GRAY_CONCRETE", "BLACK_CONCRETE", "BROWN_CONCRETE",
                    "RED_CONCRETE", "ORANGE_CONCRETE", "YELLOW_CONCRETE", "LIME_CONCRETE", "GREEN_CONCRETE",
                    "CYAN_CONCRETE", "LIGHT_BLUE_CONCRETE", "BLUE_CONCRETE", "PURPLE_CONCRETE",
                    "MAGENTA_CONCRETE", "PINK_CONCRETE");
            group("WHITE_CONCRETE_POWDER", "LIGHT_GRAY_CONCRETE_POWDER", "GRAY_CONCRETE_POWDER",
                    "BLACK_CONCRETE_POWDER", "BROWN_CONCRETE_POWDER", "RED_CONCRETE_POWDER",
                    "ORANGE_CONCRETE_POWDER", "YELLOW_CONCRETE_POWDER", "LIME_CONCRETE_POWDER",
                    "GREEN_CONCRETE_POWDER", "CYAN_CONCRETE_POWDER", "LIGHT_BLUE_CONCRETE_POWDER",
                    "BLUE_CONCRETE_POWDER", "PURPLE_CONCRETE_POWDER", "MAGENTA_CONCRETE_POWDER",
                    "PINK_CONCRETE_POWDER");
            group("TERRACOTTA", "WHITE_TERRACOTTA", "LIGHT_GRAY_TERRACOTTA", "GRAY_TERRACOTTA",
                    "BLACK_TERRACOTTA", "BROWN_TERRACOTTA", "RED_TERRACOTTA", "ORANGE_TERRACOTTA",
                    "YELLOW_TERRACOTTA", "LIME_TERRACOTTA", "GREEN_TERRACOTTA", "CYAN_TERRACOTTA",
                    "LIGHT_BLUE_TERRACOTTA", "BLUE_TERRACOTTA", "PURPLE_TERRACOTTA",
                    "MAGENTA_TERRACOTTA", "PINK_TERRACOTTA");
            group("GLASS", "WHITE_STAINED_GLASS", "LIGHT_GRAY_STAINED_GLASS", "GRAY_STAINED_GLASS",
                    "BLACK_STAINED_GLASS", "BROWN_STAINED_GLASS", "RED_STAINED_GLASS",
                    "ORANGE_STAINED_GLASS", "YELLOW_STAINED_GLASS", "LIME_STAINED_GLASS",
                    "GREEN_STAINED_GLASS", "CYAN_STAINED_GLASS", "LIGHT_BLUE_STAINED_GLASS",
                    "BLUE_STAINED_GLASS", "PURPLE_STAINED_GLASS", "MAGENTA_STAINED_GLASS",
                    "PINK_STAINED_GLASS");
        }

        private static void group(String... names) {
            List<Material> materials = new ArrayList<>();
            for (String name : names) {
                Material material = Material.matchMaterial(name);
                if (material != null) {
                    materials.add(material);
                }
            }
            for (int i = 0; i < materials.size(); i++) {
                NEXT.put(materials.get(i), materials.get((i + 1) % materials.size()));
            }
        }

        /** Next variant, or null when the material is not in any group. */
        static Material next(Material material) {
            return NEXT.get(material);
        }
    }
}
