package com.forge.core.guild;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Guilds, territory claims, invites and wars, persisted to {@code guilds.yml}.
 *
 * <p>Guild names are case-insensitive. Claims map a chunk to the owning guild's
 * lower-cased name. Wars are guild-vs-guild timed battles scored by kills in
 * enemy territory.
 */
public final class GuildManager {
    /** Chunk claim key: world UID + chunk coords. */
    public record ClaimKey(UUID world, int x, int z) {
        static ClaimKey of(Chunk chunk) {
            return new ClaimKey(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
        }

        String serialize() {
            return world + "," + x + "," + z;
        }

        static @Nullable ClaimKey deserialize(String raw) {
            try {
                String[] parts = raw.split(",", 3);
                return new ClaimKey(UUID.fromString(parts[0]),
                        Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            } catch (RuntimeException exception) {
                return null;
            }
        }
    }

    /** A pending war challenge. */
    private record WarChallenge(String challenger, long expiresAt) {
    }

    /** An active war: scores keyed by lower-cased guild name. */
    public static final class War {
        private final String guildA;
        private final String guildB;
        private final Map<String, Integer> score = new HashMap<>();
        private final long endsAt;

        War(String guildA, String guildB, long endsAt) {
            this.guildA = guildA;
            this.guildB = guildB;
            this.endsAt = endsAt;
            score.put(guildA.toLowerCase(Locale.ROOT), 0);
            score.put(guildB.toLowerCase(Locale.ROOT), 0);
        }

        public String guildA() {
            return guildA;
        }

        public String guildB() {
            return guildB;
        }

        public long endsAt() {
            return endsAt;
        }

        /** True when the guild (any case) is a combatant. */
        public boolean involves(String guildName) {
            String key = guildName.toLowerCase(Locale.ROOT);
            return score.containsKey(key);
        }

        /** The other combatant's lower-cased name. */
        public @Nullable String opponentOf(String guildName) {
            String key = guildName.toLowerCase(Locale.ROOT);
            if (key.equals(guildA.toLowerCase(Locale.ROOT))) {
                return guildB.toLowerCase(Locale.ROOT);
            }
            if (key.equals(guildB.toLowerCase(Locale.ROOT))) {
                return guildA.toLowerCase(Locale.ROOT);
            }
            return null;
        }

        public void addPoint(String guildName) {
            score.merge(guildName.toLowerCase(Locale.ROOT), 1, Integer::sum);
        }

        public int scoreOf(String guildName) {
            return score.getOrDefault(guildName.toLowerCase(Locale.ROOT), 0);
        }
    }

    private final ForgeCore plugin;
    private final File file;
    private final Map<String, Guild> guilds = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<ClaimKey, String> claims = new HashMap<>();
    private final Map<UUID, String> invites = new HashMap<>();
    private final Map<UUID, Long> inviteExpiry = new HashMap<>();
    private final Map<String, WarChallenge> challenges = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final List<War> wars = new ArrayList<>();
    private boolean dirty;

    public GuildManager(ForgeCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "guilds.yml");
        load();
        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> tick(), 20L, 20L);
    }

    /** Cost to found a guild, charged to the founder. */
    public double createCost() {
        return plugin.getConfig().getDouble("guild-create-cost", 1000.0);
    }

    /** Cost per chunk claim, charged to the guild bank. */
    public double claimCost() {
        return plugin.getConfig().getDouble("guild-claim-cost", 100.0);
    }

    /** Max chunks a guild may claim. */
    public int maxClaims() {
        return plugin.getConfig().getInt("guild-max-claims", 25);
    }

    /** Max members per guild. */
    public int maxMembers() {
        return plugin.getConfig().getInt("guild-max-members", 20);
    }

    /** War duration in minutes. */
    public int warMinutes() {
        return plugin.getConfig().getInt("guild-war-minutes", 10);
    }

    /** War challenge expiry in seconds. */
    public int challengeSeconds() {
        return plugin.getConfig().getInt("guild-war-challenge-seconds", 300);
    }

    /** Prize taken from the loser's bank (capped by their balance). */
    public double warPrize() {
        return plugin.getConfig().getDouble("guild-war-prize", 500.0);
    }

    /** Invite expiry in seconds. */
    public int inviteSeconds() {
        return 120;
    }

    /** All guilds, sorted by name. */
    public Collection<Guild> guilds() {
        return guilds.values();
    }

    /** Find a guild by name (any case); null when absent. */
    public @Nullable Guild guild(String name) {
        return guilds.get(name);
    }

    /** The guild a player belongs to; null when guildless. */
    public @Nullable Guild guildOf(UUID uuid) {
        for (Guild guild : guilds.values()) {
            if (guild.isMember(uuid)) {
                return guild;
            }
        }
        return null;
    }

    /** The guild a player belongs to; null when guildless. */
    public @Nullable Guild guildOf(Player player) {
        return guildOf(player.getUniqueId());
    }

    /** Create a guild; null when the name is taken. */
    public @Nullable Guild create(String name, String tag, UUID founder) {
        if (guilds.containsKey(name)) {
            return null;
        }
        Guild guild = new Guild(name, tag, founder);
        guilds.put(name, guild);
        markDirty();
        return guild;
    }

    /** Disband a guild, releasing its claims and ending its wars. */
    public void disband(Guild guild) {
        String key = guild.name().toLowerCase(Locale.ROOT);
        guilds.remove(guild.name());
        claims.entrySet().removeIf(entry -> entry.getValue().equals(key));
        List<War> finished = new ArrayList<>();
        for (War war : wars) {
            if (war.involves(guild.name())) {
                finished.add(war);
            }
        }
        for (War war : finished) {
            String opponentKey = war.opponentOf(guild.name());
            Guild opponent = opponentKey == null ? null : guilds.get(opponentKey);
            wars.remove(war);
            if (opponent != null) {
                awardWarWin(opponent, guild.name(), true);
            }
        }
        challenges.entrySet().removeIf(entry ->
                entry.getKey().equalsIgnoreCase(guild.name())
                        || entry.getValue().challenger().equalsIgnoreCase(guild.name()));
        markDirty();
    }

    /** Owning guild's lower-cased name for a chunk; null when wilderness. */
    public @Nullable String claimOwner(ClaimKey key) {
        return claims.get(key);
    }

    /** Owning guild for a chunk; null when wilderness or guild gone. */
    public @Nullable Guild claimOwnerGuild(Chunk chunk) {
        String owner = claims.get(ClaimKey.of(chunk));
        return owner == null ? null : guilds.get(owner);
    }

    /** Number of chunks claimed by a guild. */
    public int claimCount(Guild guild) {
        String key = guild.name().toLowerCase(Locale.ROOT);
        int count = 0;
        for (String owner : claims.values()) {
            if (owner.equals(key)) {
                count++;
            }
        }
        return count;
    }

    /** All claims owned by a guild. */
    public List<ClaimKey> claimsOf(Guild guild) {
        String key = guild.name().toLowerCase(Locale.ROOT);
        List<ClaimKey> result = new ArrayList<>();
        for (Map.Entry<ClaimKey, String> entry : claims.entrySet()) {
            if (entry.getValue().equals(key)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** Claim a chunk; false when already claimed. */
    public boolean claim(Guild guild, Chunk chunk) {
        ClaimKey key = ClaimKey.of(chunk);
        if (claims.containsKey(key)) {
            return false;
        }
        claims.put(key, guild.name().toLowerCase(Locale.ROOT));
        markDirty();
        return true;
    }

    /** Release a chunk claim; false when it was wilderness. */
    public boolean unclaim(Chunk chunk) {
        boolean removed = claims.remove(ClaimKey.of(chunk)) != null;
        if (removed) {
            markDirty();
        }
        return removed;
    }

    /** Invite a player to a guild (overwrites any prior invite). */
    public void invite(UUID player, String guildName) {
        invites.put(player, guildName);
        inviteExpiry.put(player, System.currentTimeMillis() + inviteSeconds() * 1000L);
        markDirty();
    }

    /** Pending invite's guild name; null when none or expired. */
    public @Nullable String inviteOf(UUID player) {
        Long expires = inviteExpiry.get(player);
        if (expires == null || expires < System.currentTimeMillis()) {
            invites.remove(player);
            inviteExpiry.remove(player);
            return null;
        }
        String guildName = invites.get(player);
        if (guildName != null && !guilds.containsKey(guildName)) {
            invites.remove(player);
            inviteExpiry.remove(player);
            return null;
        }
        return guildName;
    }

    /** Clear a player's pending invite. */
    public void clearInvite(UUID player) {
        invites.remove(player);
        inviteExpiry.remove(player);
    }

    /** Active wars (unmodifiable view). */
    public List<War> wars() {
        return List.copyOf(wars);
    }

    /** The active war a guild is fighting; null when at peace. */
    public @Nullable War warOf(String guildName) {
        for (War war : wars) {
            if (war.involves(guildName)) {
                return war;
            }
        }
        return null;
    }

    /** Issue a war challenge; false when either guild is busy or already challenged. */
    public boolean challenge(Guild challenger, Guild target) {
        if (warOf(challenger.name()) != null || warOf(target.name()) != null) {
            return false;
        }
        WarChallenge existing = challenges.get(target.name().toLowerCase(Locale.ROOT));
        if (existing != null
                && existing.expiresAt() > System.currentTimeMillis()
                && guilds.containsKey(existing.challenger())) {
            return false;
        }
        challenges.put(target.name().toLowerCase(Locale.ROOT), new WarChallenge(challenger.name(),
                System.currentTimeMillis() + challengeSeconds() * 1000L));
        return true;
    }

    /** Pending challenger for a guild; null when none or expired. */
    public @Nullable String challengerOf(String guildName) {
        String key = guildName.toLowerCase(Locale.ROOT);
        WarChallenge challenge = challenges.get(key);
        if (challenge == null || challenge.expiresAt() < System.currentTimeMillis()) {
            challenges.remove(key);
            return null;
        }
        if (!guilds.containsKey(challenge.challenger())) {
            challenges.remove(key);
            return null;
        }
        return challenge.challenger();
    }

    /** Start a war between two guilds. */
    public War startWar(Guild guildA, Guild guildB) {
        challenges.remove(guildA.name().toLowerCase(Locale.ROOT));
        challenges.remove(guildB.name().toLowerCase(Locale.ROOT));
        War war = new War(guildA.name(), guildB.name(),
                System.currentTimeMillis() + warMinutes() * 60_000L);
        wars.add(war);
        return war;
    }

    /** Per-second tick: expire challenges, finish ended wars. */
    private void tick() {
        long now = System.currentTimeMillis();
        challenges.entrySet().removeIf(entry -> entry.getValue().expiresAt() < now
                || !guilds.containsKey(entry.getValue().challenger()));
        List<War> finished = new ArrayList<>();
        for (War war : wars) {
            if (war.endsAt() <= now) {
                finished.add(war);
            }
        }
        for (War war : finished) {
            wars.remove(war);
            finishWar(war, false);
        }
    }

    /** Resolve a war: most points wins; winner takes the prize from the loser's bank. */
    private void finishWar(War war, boolean forfeit) {
        Guild guildA = guilds.get(war.guildA().toLowerCase(Locale.ROOT));
        Guild guildB = guilds.get(war.guildB().toLowerCase(Locale.ROOT));
        if (guildA == null || guildB == null) {
            return;
        }
        int scoreA = war.scoreOf(guildA.name());
        int scoreB = war.scoreOf(guildB.name());
        if (scoreA == scoreB && !forfeit) {
            Text.broadcast("<gold>War ended in a draw: <white>" + Text.escape(guildA.tag())
                    + "</white> vs <white>" + Text.escape(guildB.tag()) + "</white> ("
                    + scoreA + " - " + scoreB + "). No prize awarded.");
            return;
        }
        Guild winner = forfeit ? guildA : (scoreA > scoreB ? guildA : guildB);
        Guild loser = winner == guildA ? guildB : guildA;
        awardWarWin(winner, loser.name(), forfeit);
    }

    /** Pay the war prize from loser to winner and broadcast. */
    private void awardWarWin(Guild winner, String loserName, boolean forfeit) {
        Guild loser = guilds.get(loserName.toLowerCase(Locale.ROOT));
        double prize = 0.0;
        if (loser != null) {
            prize = Math.min(warPrize(), loser.bank());
            loser.bank(loser.bank() - prize);
            markDirty();
        }
        winner.bank(winner.bank() + prize);
        markDirty();
        String reason = forfeit ? "by forfeit" : "in battle";
        Text.broadcast("<gold>Guild war: <white>" + Text.escape(winner.tag()) + "</white> defeated <white>"
                + Text.escape(loserName) + "</white> " + reason + " and claimed <green>"
                + plugin.economy().format(prize) + "</green>!");
    }

    /** Broadcast a message to every online member of a guild. */
    public void broadcastToGuild(Guild guild, String miniMessage) {
        for (UUID uuid : guild.members()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                Text.send(player, miniMessage);
            }
        }
    }

    private void markDirty() {
        dirty = true;
    }

    /** Save when dirty. Called on disable. */
    public void save() {
        if (!dirty) {
            return;
        }
        YamlConfiguration config = new YamlConfiguration();
        Map<String, Object> guildMap = new LinkedHashMap<>();
        for (Guild guild : guilds.values()) {
            guildMap.put(guild.name().toLowerCase(Locale.ROOT), guild.serialize());
        }
        config.set("guilds", guildMap);
        List<String> claimList = new ArrayList<>();
        for (Map.Entry<ClaimKey, String> entry : claims.entrySet()) {
            claimList.add(entry.getKey().serialize() + ":" + entry.getValue());
        }
        config.set("claims", claimList);
        try {
            config.save(file);
            dirty = false;
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save guilds.yml: " + exception.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Object guildsRaw = config.get("guilds");
        if (guildsRaw instanceof Map<?, ?> guildMap) {
            for (Object value : guildMap.values()) {
                if (value instanceof Map<?, ?> raw) {
                    Guild guild = Guild.deserialize((Map<String, Object>) raw);
                    if (guild != null) {
                        guilds.put(guild.name(), guild);
                    }
                }
            }
        }
        for (String raw : config.getStringList("claims")) {
            int split = raw.lastIndexOf(':');
            if (split < 0) {
                continue;
            }
            String keyRaw = raw.substring(0, split);
            String guildName = raw.substring(split + 1);
            ClaimKey key = ClaimKey.deserialize(keyRaw);
            if (key != null && guilds.containsKey(guildName)) {
                claims.put(key, guildName.toLowerCase(Locale.ROOT));
            }
        }
    }
}
