package com.forge.core.gui;

import org.jspecify.annotations.NullMarked;

/**
 * Custom model data keys for ForgeCore's resource pack UI.
 *
 * <p>These correspond to the {@code minecraft:select} cases in
 * {@code assets/minecraft/items/paper.json}. Use with {@link GuiItem#model(String)}.
 */
@NullMarked
public final class ForgeIcons {
    private ForgeIcons() {
    }

    // Buttons
    public static final String BUTTON_PRIMARY = "forge_button_primary";
    public static final String BUTTON_SECONDARY = "forge_button_secondary";
    public static final String BUTTON_DANGER = "forge_button_danger";
    public static final String BUTTON_SUCCESS = "forge_button_success";
    public static final String BUTTON_BACK = "forge_button_back";
    public static final String ARROW_LEFT = "forge_arrow_left";
    public static final String ARROW_RIGHT = "forge_arrow_right";
    public static final String DOT_ACTIVE = "forge_dot_active";
    public static final String DOT_INACTIVE = "forge_dot_inactive";

    // Status
    public static final String STATUS_ONLINE = "forge_status_online";
    public static final String STATUS_OFFLINE = "forge_status_offline";
    public static final String STATUS_BUSY = "forge_status_busy";
    public static final String STATUS_AWAY = "forge_status_away";

    // Category icons
    public static final String ICON_TELEPORT = "forge_icon_teleport";
    public static final String ICON_MODERATION = "forge_icon_moderation";
    public static final String ICON_ECONOMY = "forge_icon_economy";
    public static final String ICON_TOOLS = "forge_icon_tools";
    public static final String ICON_GUILD = "forge_icon_guild";
    public static final String ICON_QUEST = "forge_icon_quest";
    public static final String ICON_PERMISSION = "forge_icon_permission";
    public static final String ICON_NPC = "forge_icon_npc";
    public static final String ICON_WARP = "forge_icon_warp";
    public static final String ICON_HOME = "forge_icon_home";
    public static final String ICON_KIT = "forge_icon_kit";
    public static final String ICON_MAIL = "forge_icon_mail";
}
