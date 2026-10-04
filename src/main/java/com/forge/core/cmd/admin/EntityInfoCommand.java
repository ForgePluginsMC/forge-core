package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Locs;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/** /entityinfo — inspect the entity you are looking at. */
public final class EntityInfoCommand extends ForgeCommand {
    public EntityInfoCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "entityinfo";
    }

    @Override
    public String description() {
        return "Show info about the entity you are looking at.";
    }

    @Override
    public String usage() {
        return "/entityinfo";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        Entity entity = player.getTargetEntity(64);
        if (entity == null) {
            Text.error(sender, "Look at an entity first.");
            return;
        }
        Text.send(sender, "<gold>Entity info:</gold>");
        Text.send(sender, "  Type: <white>"
                + entity.getType().name().toLowerCase(Locale.ROOT).replace('_', ' ') + "</white>");
        Text.send(sender, "  UUID: <white>" + entity.getUniqueId() + "</white>");
        Text.send(sender, "  Location: <white>" + Text.escape(Locs.pretty(entity.getLocation())) + "</white>");
        if (entity instanceof LivingEntity living) {
            double max = 20.0;
            var attribute = living.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
            if (attribute != null) {
                max = attribute.getValue();
            }
            Text.send(sender, "  Health: <white>"
                    + String.format(Locale.ROOT, "%.1f", living.getHealth()) + "/"
                    + String.format(Locale.ROOT, "%.1f", max) + "</white>");
        }
        Component customName = entity.customName();
        Text.send(sender, "  Custom name: <white>" + (customName == null ? "none"
                : Text.escape(PlainTextComponentSerializer.plainText().serialize(customName))) + "</white>");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
