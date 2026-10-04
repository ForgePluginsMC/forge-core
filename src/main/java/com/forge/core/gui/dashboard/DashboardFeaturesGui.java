package com.forge.core.gui.dashboard;

import com.forge.core.ForgeCore;
import com.forge.core.gui.ForgeIcons;
import com.forge.core.gui.GuiItem;
import com.forge.core.gui.WebGui;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Feature toggles: enable/disable ForgeCore systems with one click.
 *
 * <p>States persist to config.yml under {@code features.*}.
 */
@NullMarked
public final class DashboardFeaturesGui extends WebGui {
    private final ForgeCore plugin;

    /** Feature definition: config key, display name, description, default. */
    private record Feature(String key, String name, String description, boolean def) {
    }

    private static final List<Feature> FEATURES = List.of(
            new Feature("features.chat-filter", "Chat Filter",
                    "Filter profanity and slurs in chat.", true),
            new Feature("features.anti-spam", "Anti-Spam",
                    "Rate-limit chat messages.", true),
            new Feature("features.afk-system", "AFK System",
                    "Track idle players and broadcast AFK status.", true),
            new Feature("features.motd-on-join", "MOTD on Join",
                    "Show message of the day when players join.", true),
            new Feature("features.tablist-animation", "Tablist Animation",
                    "Animated tablist header and footer.", true),
            new Feature("features.death-messages", "Death Messages",
                    "Custom death message formatting.", true),
            new Feature("features.join-quit-messages", "Join/Quit Messages",
                    "Custom join and quit broadcasts.", true),
            new Feature("features.maintenance-mode", "Maintenance Mode",
                    "Block non-bypassed players from joining.", false));

    public DashboardFeaturesGui(ForgeCore plugin) {
        this.plugin = plugin;
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Dashboard", "Features");
    }

    @Override
    protected @Nullable WebGui parent() {
        return new DashboardGui(plugin);
    }

    @Override
    protected void buildContent(Player viewer) {
        int slot = 10;
        for (Feature feature : FEATURES) {
            if (slot > 34) {
                break;
            }
            set(slot, toggleCard(feature));
            slot++;
            if (slot % 9 == 8) {
                slot++; // Keep within content area nicely
            }
        }
    }

    private GuiItem toggleCard(Feature feature) {
        boolean enabled = plugin.getConfig().getBoolean(feature.key(), feature.def());
        Material icon = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String iconKey = enabled ? ForgeIcons.STATUS_ONLINE : ForgeIcons.STATUS_OFFLINE;
        String stateColor = enabled ? "<green>" : "<red>";
        String stateText = enabled ? "ON" : "OFF";

        return GuiItem.of(icon)
                .model(iconKey)
                .name(stateColor + "<bold>" + feature.name() + " [" + stateText + "]")
                .lore(
                        "<gray>" + feature.description(),
                        "",
                        "<yellow>Click to " + (enabled ? "disable" : "enable"))
                .action(p -> {
                    boolean next = !plugin.getConfig().getBoolean(feature.key(), feature.def());
                    plugin.getConfig().set(feature.key(), next);
                    plugin.saveConfig();
                    Text.send(p, stateColor + feature.name()
                            + (next ? " <green>enabled." : " <red>disabled."));
                    // Refresh to show new state
                    new DashboardFeaturesGui(plugin).open(p);
                });
    }
}
