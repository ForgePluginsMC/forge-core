package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/** /entitynbt — dump an entity's persistent data keys and basic fields. */
public final class EntityNbtCommand extends ForgeCommand {
    public EntityNbtCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "entitynbt";
    }

    @Override
    public String description() {
        return "Dump the looked-at entity's persistent data.";
    }

    @Override
    public String usage() {
        return "/entitynbt";
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
        Text.send(sender, "<gold>Entity fields:</gold>");
        Text.send(sender, "  Type: <white>"
                + entity.getType().name().toLowerCase(Locale.ROOT).replace('_', ' ') + "</white>");
        Text.send(sender, "  UUID: <white>" + entity.getUniqueId() + "</white>");
        Text.send(sender, "  Ticks lived: <white>" + entity.getTicksLived() + "</white>");
        Text.send(sender, "  Velocity: <white>"
                + String.format(Locale.ROOT, "%.2f, %.2f, %.2f",
                        entity.getVelocity().getX(), entity.getVelocity().getY(),
                        entity.getVelocity().getZ()) + "</white>");
        List<String> keys = new ArrayList<>();
        for (NamespacedKey key : entity.getPersistentDataContainer().getKeys()) {
            keys.add(key.toString());
        }
        if (keys.isEmpty()) {
            Text.send(sender, "<gray>No persistent data keys on this entity.</gray>");
        } else {
            Text.send(sender, "<gold>Persistent data keys (" + keys.size() + "):</gold>");
            for (String key : keys.subList(0, Math.min(keys.size(), 30))) {
                Text.send(sender, "  <gray>•</gray> <white>" + Text.escape(key) + "</white>");
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
