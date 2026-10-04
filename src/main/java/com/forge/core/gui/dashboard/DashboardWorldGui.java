package com.forge.core.gui.dashboard;

import com.forge.core.ForgeCore;
import com.forge.core.gui.ForgeIcons;
import com.forge.core.gui.GuiItem;
import com.forge.core.gui.WebGui;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * World management: world list with stats and chunk pregeneration.
 */
@NullMarked
public final class DashboardWorldGui extends WebGui {
    private final ForgeCore plugin;

    public DashboardWorldGui(ForgeCore plugin) {
        this.plugin = plugin;
    }

    @Override
    protected List<String> breadcrumb() {
        return List.of("Dashboard", "Worlds");
    }

    @Override
    protected @Nullable WebGui parent() {
        return new DashboardGui(plugin);
    }

    @Override
    protected void buildContent(Player viewer) {
        List<World> worlds = Bukkit.getWorlds();
        int slot = 10;

        for (World world : worlds) {
            if (slot > 34) {
                break;
            }
            set(slot, worldCard(world));
            slot += 2;
            if (slot % 9 == 8) {
                slot += 2; // Skip to next row start area
            }
        }

        // Pregeneration status / controls
        PregenerateTask active = PregenerateTask.active();
        if (active != null) {
            set(40, progressCard(active)
                    .action(p -> {
                        // Refresh to update progress
                        new DashboardWorldGui(plugin).open(p);
                    }));
            set(41, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.BUTTON_DANGER)
                    .name("<red><bold>Cancel Pregeneration")
                    .lore("<gray>Stop the running task.")
                    .action(p -> {
                        active.cancel();
                        Text.send(p, "<yellow>Pregeneration cancelled.");
                        new DashboardWorldGui(plugin).open(p);
                    }));
        } else {
            set(40, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.BUTTON_PRIMARY)
                    .name("<green><bold>Pre-generate World")
                    .lore(
                            "<gray>Generate chunks around spawn",
                            "<gray>to reduce exploration lag.",
                            "",
                            "<yellow>Click to select world and radius")
                    .action(p -> new PregenSetupGui(plugin).open(p)));
        }
    }

    private GuiItem worldCard(World world) {
        int players = world.getPlayers().size();
        int chunks = world.getLoadedChunks().length;
        int entities = world.getEntities().size();

        return GuiItem.of(Material.GRASS_BLOCK)
                .name("<gold><bold>" + world.getName())
                .lore(
                        "<gray>Environment: <white>" + world.getEnvironment().name(),
                        "<gray>Seed: <white>" + world.getSeed(),
                        "<gray>Players: <white>" + players,
                        "<gray>Loaded chunks: <white>" + chunks,
                        "<gray>Entities: <white>" + entities,
                        "<gray>Difficulty: <white>" + world.getDifficulty().name());
    }

    private GuiItem progressCard(PregenerateTask task) {
        int pct = (int) (task.progress() * 100);
        String bar = progressBar(task.progress(), 20);
        String eta = task.etaSeconds() < 0 ? "calculating..."
                : task.etaSeconds() + "s remaining";

        return GuiItem.of(Material.PAPER)
                .model(ForgeIcons.BUTTON_SUCCESS)
                .name("<green><bold>Pregenerating: " + task.worldName())
                .lore(
                        "<gray>" + bar + " <white>" + pct + "%",
                        "<gray>Chunks: <white>" + task.done() + " / " + task.total(),
                        "<gray>Elapsed: <white>" + task.elapsedSeconds() + "s",
                        "<gray>ETA: <white>" + eta,
                        "",
                        "<yellow>Click to refresh");
    }

    private static String progressBar(double progress, int width) {
        int filled = (int) (progress * width);
        StringBuilder sb = new StringBuilder("<green>");
        for (int i = 0; i < width; i++) {
            sb.append(i < filled ? "█" : "<dark_gray>█");
            if (i == filled - 1) {
                sb.append("<dark_gray>");
            }
        }
        return sb.toString();
    }

    /**
     * Sub-GUI for selecting world and radius for pregeneration.
     */
    private static final class PregenSetupGui extends WebGui {
        private final ForgeCore plugin;
        private int radius = 32; // chunks

        PregenSetupGui(ForgeCore plugin) {
            this.plugin = plugin;
        }

        @Override
        protected List<String> breadcrumb() {
            return List.of("Dashboard", "Worlds", "Pre-generate");
        }

        @Override
        protected @Nullable WebGui parent() {
            return new DashboardWorldGui(plugin);
        }

        @Override
        protected void buildContent(Player viewer) {
            // World selection
            int slot = 10;
            for (World world : Bukkit.getWorlds()) {
                if (slot > 16) {
                    break;
                }
                final World w = world;
                set(slot, GuiItem.of(Material.GRASS_BLOCK)
                        .name("<gold><bold>" + w.getName())
                        .lore("<gray>Click to pre-generate this world",
                                "<gray>Radius: <white>" + radius + " chunks")
                        .action(p -> startPregen(p, w)));
                slot++;
            }

            // Radius selector
            set(28, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.ARROW_LEFT)
                    .name("<yellow><bold>Decrease Radius")
                    .lore("<gray>Current: <white>" + radius + " chunks")
                    .action(p -> {
                        radius = Math.max(8, radius - 8);
                        open(p);
                    }));
            set(31, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.DOT_ACTIVE)
                    .name("<white><bold>Radius: " + radius + " chunks")
                    .lore("<gray>≈ " + (radius * 16) + " blocks from spawn"));
            set(34, GuiItem.of(Material.PAPER)
                    .model(ForgeIcons.ARROW_RIGHT)
                    .name("<yellow><bold>Increase Radius")
                    .lore("<gray>Current: <white>" + radius + " chunks")
                    .action(p -> {
                        radius = Math.min(128, radius + 8);
                        open(p);
                    }));
        }

        private void startPregen(Player player, World world) {
            PregenerateTask task = PregenerateTask.start(plugin, world, radius);
            if (task == null) {
                Text.error(player, "A pregeneration task is already running.");
                return;
            }
            Text.send(player, "<green>Pre-generation started for <white>" + world.getName()
                    + " <green>(" + radius + " chunk radius, "
                    + String.format(Locale.ROOT, "%,d", task.total()) + " chunks).");
            new DashboardWorldGui(plugin).open(player);
        }
    }
}
