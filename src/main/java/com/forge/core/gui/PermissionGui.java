package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.permission.Group;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Permission group browser: click a group for details and actions.
 *
 * <p>Glyph-rendered background with bright button tiles.
 */
@NullMarked
public final class PermissionGui extends GlyphGui {
    private final ForgeCore plugin;
    private final WebGui parent;

    public PermissionGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected String glyphChar() {
        return "\uE117";
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Permissions");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    @Override
    protected void buildContent(Player viewer) {
        List<String> names = new ArrayList<>(plugin.permissions().groups().groupNames());
        names.sort(String.CASE_INSENSITIVE_ORDER);

        int[] slots = CONTENT_SLOTS;
        for (int i = 0; i < names.size() && i < slots.length; i++) {
            String name = names.get(i);
            Group group = plugin.permissions().groups().getGroup(name);
            if (group == null) {
                continue;
            }
            int permCount = plugin.permissions().groups().effectivePermissions(name).size();
            set(slots[i], tile(ForgeIcons.TILE_BASE,
                    "<blue><bold>" + Text.escape(name),
                    List.of("<gray>Permissions: <white>" + permCount,
                            "<gray>Weight: <white>" + group.weight(),
                            "<gray>Prefix: <white>" + Text.escape(group.prefix()),
                            "", "<green>Click for details"),
                    p -> runCommand(p, "group info " + name)));
        }

        if (names.isEmpty()) {
            set(22, tile(ForgeIcons.TILE_GRAY, "<gray>No groups yet",
                    List.of("<dark_gray>Create one below."), p -> {}));
        }
    }

    @Override
    protected void buildFooter(Player viewer) {
        super.buildFooter(viewer);
        set(47, tile(ForgeIcons.TILE_GREEN, "<green><bold>Create Group",
                List.of("<gray>Create a new permission group.",
                        "", "<yellow>Click to enter name"),
                p -> runCommandWithInput(p, "Type the group name:", "group create")));
    }
}
