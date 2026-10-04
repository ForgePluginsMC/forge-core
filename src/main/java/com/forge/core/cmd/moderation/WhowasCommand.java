package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

/** Look up a player by any name they have used: UUID, last name/IP, activity. */
public final class WhowasCommand extends ForgeCommand {
    public WhowasCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "whowas";
    }

    @Override
    public String description() {
        return "Look up a player by a name they have used before.";
    }

    @Override
    public String usage() {
        return "/whowas <name>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        File folder = new File(plugin.getDataFolder(), "userdata");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        List<String[]> matches = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String lastName = config.getString("last-name", "");
                if (lastName.equalsIgnoreCase(args[0])) {
                    matches.add(new String[]{
                            file.getName().replace(".yml", ""),
                            lastName,
                            config.getString("last-ip", "unknown"),
                            String.valueOf(config.getLong("first-seen", 0L)),
                            String.valueOf(config.getLong("last-seen", 0L)),
                    });
                }
            }
        }
        if (matches.isEmpty()) {
            throw new CommandRegistry.CommandFailure("No record of anyone called <white>" + Text.escape(args[0]) + "</white>.");
        }
        for (String[] match : matches) {
            long firstSeen;
            long lastSeen;
            try {
                firstSeen = Long.parseLong(match[3]);
                lastSeen = Long.parseLong(match[4]);
            } catch (NumberFormatException exception) {
                firstSeen = 0L;
                lastSeen = 0L;
            }
            UUID uuid;
            try {
                uuid = UUID.fromString(match[0]);
            } catch (IllegalArgumentException exception) {
                continue;
            }
            Text.send(sender, "<gold><bold>Who was:</bold></gold> <white>" + Text.escape(match[1]) + "</white> <gray>(" + uuid + ")");
            Text.send(sender, "<gray>Last IP: <white>" + Text.escape(match[2]));
            Text.send(sender, "<gray>First seen: <white>" + (firstSeen == 0 ? "unknown" : Time.formatDate(firstSeen))
                    + " <gray>Last seen: <white>" + (lastSeen == 0 ? "unknown" : Time.formatDate(lastSeen)));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
