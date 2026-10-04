package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

/** Color held leather armor. Supports names and hex codes. */
public final class DyeCommand extends PlayerACommand {
    private static final Map<String, Color> COLORS = new HashMap<>();

    static {
        COLORS.put("red", Color.RED);
        COLORS.put("blue", Color.BLUE);
        COLORS.put("green", Color.GREEN);
        COLORS.put("yellow", Color.YELLOW);
        COLORS.put("orange", Color.ORANGE);
        COLORS.put("purple", Color.PURPLE);
        COLORS.put("pink", Color.FUCHSIA);
        COLORS.put("white", Color.WHITE);
        COLORS.put("black", Color.BLACK);
        COLORS.put("gray", Color.GRAY);
        COLORS.put("grey", Color.GRAY);
        COLORS.put("aqua", Color.AQUA);
        COLORS.put("cyan", Color.TEAL);
        COLORS.put("lime", Color.LIME);
        COLORS.put("maroon", Color.MAROON);
        COLORS.put("navy", Color.NAVY);
        COLORS.put("olive", Color.OLIVE);
        COLORS.put("silver", Color.SILVER);
        COLORS.put("teal", Color.TEAL);
    }

    public DyeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "dye";
    }

    @Override
    public String description() {
        return "Color held leather armor.";
    }

    @Override
    public String usage() {
        return "/dye <color|#hex>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty() || !(item.getItemMeta() instanceof LeatherArmorMeta meta)) {
            Text.error(sender, "Hold a piece of leather armor.");
            return;
        }
        String input = args[0].toLowerCase(Locale.ROOT);
        Color color;
        if (input.startsWith("#")) {
            try {
                int rgb = Integer.parseInt(input.substring(1), 16);
                color = Color.fromRGB(rgb);
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                Text.error(sender, "Invalid hex color. Use e.g. <white>#ff0000</white>.");
                return;
            }
        } else {
            color = COLORS.get(input);
            if (color == null) {
                Text.error(sender, "Unknown color. Use a name or <white>#hex</white>.");
                return;
            }
        }
        meta.setColor(color);
        item.setItemMeta(meta);
        Text.ok(sender, "Dyed held armor <white>" + Text.escape(args[0]) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            String last = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
            return COLORS.keySet().stream().filter(c -> c.startsWith(last)).sorted().toList();
        }
        return List.of();
    }
}
