package com.forge.core.cmd.admin;

import com.forge.core.command.CommandRegistry.CommandFailure;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

/**
 * Shared parsing helpers for admin commands. Everything here throws
 * {@link CommandFailure} on bad input so call sites stay small.
 */
public final class AdminUtil {
    private static final List<String> MATERIAL_NAMES;
    private static final List<String> ENTITY_NAMES;

    static {
        List<String> materials = new ArrayList<>();
        for (Material material : Material.values()) {
            if (!material.isAir()) {
                materials.add(material.name().toLowerCase(Locale.ROOT));
            }
        }
        MATERIAL_NAMES = List.copyOf(materials);
        List<String> entities = new ArrayList<>();
        for (EntityType type : EntityType.values()) {
            if (type.isSpawnable()) {
                entities.add(type.name().toLowerCase(Locale.ROOT));
            }
        }
        ENTITY_NAMES = List.copyOf(entities);
    }

    private AdminUtil() {
    }

    /** Parse a material name; never air, never null. */
    public static Material material(String input) {
        Material material = Material.matchMaterial(input);
        if (material == null || material.isAir()) {
            throw new CommandFailure("Unknown material: " + input);
        }
        return material;
    }

    /** Parse a spawnable entity type name. */
    public static EntityType entityType(String input) {
        try {
            EntityType type = EntityType.valueOf(input.toUpperCase(Locale.ROOT));
            if (!type.isSpawnable()) {
                throw new CommandFailure("That entity type cannot be spawned: " + input);
            }
            return type;
        } catch (IllegalArgumentException bad) {
            throw new CommandFailure("Unknown entity type: " + input);
        }
    }

    /** Parse an int clamped to a range, with a friendly error. */
    public static int intInRange(String input, int min, int max, String what) {
        int value;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException bad) {
            throw new CommandFailure(what + " must be a number, got: " + input);
        }
        if (value < min || value > max) {
            throw new CommandFailure(what + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    /** Lower-case material names for tab completion. */
    public static List<String> materialNames() {
        return MATERIAL_NAMES;
    }

    /** Lower-case spawnable entity type names for tab completion. */
    public static List<String> entityNames() {
        return ENTITY_NAMES;
    }
}
