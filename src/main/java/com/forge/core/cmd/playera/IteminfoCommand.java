package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;

/** Show details of the held item. */
public final class IteminfoCommand extends PlayerACommand {
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    public IteminfoCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "iteminfo";
    }

    @Override
    public String description() {
        return "Show details of the held item.";
    }

    @Override
    public String usage() {
        return "/iteminfo";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            Text.error(sender, "Hold an item to inspect.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        Text.send(sender, "<gold><bold>Item info</bold></gold>");
        Text.send(sender, "<gray>Type: <white>" + item.getType().name() + "</white> <gray>amount: <white>"
                + item.getAmount() + "</white> <gray>max stack: <white>" + item.getMaxStackSize() + "</white>");
        if (meta.hasDisplayName()) {
            Text.send(sender, "<gray>Name: <white>" + Text.escape(plain(meta.displayName())) + "</white>");
        }
        var lore = meta.lore();
        if (lore != null && !lore.isEmpty()) {
            Text.send(sender, "<gray>Lore (" + lore.size() + " lines):</gray>");
            for (Component line : lore) {
                Text.send(sender, "<gray>- <white>" + Text.escape(plain(line)) + "</white>");
            }
        }
        var enchants = item.getEnchantments();
        if (!enchants.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (var entry : enchants.entrySet()) {
                if (!builder.isEmpty()) {
                    builder.append("<gray>, </gray>");
                }
                builder.append("<white>").append(entry.getKey().getKey().getKey()).append(' ')
                        .append(entry.getValue()).append("</white>");
            }
            Text.send(sender, "<gray>Enchants: " + builder + "</gray>");
        }
        if (meta.isUnbreakable()) {
            Text.send(sender, "<gray>Unbreakable: <white>yes</white>");
        }
        if (meta instanceof Damageable damageable && damageable.hasDamage()) {
            Text.send(sender, "<gray>Damage: <white>" + damageable.getDamage() + "</white>");
        }
        if (meta.hasCustomModelDataComponent()) {
            var component = meta.getCustomModelDataComponent();
            Text.send(sender, "<gray>Custom model data floats: <white>"
                    + Text.escape(String.valueOf(component.getFloats())) + "</white>");
        }
        var flags = meta.getItemFlags();
        if (!flags.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (ItemFlag flag : flags) {
                if (!builder.isEmpty()) {
                    builder.append("<gray>, </gray>");
                }
                builder.append("<white>").append(flag.name().toLowerCase(Locale.ROOT)).append("</white>");
            }
            Text.send(sender, "<gray>Hidden flags: " + builder + "</gray>");
        }
    }

    private static String plain(@Nullable Component component) {
        return component == null ? "" : PLAIN.serialize(component);
    }
}
