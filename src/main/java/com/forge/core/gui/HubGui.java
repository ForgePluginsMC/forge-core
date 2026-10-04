package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The /menu hub: category icons leading to per-pack command browsers.
 */
@NullMarked
public final class HubGui extends ForgeGui {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final ForgeCore plugin;

    public HubGui(ForgeCore plugin) {
        this.plugin = plugin;
    }

    @Override
    protected Component title() {
        return MM.deserialize("<gold><bold>ForgeCore Menu");
    }

    @Override
    protected int size() {
        return 54;
    }

    @Override
    protected void build(Player viewer) {
        List<MenuCategory> categories = MenuCategory.all();
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        for (int i = 0; i < categories.size() && i < slots.length; i++) {
            MenuCategory cat = categories.get(i);
            List<ForgeCommand> commands = cat.commands().apply(plugin);
            long usable = commands.stream()
                    .filter(c -> hasAccess(viewer, c))
                    .count();
            set(slots[i], GuiItem.of(cat.icon())
                    .name(cat.displayName())
                    .lore(
                            cat.description(),
                            "",
                            "<yellow>" + usable + " <gray>commands available",
                            "",
                            "<green>Click to open")
                    .action(p -> new CategoryGui(plugin, cat, this).open(p)));
        }
        // Quick actions row.
        set(37, GuiItem.of(org.bukkit.Material.PLAYER_HEAD)
                .name("<aqua><bold>Online Players")
                .lore("<gray>View and manage online players.", "", "<green>Click to open")
                .action(p -> new PlayerListGui(plugin, this).open(p)));
        set(38, GuiItem.of(org.bukkit.Material.ENDER_PEARL)
                .name("<aqua><bold>Warps")
                .lore("<gray>Teleport to warps.", "", "<green>Click to open")
                .action(p -> new WarpGui(plugin, this).open(p)));
        set(39, GuiItem.of(org.bukkit.Material.RED_BED)
                .name("<green><bold>My Homes")
                .lore("<gray>Manage your homes.", "", "<green>Click to open")
                .action(p -> new HomeGui(plugin, this).open(p)));
        set(40, GuiItem.of(org.bukkit.Material.CHEST)
                .name("<gold><bold>Kits")
                .lore("<gray>Browse and claim kits.", "", "<green>Click to open")
                .action(p -> new KitGui(plugin, this).open(p)));
        set(41, GuiItem.of(org.bukkit.Material.SHIELD)
                .name("<yellow><bold>Guild")
                .lore("<gray>Guild dashboard.", "", "<green>Click to open")
                .action(p -> new GuildGui(plugin, this).open(p)));
        set(42, GuiItem.of(org.bukkit.Material.BARRIER)
                .name("<red><bold>Ban List")
                .lore("<gray>View and manage bans.", "", "<green>Click to open")
                .action(p -> new BanListGui(plugin, this).open(p)));
        set(43, GuiItem.of(org.bukkit.Material.PAPER)
                .name("<blue><bold>Permissions")
                .lore("<gray>Browse permission groups.", "", "<green>Click to open")
                .action(p -> new PermissionGui(plugin, this).open(p)));
        // Bottom row.
        set(47, GuiItem.of(org.bukkit.Material.BOOK)
                .name("<light_purple><bold>Quests")
                .lore("<gray>Browse and track your quests.", "", "<green>Click to open")
                .action(p -> {
                    p.closeInventory();
                    p.performCommand("quest");
                }));
        set(49, GuiItem.of(org.bukkit.Material.WRITABLE_BOOK)
                .name("<yellow><bold>Mail Inbox")
                .lore("<gray>Read your mail.", "", "<green>Click to open")
                .action(p -> new MailGui(plugin, this).open(p)));
    }

    private boolean hasAccess(Player player, ForgeCommand command) {
        String perm = command.permission();
        return perm.isEmpty() || plugin.permissions().hasPermission(player, perm);
    }

    /** Open the hub. */
    public static void open(ForgeCore plugin, Player player) {
        new HubGui(plugin).open(player);
    }
}
