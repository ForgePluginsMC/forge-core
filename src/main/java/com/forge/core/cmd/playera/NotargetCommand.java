package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jspecify.annotations.NullMarked;

/** Toggle whether mobs target you. */
@NullMarked
public final class NotargetCommand extends PlayerACommand implements Listener {
    private static final Set<UUID> NO_TARGET = ConcurrentHashMap.newKeySet();

    public NotargetCommand(ForgeCore plugin) {
        super(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String name() {
        return "notarget";
    }

    @Override
    public String description() {
        return "Toggle whether mobs target you.";
    }

    @Override
    public String usage() {
        return "/notarget";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        if (NO_TARGET.remove(uuid)) {
            Text.ok(sender, "Mobs will <green>target</green> you again.");
        } else {
            NO_TARGET.add(uuid);
            Text.ok(sender, "Mobs will <red>not target</red> you.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player && NO_TARGET.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
