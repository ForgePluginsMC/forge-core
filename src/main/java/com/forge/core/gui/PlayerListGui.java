package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Online player list: click a head for quick actions.
 */
@NullMarked
public final class PlayerListGui extends WebGui {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final ForgeCore plugin;
    private final WebGui parent;

    public PlayerListGui(ForgeCore plugin, WebGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Menu", "Players");
    }

    @Override
    protected @Nullable WebGui parent() {
        return parent;
    }

    @Override
    protected void buildContent(Player viewer) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));

        int[] slots = CONTENT_SLOTS;
        for (int i = 0; i < players.size() && i < slots.length; i++) {
            Player target = players.get(i);
            set(slots[i], playerItem(viewer, target));
        }
    }


    private GuiItem playerItem(Player viewer, Player target) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(target);
        head.setItemMeta(meta);

        GuiItem item = GuiItem.from(head)
                .name("<aqua>" + Text.escape(target.getName()))
                .lore(
                        "<gray>Level: <white>" + target.getLevel(),
                        "<gray>Gamemode: <white>" + target.getGameMode().name().toLowerCase(),
                        "<gray>Ping: <white>" + target.getPing() + "ms",
                        "",
                        "<yellow>Click for actions");
        item.action(p -> new PlayerActionGui(plugin, target, this).open(p));
        return item;
    }
}
