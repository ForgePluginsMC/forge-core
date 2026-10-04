package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

/** Look up a material by name fragment. */
public final class ItemdbCommand extends ForgeCommand {
    public ItemdbCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "itemdb";
    }

    @Override
    public List<String> aliases() {
        return List.of("idb", "material");
    }

    @Override
    public String description() {
        return "Look up item materials by name.";
    }

    @Override
    public String usage() {
        return "/itemdb <name...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        String query = String.join("_", args).toUpperCase(Locale.ROOT);
        List<Material> matches = new ArrayList<>();
        for (Material material : Material.values()) {
            if (material.name().contains(query)) {
                matches.add(material);
                if (matches.size() >= 15) {
                    break;
                }
            }
        }
        if (matches.isEmpty()) {
            Text.error(sender, "No materials match <white>" + Text.escape(String.join(" ", args)) + "</white>.");
            return;
        }
        Text.send(sender, "<white>Matches:</white>");
        for (Material material : matches) {
            Text.send(sender, "<gray>" + material.name().toLowerCase(Locale.ROOT) + "</gray>"
                    + (material.isItem() ? "" : " <dark_gray>(block only)</dark_gray>"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
