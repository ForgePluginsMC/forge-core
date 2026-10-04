package com.forge.core.permission;

import com.forge.core.ForgeCore;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * LuckPerms-style permission service: per-user nodes, groups with multiple
 * inheritance, world/gamemode contexts, temporary grants and verbose
 * debugging. Everything persists to {@code permissions.yml}.
 *
 * <p>Resolution order for a check: the most specific matching entry wins
 * (exact node beats wildcards, longer wildcard prefixes beat shorter ones);
 * user entries outrank group entries; on a final tie an explicit denial wins.
 * When nothing matches, an online player's own Bukkit permissions decide
 * (so OP status keeps working); offline players default to denied.
 */
public final class PermissionManager {
    private final ForgeCore plugin;
    private final File file;
    private final GroupManager groups;
    private final Map<UUID, Set<PermissionEntry>> userPermissions = new ConcurrentHashMap<>();
    /** User groups mapped to expiry epoch millis (0 = permanent). */
    private final Map<UUID, Map<String, Long>> userGroups = new ConcurrentHashMap<>();
    private final Set<UUID> verboseViewers = ConcurrentHashMap.newKeySet();
    private boolean verboseConsole;
    private boolean dirty;

    public PermissionManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "permissions.yml");
        this.groups = new GroupManager(this);
        load();
        // Periodic save + temp-grant expiry sweep.
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, task -> {
            if (purgeExpired()) {
                dirty = true;
            }
            if (dirty) {
                save();
            }
        }, 1200L, 1200L);
    }

    public GroupManager groups() {
        return groups;
    }

    /** Mark the store dirty so the next periodic save persists. */
    public void markDirty() {
        dirty = true;
    }

    // ---- user permissions ----

    private Set<PermissionEntry> entries(UUID uuid) {
        return userPermissions.computeIfAbsent(uuid, id -> ConcurrentHashMap.newKeySet());
    }

    /** Grant a node to a user (replaces any grant for the same pattern+contexts). */
    public void setPermission(UUID uuid, PermissionEntry entry) {
        Set<PermissionEntry> set = entries(uuid);
        set.removeIf(existing -> existing.node().equals(entry.node())
                && existing.contexts().equals(entry.contexts()));
        set.add(entry);
        dirty = true;
    }

    /** Remove every grant whose pattern equals the node. */
    public boolean unsetPermission(UUID uuid, String node) {
        String id = node.toLowerCase(Locale.ROOT);
        Set<PermissionEntry> set = userPermissions.get(uuid);
        boolean removed = set != null && set.removeIf(entry -> entry.node().equals(id));
        if (removed) {
            dirty = true;
        }
        return removed;
    }

    public Set<PermissionEntry> userEntries(UUID uuid) {
        return Set.copyOf(userPermissions.getOrDefault(uuid, Set.of()));
    }

    // ---- user groups ----

    /** Permanent group membership; false when the group does not exist. */
    public boolean addGroup(UUID uuid, String group) {
        return addGroup(uuid, group, 0);
    }

    /** Group membership with expiry epoch millis (0 = permanent). */
    public boolean addGroup(UUID uuid, String group, long expiry) {
        String id = group.toLowerCase(Locale.ROOT);
        if (groups.getGroup(id) == null) {
            return false;
        }
        userGroups.computeIfAbsent(uuid, key -> new ConcurrentHashMap<>()).put(id, expiry);
        dirty = true;
        return true;
    }

    public boolean removeGroup(UUID uuid, String group) {
        String id = group.toLowerCase(Locale.ROOT);
        Map<String, Long> map = userGroups.get(uuid);
        boolean removed = map != null && map.remove(id) != null;
        if (removed) {
            dirty = true;
        }
        return removed;
    }

    /** Permanent group names for a user (excludes expired temporary grants). */
    public Set<String> userGroups(UUID uuid) {
        purgeExpired();
        Map<String, Long> map = userGroups.getOrDefault(uuid, Map.of());
        Set<String> out = new LinkedHashSet<>();
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            long expiry = entry.getValue();
            if (expiry == 0 || expiry > System.currentTimeMillis()) {
                out.add(entry.getKey());
            }
        }
        return out;
    }

    /** Strip a deleted group from every user. */
    void removeGroupEverywhere(String group) {
        for (Map<String, Long> map : userGroups.values()) {
            map.remove(group);
        }
    }

    /** Highest-weight group of a user, or null when they have none. */
    public @Nullable String primaryGroup(UUID uuid) {
        String best = null;
        int bestWeight = Integer.MIN_VALUE;
        for (String id : userGroups(uuid)) {
            Group group = groups.getGroup(id);
            if (group == null) {
                continue;
            }
            if (group.weight() > bestWeight) {
                bestWeight = group.weight();
                best = id;
            }
        }
        return best;
    }

    // ---- resolution ----

    /** Candidate entry tagged with where it came from, for tie-breaking. */
    static final class ScoredEntry {
        final PermissionEntry entry;
        /** 2 = user grant, 1 = group grant. */
        final int sourceRank;
        final int weight;

        ScoredEntry(PermissionEntry entry, int sourceRank, int weight) {
            this.entry = entry;
            this.sourceRank = sourceRank;
            this.weight = weight;
        }
    }

    /** Result of a check, with a human-readable trace of how it resolved. */
    public record CheckResult(boolean allowed, String trace) {
    }

    public boolean hasPermission(UUID uuid, String node) {
        return check(uuid, node, Map.of()).allowed();
    }

    public boolean hasPermission(UUID uuid, String node, Map<String, String> context) {
        return check(uuid, node, context).allowed();
    }

    /** Permission check for a command sender; non-players keep Bukkit behavior. */
    public boolean hasPermission(CommandSender sender, String node) {
        if (sender instanceof Player player) {
            Map<String, String> context = Map.of(
                    "world", player.getWorld().getName(),
                    "gamemode", player.getGameMode().name().toLowerCase(Locale.ROOT));
            return hasPermission(player.getUniqueId(), node, context);
        }
        return sender.hasPermission(node);
    }

    /** Full check with a trace, used by /perm and verbose logging. */
    public CheckResult check(UUID uuid, String node, Map<String, String> context) {
        purgeExpired();
        String query = node.toLowerCase(Locale.ROOT);
        List<ScoredEntry> candidates = new ArrayList<>();
        for (PermissionEntry entry : userPermissions.getOrDefault(uuid, Set.of())) {
            if (!entry.isExpired() && entry.appliesTo(context) && entry.matches(query)) {
                candidates.add(new ScoredEntry(entry, 2, 0));
            }
        }
        candidates.addAll(groups.scoredEntries(userGroups(uuid), query, context));

        CheckResult result;
        if (candidates.isEmpty()) {
            boolean fallback = bukkitFallback(uuid, query);
            result = new CheckResult(fallback,
                    "no ForgeCore grant matched; Bukkit default -> " + onOff(fallback));
        } else {
            ScoredEntry winner = pickWinner(candidates);
            String source = winner.sourceRank == 2 ? "user node" : "group node";
            result = new CheckResult(winner.entry.value(),
                    source + " '" + winner.entry.display() + "' -> " + onOff(winner.entry.value()));
        }
        verboseLog(uuid, query, context, result);
        return result;
    }

    private static String onOff(boolean allowed) {
        return allowed ? "ALLOW" : "DENY";
    }

    /**
     * Most specific entry wins; user entries outrank group entries; higher
     * group weight outranks lower; a final tie resolves to denial.
     */
    private static ScoredEntry pickWinner(List<ScoredEntry> candidates) {
        ScoredEntry best = candidates.get(0);
        for (int i = 1; i < candidates.size(); i++) {
            ScoredEntry next = candidates.get(i);
            if (compare(next, best) > 0) {
                best = next;
            }
        }
        return best;
    }

    private static int compare(ScoredEntry a, ScoredEntry b) {
        int bySpecificity = Integer.compare(a.entry.specificity(), b.entry.specificity());
        if (bySpecificity != 0) {
            return bySpecificity;
        }
        int bySource = Integer.compare(a.sourceRank, b.sourceRank);
        if (bySource != 0) {
            return bySource;
        }
        int byWeight = Integer.compare(a.weight, b.weight);
        if (byWeight != 0) {
            return byWeight;
        }
        // Fail closed: denial wins the final tie.
        return Boolean.compare(!a.entry.value(), !b.entry.value());
    }

    private boolean bukkitFallback(UUID uuid, String node) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null && player.hasPermission(node);
    }

    // ---- verbose ----

    /** Toggle real-time check logging for a viewer; returns the new state. */
    public boolean toggleVerbose(UUID viewer) {
        if (verboseViewers.remove(viewer)) {
            return false;
        }
        verboseViewers.add(viewer);
        return true;
    }

    public boolean toggleVerboseConsole() {
        verboseConsole = !verboseConsole;
        return verboseConsole;
    }

    public boolean isVerbose(UUID viewer) {
        return verboseViewers.contains(viewer);
    }

    private void verboseLog(UUID uuid, String node, Map<String, String> context, CheckResult result) {
        if (verboseViewers.isEmpty() && !verboseConsole) {
            return;
        }
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        String line = "<gray>[Verbose] <white>" + (name == null ? uuid : name)
                + " <gray>" + node + " -> "
                + (result.allowed() ? "<green>ALLOW" : "<red>DENY")
                + " <gray>(" + result.trace() + ")</gray>";
        String plain = com.forge.core.util.Text.strip(line);
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (UUID viewer : verboseViewers) {
                Player player = Bukkit.getPlayer(viewer);
                if (player != null) {
                    player.sendMessage(com.forge.core.util.Text.of(line));
                }
            }
            if (verboseConsole) {
                plugin.getLogger().info(plain);
            }
        });
    }

    // ---- temporary grants ----

    /** Remove expired temporary grants/groups; true when anything was removed. */
    public boolean purgeExpired() {
        boolean removed = false;
        for (Set<PermissionEntry> set : userPermissions.values()) {
            if (set.removeIf(PermissionEntry::isExpired)) {
                removed = true;
            }
        }
        long now = System.currentTimeMillis();
        for (Map<String, Long> map : userGroups.values()) {
            if (map.values().removeIf(expiry -> expiry > 0 && expiry <= now)) {
                removed = true;
            }
        }
        return removed;
    }

    // ---- persistence ----

    /** Persist users, groups and tracks. */
    public void save() {
        dirty = false;
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Set<PermissionEntry>> user : userPermissions.entrySet()) {
            String base = "users." + user.getKey();
            List<Object> perms = new ArrayList<>();
            List<Object> temp = new ArrayList<>();
            for (PermissionEntry entry : user.getValue()) {
                if (entry.isTemporary()) {
                    temp.add(entryMap(entry));
                } else if (entry.contexts().isEmpty()) {
                    perms.add((entry.value() ? "" : "-") + entry.node());
                } else {
                    perms.add(entryMap(entry));
                }
            }
            config.set(base + ".permissions", perms);
            if (!temp.isEmpty()) {
                config.set(base + ".temp-permissions", temp);
            }
            Map<String, Long> memberships = userGroups.getOrDefault(user.getKey(), Map.of());
            List<String> permGroups = new ArrayList<>();
            List<Object> tempGroups = new ArrayList<>();
            for (Map.Entry<String, Long> membership : memberships.entrySet()) {
                if (membership.getValue() == 0) {
                    permGroups.add(membership.getKey());
                } else {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("group", membership.getKey());
                    row.put("expires", membership.getValue());
                    tempGroups.add(row);
                }
            }
            config.set(base + ".groups", permGroups);
            if (!tempGroups.isEmpty()) {
                config.set(base + ".temp-groups", tempGroups);
            }
        }
        for (Group group : groups.groupMap().values()) {
            String base = "groups." + group.name();
            List<Object> perms = new ArrayList<>();
            for (PermissionEntry entry : group.permissions()) {
                if (entry.contexts().isEmpty() && !entry.isTemporary()) {
                    perms.add((entry.value() ? "" : "-") + entry.node());
                } else {
                    perms.add(entryMap(entry));
                }
            }
            config.set(base + ".permissions", perms);
            config.set(base + ".parents", new ArrayList<>(group.parents()));
            config.set(base + ".prefix", group.prefix());
            config.set(base + ".suffix", group.suffix());
            config.set(base + ".weight", group.weight());
        }
        for (Map.Entry<String, List<String>> track : groups.trackMap().entrySet()) {
            config.set("tracks." + track.getKey(), new ArrayList<>(track.getValue()));
        }
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save permissions: " + exception.getMessage());
        }
    }

    private static Map<String, Object> entryMap(PermissionEntry entry) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("node", (entry.value() ? "" : "-") + entry.node());
        if (!entry.contexts().isEmpty()) {
            row.put("contexts", new LinkedHashMap<>(entry.contexts()));
        }
        if (entry.isTemporary()) {
            row.put("expires", entry.expiry());
        }
        return row;
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        var users = config.getConfigurationSection("users");
        if (users != null) {
            for (String id : users.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(id);
                    for (Object raw : users.getList(id + ".permissions", List.of())) {
                        PermissionEntry entry = parseEntry(raw);
                        if (entry != null) {
                            entries(uuid).add(entry);
                        }
                    }
                    for (Object raw : users.getList(id + ".temp-permissions", List.of())) {
                        PermissionEntry entry = parseEntry(raw);
                        if (entry != null) {
                            entries(uuid).add(entry);
                        }
                    }
                    Map<String, Long> memberships = new java.util.concurrent.ConcurrentHashMap<>();
                    for (String group : users.getStringList(id + ".groups")) {
                        memberships.put(group.toLowerCase(Locale.ROOT), 0L);
                    }
                    for (Map<?, ?> row : users.getMapList(id + ".temp-groups")) {
                        Object group = row.get("group");
                        Object expires = row.get("expires");
                        if (group != null && expires instanceof Number number) {
                            memberships.put(String.valueOf(group).toLowerCase(Locale.ROOT),
                                    number.longValue());
                        }
                    }
                    if (!memberships.isEmpty()) {
                        userGroups.put(uuid, memberships);
                    }
                } catch (IllegalArgumentException exception) {
                    plugin.getLogger().warning("Skipping bad permission user id: " + id);
                }
            }
        }
        var groupSection = config.getConfigurationSection("groups");
        if (groupSection != null) {
            for (String id : groupSection.getKeys(false)) {
                Group group = new Group(id);
                for (Object raw : groupSection.getList(id + ".permissions", List.of())) {
                    PermissionEntry entry = parseEntry(raw);
                    if (entry != null) {
                        group.permissions().add(entry);
                    }
                }
                for (String parent : groupSection.getStringList(id + ".parents")) {
                    group.parents().add(parent.toLowerCase(Locale.ROOT));
                }
                group.prefix(groupSection.getString(id + ".prefix", ""));
                group.suffix(groupSection.getString(id + ".suffix", ""));
                group.weight(groupSection.getInt(id + ".weight", 0));
                groups.groupMap().put(id.toLowerCase(Locale.ROOT), group);
            }
        }
        var trackSection = config.getConfigurationSection("tracks");
        if (trackSection != null) {
            for (String id : trackSection.getKeys(false)) {
                List<String> list = new ArrayList<>();
                for (String group : trackSection.getStringList(id)) {
                    list.add(group.toLowerCase(Locale.ROOT));
                }
                groups.trackMap().put(id.toLowerCase(Locale.ROOT), list);
            }
        }
        purgeExpired();
    }

    private static @Nullable PermissionEntry parseEntry(Object raw) {
        if (raw instanceof String text) {
            return PermissionEntry.parse(text);
        }
        if (raw instanceof Map<?, ?> row) {
            Object node = row.get("node");
            if (node == null) {
                return null;
            }
            String text = String.valueOf(node).trim().toLowerCase(Locale.ROOT);
            boolean value = true;
            if (text.startsWith("-")) {
                value = false;
                text = text.substring(1);
            }
            Map<String, String> contexts = new LinkedHashMap<>();
            Object ctx = row.get("contexts");
            if (ctx instanceof Map<?, ?> ctxMap) {
                for (Map.Entry<?, ?> entry : ctxMap.entrySet()) {
                    contexts.put(String.valueOf(entry.getKey()).toLowerCase(Locale.ROOT),
                            String.valueOf(entry.getValue()).toLowerCase(Locale.ROOT));
                }
            }
            long expiry = 0;
            Object expires = row.get("expires");
            if (expires instanceof Number number) {
                expiry = number.longValue();
            }
            return new PermissionEntry(text, value, contexts, expiry);
        }
        return null;
    }
}
