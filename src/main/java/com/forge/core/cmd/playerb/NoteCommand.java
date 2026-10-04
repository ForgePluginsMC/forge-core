package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.data.UserData;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /note — personal notes stored in userdata: add, list, delete, clear.
 * Notes persist as newline-joined text under {@code notes}.
 */
public final class NoteCommand extends ForgeCommand {
    private static final List<String> SUBS = List.of("add", "list", "delete", "clear");

    public NoteCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "note";
    }

    @Override
    public String description() {
        return "Keep personal notes.";
    }

    @Override
    public String usage() {
        return "/note <add|list|delete|clear> [text...]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        UserData data = plugin.users().get(player);
        List<String> notes = read(data);
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "add" -> {
                if (args.length < 2) {
                    Text.usage(sender, "/note add <text...>");
                    return;
                }
                notes.add(String.join(" ", argsRange(args, 1)));
                write(data, notes);
                Text.ok(sender, "Note added (<white>" + notes.size() + "</white> total).");
            }
            case "list" -> {
                if (notes.isEmpty()) {
                    Text.send(sender, "You have no notes.");
                    return;
                }
                Text.send(sender, "<white>Your notes:</white>");
                for (int i = 0; i < notes.size(); i++) {
                    Text.send(sender, "<gray>" + (i + 1) + ".</gray> " + Text.escape(notes.get(i)));
                }
            }
            case "delete", "remove" -> {
                if (args.length != 2) {
                    Text.usage(sender, "/note delete <number>");
                    return;
                }
                int index;
                try {
                    index = Integer.parseInt(args[1]) - 1;
                } catch (NumberFormatException bad) {
                    Text.error(sender, "<white>" + Text.escape(args[1]) + "</white> is not a number.");
                    return;
                }
                if (index < 0 || index >= notes.size()) {
                    Text.error(sender, "No note number <white>" + Text.escape(args[1]) + "</white>.");
                    return;
                }
                notes.remove(index);
                write(data, notes);
                Text.ok(sender, "Note deleted.");
            }
            case "clear" -> {
                write(data, new ArrayList<>());
                Text.ok(sender, "All notes cleared.");
            }
            default -> Text.usage(sender, usage());
        }
        plugin.users().save(player.getUniqueId());
    }

    private static List<String> read(UserData data) {
        String raw = data.getString("notes", "");
        List<String> notes = new ArrayList<>();
        if (!raw.isEmpty()) {
            for (String line : raw.split("\n", -1)) {
                if (!line.isEmpty()) {
                    notes.add(line);
                }
            }
        }
        return notes;
    }

    private static void write(UserData data, List<String> notes) {
        data.setString("notes", String.join("\n", notes));
    }

    private static String[] argsRange(String[] args, int from) {
        String[] out = new String[args.length - from];
        System.arraycopy(args, from, out, 0, out.length);
        return out;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(SUBS, args);
        }
        return List.of();
    }
}
