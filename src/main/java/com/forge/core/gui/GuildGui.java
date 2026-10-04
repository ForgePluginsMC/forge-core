package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.guild.Guild;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Guild dashboard: info, members, bank, claims, war status.
 */
@NullMarked
public final class GuildGui extends ForgeGui {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final ForgeCore plugin;
    private final ForgeGui parent;

    public GuildGui(ForgeCore plugin, ForgeGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected Component title() {
        return MM.deserialize("<yellow><bold>Guild");
    }

    @Override
    protected int size() {
        return 27;
    }

    @Override
    protected @Nullable ForgeGui parent() {
        return parent;
    }

    @Override
    protected void build(Player viewer) {
        Guild guild = plugin.guilds().guildOf(viewer);
        if (guild == null) {
            set(11, GuiItem.of(Material.SHIELD)
                    .name("<yellow>Create a Guild")
                    .lore("<gray>Start your own guild.", "", "<yellow>Click to enter name & tag")
                    .action(p -> {
                        p.closeInventory();
                        Text.send(p, "<yellow>Type the guild name (or 'cancel'):");
                        ChatInput.request(p, name -> {
                            if (name.equalsIgnoreCase("cancel")) {
                                Text.send(p, "<gray>Cancelled.");
                                return;
                            }
                            final String guildName = name;
                            Text.send(p, "<yellow>Type the guild tag (or 'cancel'):");
                            ChatInput.request(p, tag -> {
                                if (tag.equalsIgnoreCase("cancel")) {
                                    Text.send(p, "<gray>Cancelled.");
                                    return;
                                }
                                p.performCommand("guild create " + guildName + " " + tag);
                            });
                        });
                    }));
            set(15, GuiItem.of(Material.BOOK)
                    .name("<aqua>Guild List")
                    .lore("<gray>Browse all guilds.", "", "<green>Click to view")
                    .action(p -> runCommand(p, "guild list")));
            return;
        }

        int claims = plugin.guilds().claimsOf(guild).size();
        List<String> memberNames = new ArrayList<>();
        for (var uuid : guild.members()) {
            var offline = Bukkit.getOfflinePlayer(uuid);
            String name = offline.getName() == null ? "?" : offline.getName();
            memberNames.add(guild.roleOf(uuid) + ": " + name);
        }

        set(10, GuiItem.of(Material.SHIELD)
                .name("<yellow>" + Text.escape(guild.name()) + " <gray>[" + Text.escape(guild.tag()) + "]")
                .lore(
                        "<gray>Members: <white>" + guild.size(),
                        "<gray>Claims: <white>" + claims,
                        "<gray>Bank: <green>$" + String.format("%.2f", guild.bank()),
                        "",
                        "<green>Click for details")
                .action(p -> runCommand(p, "guild info")));
        set(12, GuiItem.of(Material.PLAYER_HEAD)
                .name("<aqua>Members (" + guild.size() + ")")
                .lore(memberNames.isEmpty() ? List.of("<gray>No members.")
                        : memberNames.stream().limit(8)
                                .map(m -> "<gray>" + Text.escape(m)).toList())
                .action(p -> runCommand(p, "guild info")));
        set(14, GuiItem.of(Material.GOLD_INGOT)
                .name("<gold>Guild Bank")
                .lore(
                        "<gray>Balance: <green>$" + String.format("%.2f", guild.bank()),
                        "",
                        "<yellow>Click to deposit",
                        "<gray>Type amount in chat")
                .action(p -> runCommandWithInput(p,
                        "Type amount to deposit:", "guild bank deposit")));
        set(16, GuiItem.of(Material.MAP)
                .name("<green>Territory (" + claims + " claims)")
                .lore("<gray>View your claim map.", "", "<green>Click to view")
                .action(p -> runCommand(p, "claimmap")));
        set(22, GuiItem.of(Material.IRON_SWORD)
                .name("<red>Guild War")
                .lore("<gray>Challenge another guild.", "", "<yellow>Click to enter guild name")
                .action(p -> runCommandWithInput(p,
                        "Type the guild to challenge:", "guild war")));
    }
}
