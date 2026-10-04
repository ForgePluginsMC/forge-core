package com.forge.core.guild;

import com.forge.core.util.Locs;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.jspecify.annotations.Nullable;

/**
 * A player-run guild: roster, bank, home, and metadata.
 *
 * <p>Roles: the leader outranks officers, who outrank members. Serialized to
 * {@code guilds.yml} via {@link GuildManager}.
 */
public final class Guild {
    private final String name;
    private String tag;
    private UUID leader;
    private final Set<UUID> officers = new LinkedHashSet<>();
    private final Set<UUID> members = new LinkedHashSet<>();
    private double bank;
    private @Nullable Location home;

    public Guild(String name, String tag, UUID leader) {
        this.name = name;
        this.tag = tag;
        this.leader = leader;
        this.members.add(leader);
    }

    /** Display name as created. */
    public String name() {
        return name;
    }

    /** Short tag shown in chat and territory messages. */
    public String tag() {
        return tag;
    }

    public void tag(String tag) {
        this.tag = tag;
    }

    /** Leader's UUID. */
    public UUID leader() {
        return leader;
    }

    public void leader(UUID leader) {
        this.leader = leader;
    }

    /** Officer UUIDs (leader excluded). */
    public Set<UUID> officers() {
        return officers;
    }

    /** All member UUIDs, including leader and officers. */
    public Set<UUID> members() {
        return members;
    }

    /** Shared guild bank balance. */
    public double bank() {
        return bank;
    }

    public void bank(double bank) {
        this.bank = Math.max(0.0, bank);
    }

    /** Guild home; null when unset. */
    public @Nullable Location home() {
        return home;
    }

    public void home(@Nullable Location home) {
        this.home = home;
    }

    /** True when the UUID holds any role in this guild. */
    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    /** True when the UUID is leader or officer. */
    public boolean isOfficer(UUID uuid) {
        return leader.equals(uuid) || officers.contains(uuid);
    }

    /** True when the UUID is the leader. */
    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }

    /** Total roster size. */
    public int size() {
        return members.size();
    }

    /** Role label for display. */
    public String roleOf(UUID uuid) {
        if (leader.equals(uuid)) {
            return "Leader";
        }
        if (officers.contains(uuid)) {
            return "Officer";
        }
        return "Member";
    }

    /** Serialize to a YAML-friendly map. */
    Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("tag", tag);
        map.put("leader", leader.toString());
        List<String> officerList = new ArrayList<>();
        for (UUID uuid : officers) {
            officerList.add(uuid.toString());
        }
        map.put("officers", officerList);
        List<String> memberList = new ArrayList<>();
        for (UUID uuid : members) {
            memberList.add(uuid.toString());
        }
        map.put("members", memberList);
        map.put("bank", bank);
        if (home != null) {
            map.put("home", Locs.serialize(home));
        }
        return map;
    }

    /** Deserialize; null when the data is malformed. */
    static @Nullable Guild deserialize(Map<String, Object> map) {
        try {
            Object nameRaw = map.get("name");
            Object tagRaw = map.get("tag");
            Object leaderRaw = map.get("leader");
            if (!(nameRaw instanceof String name) || !(tagRaw instanceof String tag)
                    || !(leaderRaw instanceof String leaderStr)) {
                return null;
            }
            Guild guild = new Guild(name, tag, UUID.fromString(leaderStr));
            Object officersRaw = map.get("officers");
            if (officersRaw instanceof List<?> officerList) {
                for (Object entry : officerList) {
                    guild.officers.add(UUID.fromString(String.valueOf(entry)));
                }
            }
            guild.members.clear();
            Object membersRaw = map.get("members");
            if (membersRaw instanceof List<?> memberList) {
                for (Object entry : memberList) {
                    guild.members.add(UUID.fromString(String.valueOf(entry)));
                }
            }
            guild.members.add(guild.leader);
            Object bankRaw = map.get("bank");
            if (bankRaw instanceof Number number) {
                guild.bank = Math.max(0.0, number.doubleValue());
            }
            guild.home = Locs.deserialize(map.get("home"));
            return guild;
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
