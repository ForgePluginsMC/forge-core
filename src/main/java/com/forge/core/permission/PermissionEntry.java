package com.forge.core.permission;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A single permission grant: a node pattern, an allow/deny value, optional
 * contexts the grant is limited to, and an optional expiry timestamp.
 *
 * <p>Patterns: exact nodes ({@code forgecore.fly}), subtree wildcards
 * ({@code forgecore.*} matches {@code forgecore.fly} and deeper), and the
 * global wildcard ({@code *}). A leading {@code -} marks the grant as a
 * denial. Nodes are case-insensitive and stored lowercase.
 */
public final class PermissionEntry {
    private final String node;
    private final boolean value;
    private final Map<String, String> contexts;
    private final long expiry;

    public PermissionEntry(String node, boolean value, Map<String, String> contexts, long expiry) {
        this.node = node.toLowerCase(Locale.ROOT);
        this.value = value;
        this.contexts = Map.copyOf(contexts);
        this.expiry = expiry;
    }

    /** Parse a stored node string: {@code "some.node"} or {@code "-some.node"}. */
    public static PermissionEntry parse(String raw) {
        String text = raw.trim().toLowerCase(Locale.ROOT);
        boolean value = true;
        if (text.startsWith("-")) {
            value = false;
            text = text.substring(1);
        }
        return new PermissionEntry(text, value, Map.of(), 0);
    }

    public String node() {
        return node;
    }

    public boolean value() {
        return value;
    }

    public Map<String, String> contexts() {
        return contexts;
    }

    /** Expiry epoch millis, or 0 for a permanent grant. */
    public long expiry() {
        return expiry;
    }

    public boolean isTemporary() {
        return expiry > 0;
    }

    public boolean isExpired() {
        return expiry > 0 && System.currentTimeMillis() >= expiry;
    }

    /** True when this entry's pattern covers the queried node. */
    public boolean matches(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        if (node.equals("*")) {
            return true;
        }
        if (node.endsWith(".*")) {
            String prefix = node.substring(0, node.length() - 1);
            return q.startsWith(prefix);
        }
        return node.equals(q);
    }

    /**
     * Specificity score: exact nodes outrank wildcards, longer wildcard
     * prefixes outrank shorter ones, {@code *} ranks last.
     */
    public int specificity() {
        if (node.equals("*")) {
            return 0;
        }
        if (node.endsWith(".*")) {
            return node.length() - 2;
        }
        return 100000 + node.length();
    }

    /** True when every context required by this entry is satisfied. */
    public boolean appliesTo(Map<String, String> query) {
        for (Map.Entry<String, String> required : contexts.entrySet()) {
            String actual = query.get(required.getKey());
            if (actual == null || !actual.equalsIgnoreCase(required.getValue())) {
                return false;
            }
        }
        return true;
    }

    /** Human-readable form, e.g. {@code -forgecore.god} or with contexts. */
    public String display() {
        StringBuilder out = new StringBuilder();
        if (!value) {
            out.append('-');
        }
        out.append(node);
        if (!contexts.isEmpty()) {
            out.append(" [");
            boolean first = true;
            for (Map.Entry<String, String> entry : contexts.entrySet()) {
                if (!first) {
                    out.append(", ");
                }
                out.append(entry.getKey()).append('=').append(entry.getValue());
                first = false;
            }
            out.append(']');
        }
        if (isTemporary()) {
            out.append(" (temp)");
        }
        return out.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PermissionEntry entry)) {
            return false;
        }
        return node.equals(entry.node) && value == entry.value
                && contexts.equals(entry.contexts) && expiry == entry.expiry;
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, value, contexts, expiry);
    }
}
