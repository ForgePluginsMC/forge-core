package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

/** Top voters, scanned from userdata files. */
public final class VotetopCommand extends ForgeCommand {
    private record Entry(String name, long votes) {
    }

    public VotetopCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "votetop";
    }

    @Override
    public String description() {
        return "Show the top voters.";
    }

    @Override
    public String usage() {
        return "/votetop";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        File folder = new File(plugin.getDataFolder(), "userdata");
        List<Entry> entries = new ArrayList<>();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String base = file.getName().substring(0, file.getName().length() - 4);
                UUID uuid;
                try {
                    uuid = UUID.fromString(base);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                long votes = config.getLong("votes", 0);
                if (votes <= 0) {
                    continue;
                }
                String name = Bukkit.getOfflinePlayer(uuid).getName();
                entries.add(new Entry(name == null ? "Unknown" : name, votes));
            }
        }
        entries.sort(Comparator.comparingLong(Entry::votes).reversed());
        Text.send(sender, "<gold>--- Top Voters ---");
        int rank = 0;
        for (Entry entry : entries.subList(0, Math.min(10, entries.size()))) {
            rank++;
            Text.send(sender, "<yellow>" + rank + ". <white>" + Text.escape(entry.name())
                    + "</white> — <green>" + entry.votes() + "</green> votes");
        }
        if (entries.isEmpty()) {
            Text.send(sender, "<gray>Nobody has voted yet.");
        }
    }
}
