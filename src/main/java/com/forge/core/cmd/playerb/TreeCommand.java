package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /tree — grow a tree at the block you are looking at, using the vanilla
 * sapling + bone-meal mechanic (no deprecated world-generation calls).
 */
public final class TreeCommand extends ForgeCommand {
    private static final List<String> TYPES = Arrays.stream(TreeType.values())
            .map(type -> type.name().toLowerCase(Locale.ROOT))
            .sorted()
            .collect(Collectors.toList());

    /** Best-effort sapling for each tree type. */
    private static final Map<TreeType, Material> SAPLINGS = new EnumMap<>(TreeType.class);

    static {
        SAPLINGS.put(TreeType.TREE, Material.OAK_SAPLING);
        SAPLINGS.put(TreeType.BIG_TREE, Material.OAK_SAPLING);
        SAPLINGS.put(TreeType.SWAMP, Material.OAK_SAPLING);
        SAPLINGS.put(TreeType.POPLAR, Material.OAK_SAPLING);
        SAPLINGS.put(TreeType.REDWOOD, Material.SPRUCE_SAPLING);
        SAPLINGS.put(TreeType.TALL_REDWOOD, Material.SPRUCE_SAPLING);
        SAPLINGS.put(TreeType.MEGA_REDWOOD, Material.SPRUCE_SAPLING);
        SAPLINGS.put(TreeType.MEGA_PINE, Material.SPRUCE_SAPLING);
        SAPLINGS.put(TreeType.BIRCH, Material.BIRCH_SAPLING);
        SAPLINGS.put(TreeType.TALL_BIRCH, Material.BIRCH_SAPLING);
        SAPLINGS.put(TreeType.JUNGLE, Material.JUNGLE_SAPLING);
        SAPLINGS.put(TreeType.SMALL_JUNGLE, Material.JUNGLE_SAPLING);
        SAPLINGS.put(TreeType.COCOA_TREE, Material.JUNGLE_SAPLING);
        SAPLINGS.put(TreeType.JUNGLE_BUSH, Material.JUNGLE_SAPLING);
        SAPLINGS.put(TreeType.ACACIA, Material.ACACIA_SAPLING);
        SAPLINGS.put(TreeType.DARK_OAK, Material.DARK_OAK_SAPLING);
        SAPLINGS.put(TreeType.CHERRY, Material.CHERRY_SAPLING);
        SAPLINGS.put(TreeType.MANGROVE, Material.MANGROVE_PROPAGULE);
        SAPLINGS.put(TreeType.TALL_MANGROVE, Material.MANGROVE_PROPAGULE);
        SAPLINGS.put(TreeType.AZALEA, Material.AZALEA);
        SAPLINGS.put(TreeType.PALE_OAK, Material.PALE_OAK_SAPLING);
        SAPLINGS.put(TreeType.PALE_OAK_CREAKING, Material.PALE_OAK_SAPLING);
        SAPLINGS.put(TreeType.RED_MUSHROOM, Material.RED_MUSHROOM);
        SAPLINGS.put(TreeType.BROWN_MUSHROOM, Material.BROWN_MUSHROOM);
        SAPLINGS.put(TreeType.CRIMSON_FUNGUS, Material.CRIMSON_FUNGUS);
        SAPLINGS.put(TreeType.WARPED_FUNGUS, Material.WARPED_FUNGUS);
        SAPLINGS.put(TreeType.CHORUS_PLANT, Material.CHORUS_FLOWER);
    }

    public TreeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tree";
    }

    @Override
    public String description() {
        return "Grow a tree at the block you are looking at.";
    }

    @Override
    public String usage() {
        return "/tree [type]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        TreeType type = TreeType.TREE;
        if (args.length >= 1) {
            try {
                type = TreeType.valueOf(args[0].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException bad) {
                Text.error(sender, "Unknown tree type. Try: <gray>"
                        + String.join(", ", TYPES.subList(0, Math.min(8, TYPES.size()))) + "...</gray>");
                return;
            }
        }
        Material sapling = SAPLINGS.get(type);
        if (sapling == null) {
            Text.error(sender, "That tree type cannot be grown this way.");
            return;
        }
        Block target = player.getTargetBlockExact(8);
        if (target == null) {
            Text.error(sender, "Look at a block within 8 blocks.");
            return;
        }
        Block spot = target.getType() == Material.AIR ? target : target.getRelative(BlockFace.UP);
        if (spot.getType() != Material.AIR) {
            Text.error(sender, "There is no room to grow a tree there.");
            return;
        }
        spot.setType(sapling, false);
        boolean grown = false;
        for (int attempt = 0; attempt < 12 && !grown; attempt++) {
            spot.applyBoneMeal(BlockFace.UP);
            grown = spot.getType() != sapling;
        }
        if (grown) {
            Text.ok(sender, "Grew a <white>" + Text.escape(type.name().toLowerCase(Locale.ROOT).replace('_', ' '))
                    + "</white> tree.");
        } else {
            spot.setType(Material.AIR, false);
            Text.error(sender, "A tree would not grow there.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(TYPES, args);
        }
        return List.of();
    }
}
