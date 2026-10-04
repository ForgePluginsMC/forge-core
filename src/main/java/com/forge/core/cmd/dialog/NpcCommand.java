package com.forge.core.cmd.dialog;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.dialog.NpcManager;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

/**
 * /npc — create and manage dialog NPCs.
 *
 * <p>Subcommands: create, dialog, list, remove, move.
 */
@NullMarked
public final class NpcCommand extends ForgeCommand {
    public NpcCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "npc";
    }

    @Override
    public List<String> aliases() {
        return List.of("npcs");
    }

    @Override
    public String description() {
        return "Create and manage dialog NPCs.";
    }

    @Override
    public String usage() {
        return "/npc <create|dialog|list|remove|move> ...";
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
        NpcManager npcs = plugin.npcs();
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "create" -> {
                if (args.length < 3) {
                    Text.usage(sender, "/npc create <id> <name...>");
                    return;
                }
                String id = args[1].toLowerCase(Locale.ROOT);
                String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                npcs.create(id, name, player.getLocation());
                Text.ok(sender, "NPC '" + id + "' created at your location.");
            }
            case "dialog" -> {
                if (args.length < 3) {
                    Text.usage(sender, "/npc dialog <id> <dialog-id>");
                    return;
                }
                if (plugin.dialogs().get(args[2].toLowerCase(Locale.ROOT)) == null) {
                    Text.error(sender, "No dialog with id '" + args[2] + "'.");
                    return;
                }
                if (!npcs.setDialog(args[1].toLowerCase(Locale.ROOT), args[2].toLowerCase(Locale.ROOT))) {
                    Text.error(sender, "No NPC with id '" + args[1] + "'.");
                    return;
                }
                Text.ok(sender, "NPC '" + args[1] + "' now shows dialog '" + args[2] + "'.");
            }
            case "list" -> {
                List<String> ids = npcs.ids();
                if (ids.isEmpty()) {
                    Text.send(sender, "No NPCs created.");
                    return;
                }
                Text.send(sender, "NPCs: " + String.join(", ", ids));
            }
            case "remove" -> {
                if (args.length < 2) {
                    Text.usage(sender, "/npc remove <id>");
                    return;
                }
                if (!npcs.remove(args[1].toLowerCase(Locale.ROOT))) {
                    Text.error(sender, "No NPC with id '" + args[1] + "'.");
                    return;
                }
                Text.ok(sender, "NPC '" + args[1] + "' removed.");
            }
            case "move" -> {
                if (args.length < 2) {
                    Text.usage(sender, "/npc move <id>");
                    return;
                }
                if (!npcs.move(args[1].toLowerCase(Locale.ROOT), player.getLocation())) {
                    Text.error(sender, "No NPC with id '" + args[1] + "'.");
                    return;
                }
                Text.ok(sender, "NPC '" + args[1] + "' moved to your location.");
            }
            default -> Text.usage(sender, usage());
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (String s : List.of("create", "dialog", "list", "remove", "move")) {
                if (s.startsWith(prefix)) {
                    out.add(s);
                }
            }
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("create") && !args[0].equalsIgnoreCase("list")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            for (String id : plugin.npcs().ids()) {
                if (id.startsWith(prefix)) {
                    out.add(id);
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("dialog")) {
            String prefix = args[2].toLowerCase(Locale.ROOT);
            for (String id : plugin.dialogs().ids()) {
                if (id.startsWith(prefix)) {
                    out.add(id);
                }
            }
        }
        return out;
    }
}
