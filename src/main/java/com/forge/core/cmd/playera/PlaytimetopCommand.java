package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

/** Top 10 total playtimes, scanned from userdata files. */
public final class PlaytimetopCommand extends PlayerACommand {
    public PlaytimetopCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "playtimetop";
    }

    @Override
    public String description() {
        return "Show the top playtimes.";
    }

    @Override
    public String usage() {
        return "/playtimetop";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Text.send(sender, "<gray>Loading playtime leaderboard…");
        File folder = new File(plugin.getDataFolder(), "userdata");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Entry> entries = scan(folder);
            entries.sort(Comparator.comparingLong(Entry::seconds).reversed());
            List<Entry> top = entries.subList(0, Math.min(10, entries.size()));
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (top.isEmpty()) {
                    Text.send(sender, "Nobody has playtime recorded yet.");
                    return;
                }
                Text.send(sender, "<gold><bold>Top playtime</bold></gold>");
                int rank = 1;
                for (Entry entry : top) {
                    Text.send(sender, "<gray>" + rank++ + ". <white>" + Text.escape(entry.name())
                            + "</white> — <green>" + Time.format(entry.seconds()) + "</green>");
                }
            });
        });
    }

    private static List<Entry> scan(File folder) {
        List<Entry> entries = new ArrayList<>();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return entries;
        }
        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            long seconds = config.getLong("playtime-seconds", 0);
            if (seconds <= 0) {
                continue;
            }
            String name = config.getString("last-name", file.getName().replace(".yml", ""));
            entries.add(new Entry(name, seconds));
        }
        return entries;
    }

    private record Entry(String name, long seconds) {
    }
}
