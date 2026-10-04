package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Warp browser: click a warp to teleport.
 *
 * <p>Glyph-rendered background with bright button tiles.
 */
@NullMarked
public final class WarpGui extends GlyphGui {
    private final ForgeCore plugin;
    private final WebGui parent;

    public WarpGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected String glyphChar() {
        return "\uE111";
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
            set(slots[i], tile(ForgeIcons.TILE_BASE,
                    "<aqua><bold>" + Text.escape(warp),
                    List.of("<gray>" + Text.escape(where),
                            "", "<green>Click to teleport"),
                    p -> runCommand(p, "warp " + warp)));
        }

        if (warps.isEmpty()) {
            set(22, tile(ForgeIcons.TILE_GRAY, "<gray>No warps set",
                    List.of("<dark_gray>Ask an admin to create one."), p -> {}));
        }
    }

    @Override
    protected void buildFooter(Player viewer) {
        super.buildFooter(viewer);
        // Set-warp button in the footer area (slot 47).
        set(47, tile(ForgeIcons.TILE_YELLOW, "<yellow><bold>Set Warp",
                List.of("<gray>Create a warp at your location.",
                        "", "<yellow>Click to enter name"),
                p -> runCommandWithInput(p, "Type the warp name:", "setwarp")));
    }
}
