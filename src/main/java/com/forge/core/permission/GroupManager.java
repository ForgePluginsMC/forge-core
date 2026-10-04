package com.forge.core.permission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;

/**
 * Group registry: CRUD, multiple inheritance, prefix/suffix/weight and named
 * promotion tracks. Backed by the shared {@code permissions.yml} store owned
 * by {@link PermissionManager}.
 */
public final class GroupManager {
    private final Map<String, Group> groups = new ConcurrentHashMap<>();
    private final Map<String, List<String>> tracks = new ConcurrentHashMap<>();
    private final PermissionManager permissions;

    GroupManager(PermissionManager permissions) {
        this.permissions = permissions;
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    /** Create a group; false when it already exists. */
    public boolean createGroup(String name) {
        String id = key(name);
        if (groups.containsKey(id)) {
            return false;
        }
        groups.put(id, new Group(id));
        permissions.markDirty();
        return true;
    }

    /** Delete a group and strip it from users, parents and tracks. */
    public boolean deleteGroup(String name) {
        String id = key(name);
        if (groups.remove(id) == null) {
            return false;
        }
        for (Group group : groups.values()) {
            group.parents().remove(id);
        }
        for (List<String> track : tracks.values()) {
            track.removeIf(entry -> entry.equals(id));
        }
        permissions.removeGroupEverywhere(id);
        permissions.markDirty();
        return true;
    }

    public @Nullable Group getGroup(String name) {
        return groups.get(key(name));
    }

    public Set<String> groupNames() {
        return new TreeMap<>(groups).navigableKeySet();
    }

    Map<String, Group> groupMap() {
        return groups;
    }

    /** All groups a group inherits from, transitively, cycle-safe. */
    public Set<String> effectiveParents(String name) {
        Set<String> seen = new LinkedHashSet<>();
        collectParents(key(name), seen);
        seen.remove(key(name));
        return seen;
    }

    private void collectParents(String name, Set<String> seen) {
        if (!seen.add(name)) {
            return;
        }
        Group group = groups.get(name);
        if (group == null) {
            return;
        }
        for (String parent : group.parents()) {
            collectParents(parent, seen);
        }
    }

    /** Every permission entry visible through a group, including inheritance. */
    public Set<PermissionEntry> effectivePermissions(String name) {
        Set<PermissionEntry> out = new LinkedHashSet<>();
        Set<String> chain = new LinkedHashSet<>();
        chain.add(key(name));
        chain.addAll(effectiveParents(name));
        for (String id : chain) {
            Group group = groups.get(id);
            if (group != null) {
                out.addAll(group.permissions());
            }
        }
        return out;
    }

    /**
     * Resolve the winning entry for a node across a set of groups.
     * Used by {@link PermissionManager} for group-sourced candidates.
     */
    List<PermissionManager.ScoredEntry> scoredEntries(Set<String> groupIds, String node,
            Map<String, String> context) {
        List<PermissionManager.ScoredEntry> out = new ArrayList<>();
        for (String id : groupIds) {
            Group group = groups.get(id);
            if (group == null) {
                continue;
            }
            for (PermissionEntry entry : effectivePermissions(id)) {
                if (!entry.isExpired() && entry.appliesTo(context) && entry.matches(node)) {
                    out.add(new PermissionManager.ScoredEntry(entry, 1, group.weight()));
                }
            }
        }
        return out;
    }

    // ---- tracks ----

    public boolean createTrack(String name) {
        String id = key(name);
        if (tracks.containsKey(id)) {
            return false;
        }
        tracks.put(id, new ArrayList<>());
        permissions.markDirty();
        return true;
    }

    public boolean deleteTrack(String name) {
        if (tracks.remove(key(name)) == null) {
            return false;
        }
        permissions.markDirty();
        return true;
    }

    public Set<String> trackNames() {
        return new TreeMap<>(tracks).navigableKeySet();
    }

    public @Nullable List<String> getTrack(String name) {
        List<String> track = tracks.get(key(name));
        return track == null ? null : List.copyOf(track);
    }

    Map<String, List<String>> trackMap() {
        return tracks;
    }

    /** Append a group to a track; false when the track is missing or group unknown. */
    public boolean trackAdd(String track, String group) {
        List<String> list = tracks.get(key(track));
        String gid = key(group);
        if (list == null || !groups.containsKey(gid) || list.contains(gid)) {
            return false;
        }
        list.add(gid);
        permissions.markDirty();
        return true;
    }

    public boolean trackRemove(String track, String group) {
        List<String> list = tracks.get(key(track));
        if (list == null || !list.remove(key(group))) {
            return false;
        }
        permissions.markDirty();
        return true;
    }

    /** Snapshot for persistence. */
    Map<String, List<String>> tracksSnapshot() {
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : tracks.entrySet()) {
            out.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return out;
    }
}
