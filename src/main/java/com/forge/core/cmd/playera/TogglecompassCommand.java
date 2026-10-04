package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jspecify.annotations.NullMarked;

/** Toggle a bossbar compass overlay showing your facing direction. */
@NullMarked
public final class TogglecompassCommand extends PlayerACommand implements Listener {
    private final Map<UUID, BossBar> bars = new ConcurrentHashMap<>();

    public TogglecompassCommand(ForgeCore plugin) {
        super(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String name() {
        return "togglecompass";
    }

    @Override
    public String description() {
        return "Toggle a bossbar compass overlay.";
    }

    @Override
    public String usage() {
        return "/togglecompass";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        BossBar existing = bars.remove(uuid);
        if (existing != null) {
            player.hideBossBar(existing);
            Text.ok(sender, "Compass overlay <red>disabled</red>.");
            return;
        }
        BossBar bar = BossBar.bossBar(compass(player), 1.0f,
                BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
        bars.put(uuid, bar);
        player.showBossBar(bar);
        Text.ok(sender, "Compass overlay <green>enabled</green>.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        BossBar bar = bars.get(event.getPlayer().getUniqueId());
        if (bar == null) {
            return;
        }
        bar.name(compass(event.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        bars.remove(event.getPlayer().getUniqueId());
    }

    private static Component compass(Player player) {
        float yaw = player.getLocation().getYaw();
        // Normalize to 0-360, 0 = south in Minecraft; convert to compass
        double deg = ((yaw % 360) + 360) % 360;
        String facing;
        if (deg < 22.5 || deg >= 337.5) facing = "S";
        else if (deg < 67.5) facing = "SW";
        else if (deg < 112.5) facing = "W";
        else if (deg < 157.5) facing = "NW";
        else if (deg < 202.5) facing = "N";
        else if (deg < 247.5) facing = "NE";
        else if (deg < 292.5) facing = "E";
        else facing = "SE";
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        return Text.of("<white>N</white> <gray>·</gray> <white>NE</white> <gray>·</gray> "
                + "<white>E</white> <gray>·</gray> <white>SE</white> <gray>·</gray> "
                + "<gold><bold>" + facing + "</bold></gold> <gray>·</gray> "
                + "<gray>" + x + ", " + y + ", " + z + "</gray>");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
