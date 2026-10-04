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
 * Home manager: click to teleport, buttons to set/delete.
 *
 * <p>Glyph-rendered background with bright button tiles.
 */
@NullMarked
public final class HomeGui extends GlyphGui {
    private final ForgeCore plugin;
    private final WebGui parent;

    public HomeGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected String glyphChar() {
        return "\uE112";
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Teleport", "Homes");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    @Override
    protected void buildContent(Player viewer) {
        var homes = plugin.users().get(viewer).homes();
        List<String> names = new ArrayList<>(homes.keySet());
        names.sort(String.CASE_INSENSITIVE_ORDER);

        int[] slots = CONTENT_SLOTS;
        for (int i = 0; i < names.size() && i < slots.length; i++) {
            String name = names.get(i);
            Location loc = homes.get(name);
            String where = loc.getWorld() == null ? "unknown"
                    : loc.getWorld().getName() + " " + loc.getBlockX() + ", " + loc.getBlockY()
                            + ", " + loc.getBlockZ();
            set(slots[i], tile(ForgeIcons.TILE_GREEN,
                    "<green><bold>" + Text.escape(name),
                    List.of("<gray>" + Text.escape(where),
                            "", "<green>Click to teleport",
                            "<red>Right-click to delete"),
                    p -> runCommand(p, "home " + name)));
        }

        if (names.isEmpty()) {
            set(22, tile(ForgeIcons.TILE_GRAY, "<gray>No homes set",
                    List.of("<dark_gray>Use the button below to set one."), p -> {}));
        }
    }

    @Override
    protected void buildFooter(Player viewer) {
        super.buildFooter(viewer);
        set(47, tile(ForgeIcons.TILE_YELLOW, "<yellow><bold>Set Home",
                List.of("<gray>Create a home at your location.",
                        "", "<yellow>Click to enter name"),
                p -> runCommandWithInput(p, "Type the home name:", "sethome")));
    }
}
