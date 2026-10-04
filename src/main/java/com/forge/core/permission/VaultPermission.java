package com.forge.core.permission;

import com.forge.core.ForgeCore;
import java.util.Map;
import java.util.UUID;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NullMarked;

/**
 * Vault permission provider bridging to ForgeCore's built-in permission
 * system. Lets any Vault-aware plugin query groups and nodes.
 */
@NullMarked
@SuppressWarnings("deprecation")
public final class VaultPermission extends Permission {
    private final ForgeCore core;

    public VaultPermission(ForgeCore core) {
        this.core = core;
        this.plugin = core;
    }

    private PermissionManager perms() {
        return core.permissions();
    }

    private static UUID uuidOf(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return player.getUniqueId();
    }

    private static Map<String, String> worldContext(String world) {
        if (world == null || world.isEmpty()) {
            return Map.of();
        }
        return Map.of("world", world.toLowerCase(java.util.Locale.ROOT));
    }

    @Override
    public String getName() {
        return "ForgeCore";
    }

    @Override
    public boolean isEnabled() {
        return core.isEnabled();
    }

    @Override
    public boolean hasSuperPermsCompat() {
        return true;
    }

    @Override
    public boolean playerHas(String world, String player, String permission) {
        return perms().hasPermission(uuidOf(player), permission, worldContext(world));
    }

    @Override
    public boolean playerAdd(String world, String player, String permission) {
        perms().setPermission(uuidOf(player),
                PermissionEntry.parse(permission));
        return true;
    }

    @Override
    public boolean playerRemove(String world, String player, String permission) {
        return perms().unsetPermission(uuidOf(player), permission);
    }

    @Override
    public boolean groupHas(String world, String group, String permission) {
        Group g = perms().groups().getGroup(group);
        if (g == null) {
            return false;
        }
        Map<String, String> context = worldContext(world);
        PermissionManager.ScoredEntry best = null;
        for (PermissionEntry entry : perms().groups().effectivePermissions(g.name())) {
            if (entry.isExpired() || !entry.appliesTo(context) || !entry.matches(permission)) {
                continue;
            }
            if (best == null || entry.specificity() > best.entry.specificity()
                    || (entry.specificity() == best.entry.specificity() && !entry.value())) {
                best = new PermissionManager.ScoredEntry(entry, 1, g.weight());
            }
        }
        return best != null && best.entry.value();
    }

    @Override
    public boolean groupAdd(String world, String group, String permission) {
        Group g = perms().groups().getGroup(group);
        if (g == null) {
            return false;
        }
        Map<String, String> context = worldContext(world);
        PermissionEntry parsed = PermissionEntry.parse(permission);
        g.setPermission(new PermissionEntry(parsed.node(), parsed.value(), context, 0));
        perms().markDirty();
        return true;
    }

    @Override
    public boolean groupRemove(String world, String group, String permission) {
        Group g = perms().groups().getGroup(group);
        if (g == null) {
            return false;
        }
        boolean removed = g.removePermission(permission);
        if (removed) {
            perms().markDirty();
        }
        return removed;
    }

    @Override
    public boolean playerInGroup(String world, String player, String group) {
        return perms().userGroups(uuidOf(player)).contains(group.toLowerCase(java.util.Locale.ROOT));
    }

    @Override
    public boolean playerAddGroup(String world, String player, String group) {
        return perms().addGroup(uuidOf(player), group);
    }

    @Override
    public boolean playerRemoveGroup(String world, String player, String group) {
        return perms().removeGroup(uuidOf(player), group);
    }

    @Override
    public String[] getPlayerGroups(String world, String player) {
        return perms().userGroups(uuidOf(player)).toArray(String[]::new);
    }

    @Override
    public String getPrimaryGroup(String world, String player) {
        String primary = perms().primaryGroup(uuidOf(player));
        return primary == null ? "" : primary;
    }

    @Override
    public String[] getGroups() {
        return perms().groups().groupNames().toArray(String[]::new);
    }

    @Override
    public boolean hasGroupSupport() {
        return true;
    }
}
