package com.forge.core.permission;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A permission group: a named bundle of nodes with multiple inheritance,
 * chat prefix/suffix and a weight used for ordering and primary-group
 * resolution.
 */
public final class Group {
    private final String name;
    private final Set<PermissionEntry> permissions = new LinkedHashSet<>();
    private final Set<String> parents = new LinkedHashSet<>();
    private String prefix = "";
    private String suffix = "";
    private int weight;

    public Group(String name) {
        this.name = name.toLowerCase(Locale.ROOT);
    }

    public String name() {
        return name;
    }

    public Set<PermissionEntry> permissions() {
        return permissions;
    }

    /** Parent group names (lowercase), inherited transitively. */
    public Set<String> parents() {
        return parents;
    }

    public String prefix() {
        return prefix;
    }

    public void prefix(String prefix) {
        this.prefix = prefix;
    }

    public String suffix() {
        return suffix;
    }

    public void suffix(String suffix) {
        this.suffix = suffix;
    }

    public int weight() {
        return weight;
    }

    public void weight(int weight) {
        this.weight = weight;
    }

    /** Add or replace the entry for its node pattern. */
    public void setPermission(PermissionEntry entry) {
        permissions.removeIf(existing -> existing.node().equals(entry.node())
                && existing.contexts().equals(entry.contexts()));
        permissions.add(entry);
    }

    /** Remove every entry whose pattern equals the node (any value/context). */
    public boolean removePermission(String node) {
        String key = node.toLowerCase(Locale.ROOT);
        return permissions.removeIf(existing -> existing.node().equals(key));
    }
}
