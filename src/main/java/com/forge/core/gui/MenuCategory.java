package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import java.util.List;
import java.util.function.Function;
import org.bukkit.Material;
import org.jspecify.annotations.NullMarked;

/**
 * Menu categories: display info + supplier of that pack's commands.
 */
@NullMarked
public record MenuCategory(
        String id,
        String displayName,
        Material icon,
        String description,
        Function<ForgeCore, List<ForgeCommand>> commands) {

    /** All categories shown in the /menu hub. */
    public static List<MenuCategory> all() {
        return List.of(
                new MenuCategory("teleport", "<aqua><bold>Teleport",
                        Material.ENDER_PEARL, "<gray>Homes, warps, teleports & requests.",
                        p -> com.forge.core.cmd.teleport.TeleportPack.commands(p)),
                new MenuCategory("moderation", "<red><bold>Moderation",
                        Material.IRON_SWORD, "<gray>Bans, mutes, kicks & warnings.",
                        p -> com.forge.core.cmd.moderation.ModerationPack.commands(p)),
                new MenuCategory("economy", "<gold><bold>Economy",
                        Material.GOLD_INGOT, "<gray>Money, shops, jobs & payments.",
                        p -> com.forge.core.cmd.economy.EconomyPack.commands(p)),
                new MenuCategory("player", "<green><bold>Player Tools",
                        Material.PLAYER_HEAD, "<gray>Kits, effects, utilities & fun.",
                        p -> combine(p,
                                com.forge.core.cmd.playera.PlayerAPack::commands,
                                com.forge.core.cmd.playerb.PlayerBPack::commands)),
                new MenuCategory("admin", "<dark_red><bold>Admin",
                        Material.COMMAND_BLOCK, "<gray>Server management & world tools.",
                        p -> combine(p,
                                com.forge.core.cmd.admin.AdminPack::commands,
                                com.forge.core.cmd.systemsa.SystemsAPack::commands,
                                com.forge.core.cmd.systemsb.SystemsBPack::commands)),
                new MenuCategory("guild", "<yellow><bold>Guilds",
                        Material.SHIELD, "<gray>Guilds, territory & wars.",
                        p -> com.forge.core.guild.GuildPack.commands(p)),
                new MenuCategory("quest", "<light_purple><bold>Quests",
                        Material.BOOK, "<gray>Quests, dailies & weeklies.",
                        p -> com.forge.core.cmd.quest.QuestPack.commands(p)),
                new MenuCategory("permission", "<blue><bold>Permissions",
                        Material.PAPER, "<gray>Groups, tracks & permission nodes.",
                        p -> com.forge.core.cmd.permission.PermissionPack.commands(p)),
                new MenuCategory("dialog", "<white><bold>NPCs & Dialogs",
                        Material.VILLAGER_SPAWN_EGG, "<gray>NPCs and fullscreen dialogs.",
                        p -> com.forge.core.cmd.dialog.DialogPack.commands(p)),
                new MenuCategory("extras", "<dark_aqua><bold>Extras",
                        Material.CHEST, "<gray>Announcer, chat, stacking, items & playtime.",
                        p -> combine(p,
                                com.forge.core.merge.announcer.AnnouncerPack::commands,
                                com.forge.core.merge.chat.ChatPack::commands,
                                com.forge.core.merge.stack.StackPack::commands,
                                com.forge.core.merge.items.ItemsPack::commands,
                                com.forge.core.merge.playtime.PlaytimePack::commands)));
    }

    @SafeVarargs
    private static List<ForgeCommand> combine(
            ForgeCore plugin, Function<ForgeCore, List<ForgeCommand>>... suppliers) {
        var out = new java.util.ArrayList<ForgeCommand>();
        for (var s : suppliers) {
            out.addAll(s.apply(plugin));
        }
        return out;
    }
}
