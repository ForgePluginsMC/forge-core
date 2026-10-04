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
public final class PlayerListGui extends ForgeGui {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final ForgeCore plugin;
    private final ForgeGui parent;

    public PlayerListGui(ForgeCore plugin, ForgeGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected Component title() {
        return MM.deserialize("<aqua><bold>Online Players");
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
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));

        int[] slots = gridSlots();
        for (int i = 0; i < players.size() && i < slots.length; i++) {
            Player target = players.get(i);
            set(slots[i], playerItem(viewer, target));
        }
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
