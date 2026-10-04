package com.forge.core.guild;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Territory enforcement and feedback:
 * <ul>
 *   <li>Only guild members (or bypass permission) may build/break in claimed chunks.</li>
 *   <li>Action-bar territory notices when crossing chunk borders.</li>
 *   <li>War scoring: kills inside enemy territory earn a point for the killer's guild.</li>
 * </ul>
 */
public final class GuildListener implements Listener {
    private final ForgeCore plugin;
    private final GuildManager guilds;
    /** Last notified claim key per player, to avoid action-bar spam. */
    private final Map<UUID, String> lastTerritory = new HashMap<>();

    public GuildListener(ForgeCore plugin, GuildManager guilds) {
        this.plugin = plugin;
        this.guilds = guilds;
    }

    private boolean canBuild(Player player, Chunk chunk) {
        if (plugin.permissions().hasPermission(player, "forgecore.claim.bypass")) {
            return true;
        }
        Guild owner = guilds.claimOwnerGuild(chunk);
        if (owner == null) {
            return true;
        }
        Guild playerGuild = guilds.guildOf(player);
        return playerGuild != null && playerGuild.name().equalsIgnoreCase(owner.name());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!canBuild(event.getPlayer(), event.getBlock().getChunk())) {
            event.setCancelled(true);
            Text.error(event.getPlayer(), "This land is claimed by another guild.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!canBuild(event.getPlayer(), event.getBlock().getChunk())) {
            event.setCancelled(true);
            Text.error(event.getPlayer(), "This land is claimed by another guild.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Chunk from = event.getFrom().getChunk();
        Chunk to = event.getTo().getChunk();
        if (from.getX() == to.getX() && from.getZ() == to.getZ()
                && from.getWorld().getUID().equals(to.getWorld().getUID())) {
            return;
        }
        Player player = event.getPlayer();
        Guild owner = guilds.claimOwnerGuild(to);
        String key = owner == null ? "wild" : owner.name().toLowerCase(Locale.ROOT);
        if (key.equals(lastTerritory.get(player.getUniqueId()))) {
            return;
        }
        lastTerritory.put(player.getUniqueId(), key);
        if (owner == null) {
            player.sendActionBar(Text.of("<gray>Wilderness</gray>"));
        } else {
            Guild playerGuild = guilds.guildOf(player);
            boolean own = playerGuild != null && playerGuild.name().equalsIgnoreCase(owner.name());
            String color = own ? "<green>" : "<red>";
            player.sendActionBar(Text.of(color + Text.escape(owner.tag())
                    + " <gray>Territory</gray>"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) {
            return;
        }
        Guild killerGuild = guilds.guildOf(killer);
        Guild victimGuild = guilds.guildOf(victim);
        if (killerGuild == null || victimGuild == null) {
            return;
        }
        if (killerGuild.name().equalsIgnoreCase(victimGuild.name())) {
            return;
        }
        GuildManager.War war = guilds.warOf(killerGuild.name());
        if (war == null || !war.involves(victimGuild.name())) {
            return;
        }
        Guild chunkOwner = guilds.claimOwnerGuild(victim.getLocation().getChunk());
        if (chunkOwner == null || !chunkOwner.name().equalsIgnoreCase(victimGuild.name())) {
            return;
        }
        war.addPoint(killerGuild.name());
        int scoreK = war.scoreOf(killerGuild.name());
        int scoreV = war.scoreOf(victimGuild.name());
        String message = "<gold>War point: <white>" + Text.escape(killerGuild.tag())
                + "</white> " + scoreK + " - " + scoreV + " <white>"
                + Text.escape(victimGuild.tag()) + "</white>";
        guilds.broadcastToGuild(killerGuild, message);
        guilds.broadcastToGuild(victimGuild, message);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastTerritory.remove(event.getPlayer().getUniqueId());
    }
}
