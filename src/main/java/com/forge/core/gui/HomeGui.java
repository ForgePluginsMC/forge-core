package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Home manager: click to teleport, shift-friendly buttons to set/delete.
 */
@NullMarked
public final class HomeGui extends ForgeGui {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final ForgeCore plugin;
    private final ForgeGui parent;

    public HomeGui(ForgeCore plugin, ForgeGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected Component title() {
        return MM.deserialize("<green><bold>My Homes");
    }

    @Override
    protected int size() {
        return 54;
    }

    @Override
    protected @Nullable ForgeGui parent() {
        return parent;
    }

    @Override
    protected void build(Player viewer) {
        var homes = plugin.users().get(viewer).homes();
        List<String> names = new ArrayList<>(homes.keySet());
        names.sort(String.CASE_INSENSITIVE_ORDER);

        int[] slots = gridSlots();
        for (int i = 0; i < names.size() && i < slots.length; i++) {
            String name = names.get(i);
            Location loc = homes.get(name);
            String where = loc.getWorld() == null ? "unknown"
                    : loc.getWorld().getName() + " " + loc.getBlockX() + ", " + loc.getBlockY()
                            + ", " + loc.getBlockZ();
            set(slots[i], GuiItem.of(Material.RED_BED)
                    .name("<green>" + Text.escape(name))
                    .lore(
                            "<gray>" + Text.escape(where),
                            "",
                            "<green>Click to teleport",
                            "<red>Right-click to delete")
                    .action(p -> runCommand(p, "home " + name)));
        }

        set(49, GuiItem.of(Material.OAK_SIGN)
                .name("<yellow>Set Home")
                .lore("<gray>Create a home at your location.", "", "<yellow>Click to enter name")
                .action(p -> runCommandWithInput(p, "Type the home name:", "sethome")));
    }

    private static int[] gridSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                slots.add(row * 9 + col);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }
}
