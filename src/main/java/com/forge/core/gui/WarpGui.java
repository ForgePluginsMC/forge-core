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
 * Warp browser: click a warp to teleport.
 */
@NullMarked
public final class WarpGui extends WebGui {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final ForgeCore plugin;
    private final WebGui parent;

    public WarpGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Teleport", "Warps");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    @Override
    protected void buildContent(Player viewer) {
        List<String> warps = new ArrayList<>(plugin.warps().names());
        warps.sort(String.CASE_INSENSITIVE_ORDER);

        int[] slots = CONTENT_SLOTS;
        for (int i = 0; i < warps.size() && i < slots.length; i++) {
            String warp = warps.get(i);
            Location loc = plugin.warps().get(warp);
            String where = loc == null ? "unknown"
                    : loc.getWorld() == null ? "unknown"
                    : loc.getWorld().getName() + " " + loc.getBlockX() + ", " + loc.getBlockY()
                            + ", " + loc.getBlockZ();
            set(slots[i], GuiItem.of(Material.ENDER_PEARL)
                    .name("<aqua>" + Text.escape(warp))
                    .lore(
                            "<gray>" + Text.escape(where),
                            "",
                            "<green>Click to teleport")
                    .action(p -> runCommand(p, "warp " + warp)));
        }

        set(49, GuiItem.of(Material.NAME_TAG)
                .name("<yellow>Set Warp")
                .lore("<gray>Create a warp at your location.", "", "<yellow>Click to enter name")
                .action(p -> runCommandWithInput(p, "Type the warp name:", "setwarp")));
    }

}
