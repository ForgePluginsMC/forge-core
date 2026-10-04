package com.forge.core.gui.dashboard;

import com.forge.core.ForgeCore;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Async world pregeneration with progress tracking.
 *
 * <p>Loads chunks in a spiral pattern from the world spawn, processing a
 * configurable number per tick to avoid lag. Progress is queryable for GUI
 * display.
 */
@NullMarked
public final class PregenerateTask {
    private static @Nullable PregenerateTask active;

    private final ForgeCore plugin;
    private final UUID worldId;
    private final String worldName;
    private final int radius;
    private final Deque<long[]> queue = new ArrayDeque<>();
    private final int total;
    private int done;
    private final long startTime;
    private @Nullable BukkitTask task;
    private volatile boolean cancelled;

    private PregenerateTask(ForgeCore plugin, World world, int radiusChunks) {
        this.plugin = plugin;
        this.worldId = world.getUID();
        this.worldName = world.getName();
        this.radius = radiusChunks;
        this.startTime = System.currentTimeMillis();

        int spawnX = world.getSpawnLocation().getBlockX() >> 4;
        int spawnZ = world.getSpawnLocation().getBlockZ() >> 4;

        // Spiral pattern from center outward
        for (int r = 0; r <= radiusChunks; r++) {
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) == r) {
                        queue.add(new long[]{spawnX + x, spawnZ + z});
                    }
                }
            }
        }
        this.total = queue.size();
        this.done = 0;
    }

    /**
     * Start pregeneration, or return null if one is already running.
     *
     * @param radiusChunks radius in chunks (not blocks)
     * @return the task, or null if already running
     */
    public static synchronized @Nullable PregenerateTask start(
            ForgeCore plugin, World world, int radiusChunks) {
        if (active != null && !active.isDone()) {
            return null;
        }
        active = new PregenerateTask(plugin, world, radiusChunks);
        active.begin();
        return active;
    }

    /** Get the currently active task, or null. */
    public static synchronized @Nullable PregenerateTask active() {
        if (active != null && active.isDone()) {
            active = null;
        }
        return active;
    }

    private void begin() {
        // Process 4 chunks per tick (~80/sec) — gentle on the server
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            World world = Bukkit.getWorld(worldId);
            if (world == null || cancelled) {
                finish();
                return;
            }
            for (int i = 0; i < 4 && !queue.isEmpty(); i++) {
                long[] coords = queue.poll();
                if (coords == null) {
                    break;
                }
                int x = (int) coords[0];
                int z = (int) coords[1];
                // Async chunk load — non-blocking
                world.getChunkAtAsync(x, z).thenRun(() -> {
                    synchronized (this) {
                        done++;
                    }
                });
            }
            if (queue.isEmpty()) {
                // Wait for pending async loads to complete
                Bukkit.getScheduler().runTaskLater(plugin, this::finish, 100L);
            }
        }, 1L, 1L);
    }

    private void finish() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /** Cancel the task. */
    public synchronized void cancel() {
        cancelled = true;
        finish();
    }

    /** Whether the task is complete or cancelled. */
    public synchronized boolean isDone() {
        return queue.isEmpty() && done >= total || cancelled;
    }

    /** Chunks completed. */
    public synchronized int done() {
        return done;
    }

    /** Total chunks to generate. */
    public int total() {
        return total;
    }

    /** Progress as 0.0-1.0. */
    public synchronized double progress() {
        return total == 0 ? 1.0 : Math.min(1.0, (double) done / total);
    }

    /** World name being generated. */
    public String worldName() {
        return worldName;
    }

    /** Radius in chunks. */
    public int radius() {
        return radius;
    }

    /** Elapsed seconds. */
    public long elapsedSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    /** Estimated seconds remaining, or -1 if unknown. */
    public synchronized long etaSeconds() {
        if (done == 0) {
            return -1;
        }
        double rate = (double) done / Math.max(1, elapsedSeconds());
        if (rate <= 0) {
            return -1;
        }
        return (long) ((total - done) / rate);
    }
}
