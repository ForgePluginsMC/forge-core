package com.forge.core.merge.announcer;

import com.forge.core.ForgeCore;
import com.forge.core.util.Placeholders;
import com.forge.core.util.Text;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 * Loads announcements from {@code plugins/ForgeCore/announcer.yml}, rotates
 * each on its own scheduler interval, and delivers the formatted message to
 * every online player. ForgeCore placeholders (e.g. {@code %player_name%})
 * are expanded per player before MiniMessage parsing.
 */
final class AnnouncerManager {

    private static final String DEFAULT_YML = """
            # ForgeCore announcer configuration.
            #
            # Each announcement rotates on its own interval and is delivered to every
            # online player. All messages are MiniMessage; chat messages support click
            # events like <click:run_command:/rules>. ForgeCore placeholders such as
            # %player_name% are expanded per player.
            #
            #   id:               unique name, used by /announce broadcast <id>
            #   messages:         list of MiniMessage strings
            #   interval-seconds: how often this announcement fires
            #   mode:             sequential (cycle in order) or random
            #   delivery:         chat | actionbar | bossbar | title
            #   bossbar-color:    PINK | BLUE | RED | GREEN | YELLOW | PURPLE | WHITE
            #   bossbar-seconds:  how long the boss bar stays visible
            #   title-fade-in:    title fade-in time in ticks (20 = 1 second)
            #   title-stay:       title stay time in ticks
            #   title-fade-out:   title fade-out time in ticks

            announcements:
              - id: welcome
                messages:
                  - "<gradient:#ffb347:#ff7b1c>Welcome to the server!</gradient> <gray>Read the <click:run_command:/rules><u>rules</u></click> before you play.</gray>"
                  - "<gradient:#ffb347:#ff7b1c>Welcome to the server!</gradient> <gray>Need help? Ask in chat.</gray>"
                interval-seconds: 300
                mode: sequential
                delivery: chat
              - id: vote
                messages:
                  - "<aqua><bold>Vote for the server</bold></aqua> <gray>and earn rewards! <click:run_command:/vote><u>Click to vote</u></click></gray>"
                interval-seconds: 600
                mode: random
                delivery: bossbar
                bossbar-color: BLUE
                bossbar-seconds: 8
            """;

    private final ForgeCore plugin;
    private final File file;
    private final List<Announcement> announcements = new ArrayList<>();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final List<BossBar> activeBars = new ArrayList<>();
    private final Map<String, Integer> sequentialIndex = new HashMap<>();

    AnnouncerManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "announcer.yml");
    }

    void start() {
        load();
    }

    void reload() {
        cancelAll();
        load();
    }

    int count() {
        return announcements.size();
    }

    /** Copy of the configured announcement ids (safe to call off-thread). */
    List<String> ids() {
        List<String> ids = new ArrayList<>();
        for (Announcement announcement : announcements) {
            ids.add(announcement.id());
        }
        return ids;
    }

    /** Immediately broadcasts one announcement's next message. False when the id is unknown. */
    boolean broadcastNow(String id) {
        for (Announcement announcement : announcements) {
            if (announcement.id().equalsIgnoreCase(id)) {
                deliver(announcement);
                return true;
            }
        }
        return false;
    }

    private void load() {
        if (!file.exists()) {
            try {
                Files.writeString(file.toPath(), DEFAULT_YML, StandardCharsets.UTF_8);
            } catch (IOException e) {
                plugin.getLogger().warning("Could not write default announcer.yml: " + e.getMessage());
                return;
            }
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<Map<?, ?>> entries = config.getMapList("announcements");
        for (Map<?, ?> entry : entries) {
            Announcement announcement = Announcement.fromMap(entry);
            if (announcement == null) {
                plugin.getLogger().warning(
                        "Skipping announcement entry: needs an id, at least one message, and a positive interval-seconds.");
                continue;
            }
            announcements.add(announcement);
            long periodTicks = announcement.intervalSeconds() * 20L;
            BukkitTask task = plugin.getServer().getScheduler()
                    .runTaskTimer(plugin, () -> deliver(announcement), periodTicks, periodTicks);
            tasks.add(task);
            plugin.getLogger().info("Scheduled announcement '" + announcement.id() + "' every "
                    + announcement.intervalSeconds() + "s ("
                    + announcement.delivery().name().toLowerCase() + ").");
        }
    }

    private void cancelAll() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
        for (BossBar bar : activeBars) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.hideBossBar(bar);
            }
        }
        activeBars.clear();
        announcements.clear();
        sequentialIndex.clear();
    }

    private void deliver(Announcement announcement) {
        switch (announcement.delivery()) {
            case CHAT -> {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    player.sendMessage(nextMessage(announcement, player));
                }
            }
            case ACTIONBAR -> {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    player.sendActionBar(nextMessage(announcement, player));
                }
            }
            case TITLE -> {
                Title.Times times = Title.Times.times(
                        Duration.ofMillis(announcement.titleFadeIn() * 50L),
                        Duration.ofMillis(announcement.titleStay() * 50L),
                        Duration.ofMillis(announcement.titleFadeOut() * 50L));
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    player.showTitle(Title.title(nextMessage(announcement, player), Component.empty(), times));
                }
            }
            case BOSSBAR -> {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    BossBar bar = BossBar.bossBar(nextMessage(announcement, player), 1.0f,
                            announcement.bossbarColor(), BossBar.Overlay.PROGRESS);
                    player.showBossBar(bar);
                    activeBars.add(bar);
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        player.hideBossBar(bar);
                        activeBars.remove(bar);
                    }, announcement.bossbarSeconds() * 20L);
                }
            }
        }
    }

    private Component nextMessage(Announcement announcement, Player player) {
        List<String> messages = announcement.messages();
        String raw;
        if (announcement.mode() == Announcement.Mode.RANDOM || messages.size() == 1) {
            raw = messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
        } else {
            int index = sequentialIndex.getOrDefault(announcement.id(), 0);
            raw = messages.get(index % messages.size());
            sequentialIndex.put(announcement.id(), index + 1);
        }
        return Text.of(Placeholders.apply(player, raw));
    }
}
