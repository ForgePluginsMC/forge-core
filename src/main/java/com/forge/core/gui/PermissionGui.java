package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.permission.Group;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Permission group browser: click a group for details and actions.
 */
@NullMarked
public final class PermissionGui extends WebGui {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final ForgeCore plugin;
    private final WebGui parent;

    public PermissionGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
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
            set(slots[i], GuiItem.of(Material.PAPER)
                    .name("<blue>" + Text.escape(name))
                    .lore(
                            "<gray>Permissions: <white>" + permCount,
                            "<gray>Weight: <white>" + group.weight(),
                            "<gray>Prefix: <white>" + Text.escape(group.prefix()),
                            "",
                            "<green>Click for details")
                    .action(p -> runCommand(p, "group info " + name)));
        }

        set(49, GuiItem.of(Material.NAME_TAG)
                .name("<green>Create Group")
                .lore("<gray>Create a new permission group.", "", "<yellow>Click to enter name")
                .action(p -> runCommandWithInput(p, "Type the group name:", "group create")));
    }

}
