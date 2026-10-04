package com.forge.core.gui.dashboard;

import com.forge.core.ForgeCore;
import com.forge.core.gui.ChatInput;
import com.forge.core.gui.ForgeIcons;
import com.forge.core.gui.GuiItem;
import com.forge.core.gui.WebGui;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Setup assistant: first-run wizard for essential server configuration.
 *
 * <p>Steps: spawn location, MOTD, starting balance, default group.
 */
@NullMarked
public final class DashboardSetupGui extends WebGui {
    private final ForgeCore plugin;
    private final int step;

    public DashboardSetupGui(ForgeCore plugin) {
        this(plugin, 0);
    }

    private DashboardSetupGui(ForgeCore plugin, int step) {
        this.plugin = plugin;
        this.step = step;
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Dashboard", "Setup", "Step " + (step + 1) + " of 4");
    }

    @Override
    protected @Nullable WebGui parent() {
        return new DashboardGui(plugin);
    }

    @Override
    protected void buildContent(Player viewer) {
        // Progress indicator
        set(10, progressCard());

        switch (step) {
            case 0 -> buildSpawnStep(viewer);
            case 1 -> buildMotdStep(viewer);
            case 2 -> buildBalanceStep(viewer);
            case 3 -> buildGroupStep(viewer);
            default -> buildDoneStep(viewer);
        }
    }

    private GuiItem progressCard() {
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            bar.append(i <= step ? "<green>█" : "<dark_gray>█");
        }
        return GuiItem.of(Material.PAPER)
                .model(ForgeIcons.DOT_ACTIVE)
                .name("<gold><bold>Setup Progress")
                .lore("<gray>" + bar + " <white>Step " + (step + 1) + " of 4");
    }

    private void buildSpawnStep(Player viewer) {
        set(22, GuiItem.of(Material.COMPASS)
                .name("<gold><bold>Set Spawn Location")
                .lore(
                        "<gray>Set the world spawn to your",
                        "<gray>current location.",
                        "",
                        "<gray>Current: <white>" + formatLoc(viewer),
                        "",
                        "<yellow>Click to set spawn here")
                .action(p -> {
                    p.getWorld().setSpawnLocation(p.getLocation());
                    Text.send(p, "<green>Spawn set to your location.");
                    next(p);
                }));
        addNavButtons(viewer, false);
    }

    private void buildMotdStep(Player viewer) {
        String current = plugin.getConfig().getString("motd", "");
        set(22, GuiItem.of(Material.OAK_SIGN)
                .name("<gold><bold>Set MOTD")
                .lore(
                        "<gray>Message shown to players on join.",
                        "",
                        "<gray>Current: <white>" + truncate(strip(current), 40),
                        "",
                        "<yellow>Click to enter a new MOTD")
                .action(p -> {
                    p.closeInventory();
                    Text.send(p, "<yellow>Enter the new MOTD <gray>(MiniMessage supported, 'cancel' to abort):");
                    ChatInput.request(p, input -> {
                        if (input.equalsIgnoreCase("cancel")) {
                            Text.send(p, "<gray>Cancelled.");
                            return;
                        }
                        plugin.getConfig().set("motd", input);
                        plugin.saveConfig();
                        Text.send(p, "<green>MOTD updated.");
                        next(p);
                    });
                }));
        addNavButtons(viewer, true);
    }

    private void buildBalanceStep(Player viewer) {
        double current = plugin.getConfig().getDouble("starting-balance", 100.0);
        set(22, GuiItem.of(Material.GOLD_INGOT)
                .name("<gold><bold>Starting Balance")
                .lore(
                        "<gray>Currency given to first-time players.",
                        "",
                        "<gray>Current: <white>" + current,
                        "",
                        "<yellow>Click to set a new amount")
                .action(p -> {
                    p.closeInventory();
                    Text.send(p, "<yellow>Enter the starting balance <gray>(number, 'cancel' to abort):");
                    ChatInput.request(p, input -> {
                        if (input.equalsIgnoreCase("cancel")) {
                            Text.send(p, "<gray>Cancelled.");
                            return;
                        }
                        try {
                            double amount = Double.parseDouble(input);
                            if (amount < 0) {
                                Text.error(p, "Amount must be 0 or more.");
                                return;
                            }
                            plugin.getConfig().set("starting-balance", amount);
                            plugin.saveConfig();
                            Text.send(p, "<green>Starting balance set to " + amount + ".");
                            next(p);
                        } catch (NumberFormatException e) {
                            Text.error(p, "'" + input + "' is not a number.");
                        }
                    });
                }));
        addNavButtons(viewer, true);
    }

    private void buildGroupStep(Player viewer) {
        var groups = new ArrayList<>(plugin.permissions().groups().groupNames());
        if (groups.isEmpty()) {
            set(22, GuiItem.of(Material.BARRIER)
                    .name("<red><bold>No groups found")
                    .lore("<gray>Create a group first with /group create <name>."));
        } else {
            int slot = 19;
            for (String group : groups) {
                if (slot > 25) {
                    break;
                }
                final String g = group;
                set(slot, GuiItem.of(Material.PLAYER_HEAD)
                        .name("<gold><bold>" + g)
                        .lore("<gray>Click to set as default group")
                        .action(p -> {
                            plugin.getConfig().set("default-group", g);
                            plugin.saveConfig();
                            Text.send(p, "<green>Default group set to " + g + ".");
                            next(p);
                        }));
                slot++;
            }
        }
        addNavButtons(viewer, true);
    }

    private void buildDoneStep(Player viewer) {
        set(22, GuiItem.of(Material.EMERALD_BLOCK)
                .model(ForgeIcons.STATUS_ONLINE)
                .name("<green><bold>Setup Complete!")
                .lore(
                        "<gray>Your server is configured.",
                        "",
                        "<yellow>Click to return to dashboard")
                .action(p -> new DashboardGui(plugin).open(p)));
    }

    private void addNavButtons(Player viewer, boolean showBack) {
        if (showBack && step > 0) {
            set(38, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.ARROW_LEFT)
                    .name("<yellow><bold>Previous Step")
                    .action(p -> new DashboardSetupGui(plugin, step - 1).open(p)));
        }
        if (step < 4) {
            set(42, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.ARROW_RIGHT)
                    .name("<yellow><bold>Skip Step")
                    .lore("<gray>Leave this setting unchanged.")
                    .action(this::next));
        }
    }

    private void next(Player player) {
        if (step < 4) {
            new DashboardSetupGui(plugin, step + 1).open(player);
        } else {
            new DashboardGui(plugin).open(player);
        }
    }

    private static String formatLoc(Player player) {
        var loc = player.getLocation();
        return String.format("%d, %d, %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private static String strip(String miniMessage) {
        return miniMessage.replaceAll("<[^>]+>", "");
    }

    private static String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
