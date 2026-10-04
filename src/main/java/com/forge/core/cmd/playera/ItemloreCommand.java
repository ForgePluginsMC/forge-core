package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Edit the held item's lore: {@code add} appends a line, {@code set <n> <text>}
 * replaces line n, {@code remove <n>} deletes it, {@code clear} wipes it.
 * Colors need {@code forgecore.itemlore.color}.
 */
public final class ItemloreCommand extends PlayerACommand {
    public ItemloreCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "itemlore";
    }

    @Override
    public String description() {
        return "Edit the held item's lore.";
    }

    @Override
    public String usage() {
        return "/itemlore <add|set|clear|remove> [text...]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            Text.error(sender, "Hold an item to edit.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        var existing = meta.lore();
        List<Component> lore = new ArrayList<>(existing == null ? List.of() : existing);
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "add" -> {
                if (args.length < 2) {
                    Text.usage(sender, "/itemlore add <text...>");
                    return;
                }
                lore.add(colored(sender, join(args, 1)));
            }
            case "set" -> {
                if (args.length < 3) {
                    Text.usage(sender, "/itemlore set <line> <text...>");
                    return;
                }
                int line = lineNumber(sender, args[1], lore.size());
                if (line < 0) {
                    return;
                }
                lore.set(line, colored(sender, join(args, 2)));
            }
            case "remove" -> {
                if (args.length < 2) {
                    Text.usage(sender, "/itemlore remove <line>");
                    return;
                }
                int line = lineNumber(sender, args[1], lore.size());
                if (line < 0) {
                    return;
                }
                lore.remove(line);
            }
            case "clear" -> lore.clear();
            default -> {
                Text.usage(sender, usage());
                return;
            }
        }
        meta.lore(lore.isEmpty() ? null : lore);
        item.setItemMeta(meta);
        Text.ok(sender, "Lore updated <gray>(" + lore.size() + " lines).</gray>");
    }

    private static String join(String[] args, int from) {
        StringBuilder builder = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (i > from) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }

    private static Component colored(CommandSender sender, String text) {
        String mini = sender.hasPermission("forgecore.itemlore.color") ? text : Text.strip(text);
        return Text.of(mini);
    }

    /** Parse a 1-based line number; -1 when invalid (already messaged). */
    private static int lineNumber(CommandSender sender, String raw, int size) {
        int line;
        try {
            line = Integer.parseInt(raw);
        } catch (NumberFormatException exception) {
            Text.error(sender, "Line must be a number.");
            return -1;
        }
        if (line < 1 || line > size) {
            Text.error(sender, "Line must be between 1 and " + Math.max(size, 1) + ".");
            return -1;
        }
        return line - 1;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("add", "set", "clear", "remove"), args);
        }
        return List.of();
    }
}
