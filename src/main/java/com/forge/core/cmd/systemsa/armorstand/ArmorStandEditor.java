package com.forge.core.cmd.systemsa.armorstand;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.EulerAngle;
import org.jspecify.annotations.Nullable;

/**
 * In-GUI armor stand editor: cycle head/body/arm/leg poses, toggle arms,
 * base plate, size, visibility, gravity and invulnerability, and dye worn
 * leather armor with a dye on the cursor.
 */
public final class ArmorStandEditor implements Listener {
    private static final EulerAngle[] POSES = {
            new EulerAngle(0, 0, 0),
            new EulerAngle(-0.6, 0, 0),
            new EulerAngle(0.6, 0, 0),
            new EulerAngle(0, 0.6, 0),
            new EulerAngle(0, -0.6, 0),
            new EulerAngle(0, 0, 0.5),
    };

    private static final int HEAD = 10;
    private static final int BODY = 11;
    private static final int ARM_R = 12;
    private static final int ARM_L = 13;
    private static final int LEG_R = 14;
    private static final int LEG_L = 15;
    private static final int RESET = 16;
    private static final int T_ARMS = 28;
    private static final int T_BASE = 29;
    private static final int T_SMALL = 30;
    private static final int T_VISIBLE = 31;
    private static final int T_GRAVITY = 32;
    private static final int T_INVULN = 33;
    private static final int PAINT = 40;
    private static final int CLOSE = 49;

    private static @Nullable ArmorStandEditor instance;

    /** Global accessor. */
    public static ArmorStandEditor get() {
        if (instance == null) {
            throw new IllegalStateException("ArmorStandEditor not initialized");
        }
        return instance;
    }

    public ArmorStandEditor(ForgeCore plugin) {
        instance = this;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /** One open editor window, bound to a single armor stand. */
    private static final class Session implements InventoryHolder {
        final ArmorStand stand;
        @Nullable Inventory inventory;

        Session(ArmorStand stand) {
            this.stand = stand;
        }

        @Override
        public Inventory getInventory() {
            if (inventory == null) {
                throw new IllegalStateException("Session not attached");
            }
            return inventory;
        }
    }

    /** Open the editor for the given stand. */
    public void open(Player player, ArmorStand stand) {
        Session session = new Session(stand);
        Inventory inventory = Bukkit.createInventory(session, 54, Text.of("<gold>Armor Stand Editor"));
        session.inventory = inventory;
        refresh(session);
        player.openInventory(inventory);
    }

    /** Nearest armor stand within radius blocks, or null. */
    public static @Nullable ArmorStand nearest(Player player, double radius) {
        ArmorStand best = null;
        double bestDistance = radius * radius;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof ArmorStand stand) {
                double distance = entity.getLocation().distanceSquared(player.getLocation());
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = stand;
                }
            }
        }
        return best;
    }

    private static String state(boolean on) {
        return on ? "<green>ON" : "<red>OFF";
    }

    private static ItemStack button(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.of(name));
        List<Component> lore = new ArrayList<>();
        for (String line : loreLines) {
            lore.add(Text.of("<gray>" + line));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void refresh(Session session) {
        Inventory inventory = session.getInventory();
        ArmorStand stand = session.stand;
        inventory.setItem(HEAD, button(Material.CARVED_PUMPKIN, "<yellow>Head pose",
                "Click to cycle the head pose."));
        inventory.setItem(BODY, button(Material.LEATHER_CHESTPLATE, "<yellow>Body pose",
                "Click to cycle the body pose."));
        inventory.setItem(ARM_R, button(Material.STICK, "<yellow>Right arm pose",
                "Click to cycle the right arm pose."));
        inventory.setItem(ARM_L, button(Material.BONE, "<yellow>Left arm pose",
                "Click to cycle the left arm pose."));
        inventory.setItem(LEG_R, button(Material.LEATHER_LEGGINGS, "<yellow>Right leg pose",
                "Click to cycle the right leg pose."));
        inventory.setItem(LEG_L, button(Material.LEATHER_BOOTS, "<yellow>Left leg pose",
                "Click to cycle the left leg pose."));
        inventory.setItem(RESET, button(Material.MILK_BUCKET, "<yellow>Reset poses",
                "Click to straighten every pose."));
        inventory.setItem(T_ARMS, button(Material.ARMOR_STAND,
                "<yellow>Arms: " + state(stand.hasArms()), "Click to toggle arms."));
        inventory.setItem(T_BASE, button(Material.SMOOTH_STONE_SLAB,
                "<yellow>Base plate: " + state(stand.hasBasePlate()), "Click to toggle the base plate."));
        inventory.setItem(T_SMALL, button(Material.ENDER_PEARL,
                "<yellow>Small: " + state(stand.isSmall()), "Click to toggle small size."));
        inventory.setItem(T_VISIBLE, button(Material.GLASS,
                "<yellow>Visible: " + state(stand.isVisible()), "Click to toggle visibility."));
        inventory.setItem(T_GRAVITY, button(Material.FEATHER,
                "<yellow>Gravity: " + state(stand.hasGravity()), "Click to toggle gravity."));
        inventory.setItem(T_INVULN, button(Material.BEDROCK,
                "<yellow>Invulnerable: " + state(stand.isInvulnerable()), "Click to toggle invulnerability."));
        inventory.setItem(PAINT, button(Material.WHITE_DYE, "<yellow>Armor paint",
                "Hold any dye on your cursor",
                "and click here to dye the stand's",
                "worn leather armor."));
        inventory.setItem(CLOSE, button(Material.BARRIER, "<red>Close",
                "Click to close the editor."));
    }

    private static int poseIndex(EulerAngle current) {
        for (int i = 0; i < POSES.length; i++) {
            EulerAngle pose = POSES[i];
            if (Double.compare(pose.getX(), current.getX()) == 0
                    && Double.compare(pose.getY(), current.getY()) == 0
                    && Double.compare(pose.getZ(), current.getZ()) == 0) {
                return i;
            }
        }
        return -1;
    }

    private static EulerAngle nextPose(EulerAngle current) {
        return POSES[(poseIndex(current) + 1) % POSES.length];
    }

    private void paint(Player player, ArmorStand stand, ItemStack cursor) {
        if (cursor.getType().isAir() || !Tag.ITEMS_DYES.isTagged(cursor.getType())) {
            Text.error(player, "Hold a dye on your cursor, then click the paint button.");
            return;
        }
        DyeColor dye;
        try {
            dye = DyeColor.valueOf(cursor.getType().name().replace("_DYE", ""));
        } catch (IllegalArgumentException exception) {
            Text.error(player, "That dye cannot be used for painting.");
            return;
        }
        Color color = dye.getColor();
        EntityEquipment equipment = stand.getEquipment();
        if (equipment == null) {
            return;
        }
        boolean dyed = false;
        ItemStack[] pieces = {
                equipment.getHelmet(), equipment.getChestplate(),
                equipment.getLeggings(), equipment.getBoots()};
        for (ItemStack piece : pieces) {
            if (piece != null && !piece.getType().isAir()
                    && piece.getItemMeta() instanceof LeatherArmorMeta leather) {
                leather.setColor(color);
                piece.setItemMeta(leather);
                dyed = true;
            }
        }
        if (dyed) {
            cursor.setAmount(cursor.getAmount() - 1);
            Text.ok(player, "Dyed the stand's leather armor "
                    + dye.name().toLowerCase(Locale.ROOT).replace('_', ' ') + ".");
        } else {
            Text.error(player, "The stand is not wearing any leather armor.");
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Session session)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ArmorStand stand = session.stand;
        if (!stand.isValid()) {
            player.closeInventory();
            Text.error(player, "That armor stand is gone.");
            return;
        }
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= session.getInventory().getSize()) {
            return;
        }
        switch (slot) {
            case HEAD -> stand.setHeadPose(nextPose(stand.getHeadPose()));
            case BODY -> stand.setBodyPose(nextPose(stand.getBodyPose()));
            case ARM_R -> stand.setRightArmPose(nextPose(stand.getRightArmPose()));
            case ARM_L -> stand.setLeftArmPose(nextPose(stand.getLeftArmPose()));
            case LEG_R -> stand.setRightLegPose(nextPose(stand.getRightLegPose()));
            case LEG_L -> stand.setLeftLegPose(nextPose(stand.getLeftLegPose()));
            case RESET -> {
                stand.setHeadPose(POSES[0]);
                stand.setBodyPose(POSES[0]);
                stand.setRightArmPose(POSES[0]);
                stand.setLeftArmPose(POSES[0]);
                stand.setRightLegPose(POSES[0]);
                stand.setLeftLegPose(POSES[0]);
            }
            case T_ARMS -> stand.setArms(!stand.hasArms());
            case T_BASE -> stand.setBasePlate(!stand.hasBasePlate());
            case T_SMALL -> stand.setSmall(!stand.isSmall());
            case T_VISIBLE -> stand.setVisible(!stand.isVisible());
            case T_GRAVITY -> stand.setGravity(!stand.hasGravity());
            case T_INVULN -> stand.setInvulnerable(!stand.isInvulnerable());
            case PAINT -> paint(player, stand, event.getCursor());
            case CLOSE -> {
                player.closeInventory();
                return;
            }
            default -> {
                return;
            }
        }
        refresh(session);
    }
}
