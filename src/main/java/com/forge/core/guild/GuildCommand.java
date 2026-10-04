package com.forge.core.guild;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * /guild — create, manage and wage war with player guilds.
 *
 * <p>Subcommands: create, invite, accept, deny, kick, promote, demote, leave,
 * transfer, disband, info, bank, home, sethome, list, war, waraccept.
 */
public final class GuildCommand extends ForgeCommand {
    /** Two-step confirm timestamps for destructive actions. */
    private final Map<UUID, Long> confirms = new HashMap<>();

    public GuildCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "guild";
    }

    @Override
    public List<String> aliases() {
        return List.of("gld");
    }

    @Override
    public String description() {
        return "Create and manage player guilds, territory and wars.";
    }

    @Override
    public String usage() {
        return "/guild <create|invite|accept|deny|kick|promote|demote|leave|transfer|disband|info|bank|home|sethome|list|war|waraccept>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "create" -> create(sender, args);
            case "invite" -> invite(sender, args);
            case "accept" -> accept(sender);
            case "deny" -> deny(sender);
            case "kick" -> kick(sender, args);
            case "promote" -> promote(sender, args);
            case "demote" -> demote(sender, args);
            case "leave" -> leave(sender);
            case "transfer" -> transfer(sender, args);
            case "disband" -> disband(sender);
            case "info" -> info(sender, args);
            case "bank" -> bank(sender, args);
            case "home" -> home(sender);
            case "sethome" -> sethome(sender);
            case "list" -> list(sender);
            case "war" -> war(sender, args);
            case "waraccept" -> warAccept(sender);
            default -> Text.usage(sender, usage());
        }
    }

    private GuildManager guilds() {
        return plugin.guilds();
    }

    private @Nullable Guild requireGuild(CommandSender sender) {
        Player player = asPlayer(sender);
        if (player == null) {
            Text.error(sender, "Only players can use that.");
            return null;
        }
        Guild guild = guilds().guildOf(player);
        if (guild == null) {
            Text.error(sender, "You are not in a guild. Create one with <white>/guild create <name> <tag></white>.");
            return null;
        }
        return guild;
    }

    private @Nullable Guild requireOfficer(CommandSender sender) {
        Guild guild = requireGuild(sender);
        if (guild == null) {
            return null;
        }
        Player player = asPlayer(sender);
        if (player != null && !guild.isOfficer(player.getUniqueId())) {
            Text.error(sender, "Only guild officers can do that.");
            return null;
        }
        return guild;
    }

    private @Nullable Guild requireLeader(CommandSender sender) {
        Guild guild = requireGuild(sender);
        if (guild == null) {
            return null;
        }
        Player player = asPlayer(sender);
        if (player != null && !guild.isLeader(player.getUniqueId())) {
            Text.error(sender, "Only the guild leader can do that.");
            return null;
        }
        return guild;
    }

    private static boolean validName(String name) {
        return name.matches("[A-Za-z0-9_]{3,16}");
    }

    private static boolean validTag(String tag) {
        return tag.matches("[A-Za-z0-9]{2,5}");
    }

    private void create(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            Text.error(sender, "Only players can create guilds.");
            return;
        }
        if (args.length != 3) {
            Text.usage(sender, "/guild create <name> <tag>");
            return;
        }
        if (guilds().guildOf(player) != null) {
            Text.error(sender, "You are already in a guild. Leave it first.");
            return;
        }
        String name = args[1];
        String tag = args[2];
        if (!validName(name)) {
            Text.error(sender, "Guild name must be 3-16 letters, numbers or underscores.");
            return;
        }
        if (!validTag(tag)) {
            Text.error(sender, "Guild tag must be 2-5 letters or numbers.");
            return;
        }
        if (guilds().guild(name) != null) {
            Text.error(sender, "A guild named <white>" + Text.escape(name) + "</white> already exists.");
            return;
        }
        double cost = guilds().createCost();
        if (cost > 0 && !plugin.economy().take(player.getUniqueId(), cost)) {
            Text.error(sender, "Creating a guild costs <white>" + plugin.economy().format(cost)
                    + "</white>. You can't afford it.");
            return;
        }
        Guild guild = guilds().create(name, tag, player.getUniqueId());
        if (guild == null) {
            Text.error(sender, "A guild named <white>" + Text.escape(name) + "</white> already exists.");
            if (cost > 0) {
                plugin.economy().add(player.getUniqueId(), cost);
            }
            return;
        }
        guilds().save();
        Text.ok(sender, "Guild <white>" + Text.escape(name) + "</white> [" + Text.escape(tag)
                + "] created! Invite players with <white>/guild invite <player></white>.");
    }

    private void invite(CommandSender sender, String[] args) {
        Guild guild = requireOfficer(sender);
        if (guild == null) {
            return;
        }
        if (args.length != 2) {
            Text.usage(sender, "/guild invite <player>");
            return;
        }
        Player target = Players.find(sender, args[1]);
        if (target == null) {
            return;
        }
        if (guilds().guildOf(target) != null) {
            Text.error(sender, "That player is already in a guild.");
            return;
        }
        if (guild.size() >= guilds().maxMembers()) {
            Text.error(sender, "Your guild is full (" + guilds().maxMembers() + " members).");
            return;
        }
        guilds().invite(target.getUniqueId(), guild.name());
        Text.ok(sender, "Invited <white>" + Text.escape(target.getName()) + "</white> to the guild.");
        Text.send(target, "<gold>[Guild]</gold> <white>" + Text.escape(sender.getName())
                + "</white> invited you to <white>" + Text.escape(guild.name()) + "</white> ["
                + Text.escape(guild.tag()) + "]. Type <white>/guild accept</white> or <white>/guild deny</white>.");
    }

    private void accept(CommandSender sender) {
        Player player = asPlayer(sender);
        if (player == null) {
            Text.error(sender, "Only players can accept invites.");
            return;
        }
        if (guilds().guildOf(player) != null) {
            Text.error(sender, "You are already in a guild.");
            guilds().clearInvite(player.getUniqueId());
            return;
        }
        String guildName = guilds().inviteOf(player.getUniqueId());
        if (guildName == null) {
            Text.error(sender, "You have no pending guild invite.");
            return;
        }
        Guild guild = guilds().guild(guildName);
        if (guild == null) {
            Text.error(sender, "That guild no longer exists.");
            guilds().clearInvite(player.getUniqueId());
            return;
        }
        if (guild.size() >= guilds().maxMembers()) {
            Text.error(sender, "That guild is full.");
            return;
        }
        guild.members().add(player.getUniqueId());
        guilds().clearInvite(player.getUniqueId());
        guilds().save();
        Text.ok(sender, "Welcome to <white>" + Text.escape(guild.name()) + "</white>!");
        guilds().broadcastToGuild(guild, "<gold>[Guild]</gold> <white>" + Text.escape(player.getName())
                + "</white> joined the guild.");
    }

    private void deny(CommandSender sender) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        if (guilds().inviteOf(player.getUniqueId()) == null) {
            Text.error(sender, "You have no pending guild invite.");
            return;
        }
        guilds().clearInvite(player.getUniqueId());
        Text.ok(sender, "Invite declined.");
    }

    private @Nullable UUID resolveMember(CommandSender sender, Guild guild, String name) {
        OfflinePlayer target = Players.offline(name);
        if (target == null || target.getUniqueId() == null) {
            Text.error(sender, "Player not found.");
            return null;
        }
        UUID uuid = target.getUniqueId();
        if (!guild.isMember(uuid)) {
            Text.error(sender, "That player is not in your guild.");
            return null;
        }
        return uuid;
    }

    private static String displayName(UUID uuid) {
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name == null ? "?" : name;
    }

    private void kick(CommandSender sender, String[] args) {
        Guild guild = requireOfficer(sender);
        if (guild == null) {
            return;
        }
        if (args.length != 2) {
            Text.usage(sender, "/guild kick <player>");
            return;
        }
        UUID uuid = resolveMember(sender, guild, args[1]);
        if (uuid == null) {
            return;
        }
        Player self = asPlayer(sender);
        if (self != null && uuid.equals(self.getUniqueId())) {
            Text.error(sender, "Use <white>/guild leave</white> to leave.");
            return;
        }
        if (guild.isLeader(uuid)) {
            Text.error(sender, "You cannot kick the leader.");
            return;
        }
        if (guild.officers().contains(uuid) && self != null && !guild.isLeader(self.getUniqueId())) {
            Text.error(sender, "Only the leader can kick officers.");
            return;
        }
        guild.members().remove(uuid);
        guild.officers().remove(uuid);
        guilds().save();
        Text.ok(sender, "Kicked <white>" + Text.escape(displayName(uuid)) + "</white> from the guild.");
        guilds().broadcastToGuild(guild, "<gold>[Guild]</gold> <white>"
                + Text.escape(displayName(uuid)) + "</white> was kicked.");
    }

    private void promote(CommandSender sender, String[] args) {
        Guild guild = requireLeader(sender);
        if (guild == null) {
            return;
        }
        if (args.length != 2) {
            Text.usage(sender, "/guild promote <player>");
            return;
        }
        UUID uuid = resolveMember(sender, guild, args[1]);
        if (uuid == null) {
            return;
        }
        if (guild.officers().contains(uuid) || guild.isLeader(uuid)) {
            Text.error(sender, "That player is already an officer or the leader.");
            return;
        }
        guild.officers().add(uuid);
        guilds().save();
        Text.ok(sender, "Promoted <white>" + Text.escape(displayName(uuid)) + "</white> to officer.");
    }

    private void demote(CommandSender sender, String[] args) {
        Guild guild = requireLeader(sender);
        if (guild == null) {
            return;
        }
        if (args.length != 2) {
            Text.usage(sender, "/guild demote <player>");
            return;
        }
        UUID uuid = resolveMember(sender, guild, args[1]);
        if (uuid == null) {
            return;
        }
        if (!guild.officers().remove(uuid)) {
            Text.error(sender, "That player is not an officer.");
            return;
        }
        guilds().save();
        Text.ok(sender, "Demoted <white>" + Text.escape(displayName(uuid)) + "</white> to member.");
    }

    private void leave(CommandSender sender) {
        Guild guild = requireGuild(sender);
        Player player = asPlayer(sender);
        if (guild == null || player == null) {
            return;
        }
        if (guild.isLeader(player.getUniqueId())) {
            Text.error(sender, "The leader cannot leave. Transfer leadership with <white>/guild transfer <player></white> or <white>/guild disband</white>.");
            return;
        }
        guild.members().remove(player.getUniqueId());
        guild.officers().remove(player.getUniqueId());
        guilds().save();
        Text.ok(sender, "You left <white>" + Text.escape(guild.name()) + "</white>.");
        guilds().broadcastToGuild(guild, "<gold>[Guild]</gold> <white>"
                + Text.escape(player.getName()) + "</white> left the guild.");
    }

    private void transfer(CommandSender sender, String[] args) {
        Guild guild = requireLeader(sender);
        if (guild == null) {
            return;
        }
        if (args.length != 2) {
            Text.usage(sender, "/guild transfer <player>");
            return;
        }
        UUID uuid = resolveMember(sender, guild, args[1]);
        if (uuid == null) {
            return;
        }
        Player self = asPlayer(sender);
        if (self != null && uuid.equals(self.getUniqueId())) {
            Text.error(sender, "You are already the leader.");
            return;
        }
        guild.officers().add(guild.leader());
        guild.officers().remove(uuid);
        guild.leader(uuid);
        guilds().save();
        Text.ok(sender, "Leadership transferred to <white>" + Text.escape(displayName(uuid)) + "</white>.");
        guilds().broadcastToGuild(guild, "<gold>[Guild]</gold> <white>"
                + Text.escape(displayName(uuid)) + "</white> is now the guild leader.");
    }

    private void disband(CommandSender sender) {
        Guild guild = requireLeader(sender);
        if (guild == null) {
            return;
        }
        Player player = asPlayer(sender);
        if (player != null && !confirmPending(player)) {
            Text.send(sender, "<yellow>Disband <white>" + Text.escape(guild.name())
                    + "</white></yellow>? This releases all claims and the bank is lost. "
                    + "Run <white>/guild disband</white> again to confirm.");
            return;
        }
        String playerName = player == null ? sender.getName() : player.getName();
        int claims = guilds().claimCount(guild);
        guilds().disband(guild);
        guilds().save();
        Text.broadcast("<gold>Guild <white>" + Text.escape(guild.name()) + "</white> was disbanded by <white>"
                + Text.escape(playerName) + "</white> (" + claims + " claims released).");
    }

    private boolean confirmPending(Player player) {
        Long at = confirms.get(player.getUniqueId());
        long now = System.currentTimeMillis();
        if (at != null && now - at < 15_000L) {
            confirms.remove(player.getUniqueId());
            return true;
        }
        confirms.put(player.getUniqueId(), now);
        return false;
    }

    private void info(CommandSender sender, String[] args) {
        Guild guild;
        if (args.length >= 2) {
            Guild found = guilds().guild(args[1]);
            if (found == null) {
                Text.error(sender, "No guild named <white>" + Text.escape(args[1]) + "</white>.");
                return;
            }
            guild = found;
        } else {
            Guild own = requireGuild(sender);
            if (own == null) {
                return;
            }
            guild = own;
        }
        Text.send(sender, "<gold><bold>" + Text.escape(guild.name()) + "</bold></gold> <gray>["
                + Text.escape(guild.tag()) + "]</gray>");
        Text.send(sender, "<gray>Leader:</gray> <white>" + Text.escape(displayName(guild.leader())) + "</white>");
        List<String> officerNames = new ArrayList<>();
        for (UUID uuid : guild.officers()) {
            officerNames.add(displayName(uuid));
        }
        Text.send(sender, "<gray>Officers (" + officerNames.size() + "):</gray> <white>"
                + Text.escape(String.join(", ", officerNames)) + "</white>");
        Text.send(sender, "<gray>Members:</gray> <white>" + guild.size() + "</white>"
                + " <gray>Claims:</gray> <white>" + guilds().claimCount(guild) + "</white>"
                + " <gray>Bank:</gray> <green>" + plugin.economy().format(guild.bank()) + "</green>");
        GuildManager.War war = guilds().warOf(guild.name());
        if (war != null) {
            String opponentKey = war.opponentOf(guild.name());
            Guild opponent = opponentKey == null ? null : guilds().guild(opponentKey);
            String opponentName = opponent == null ? "?" : opponent.name();
            Text.send(sender, "<red>At war with <white>" + Text.escape(opponentName) + "</white> ("
                    + war.scoreOf(guild.name()) + " - " + war.scoreOf(opponentName) + ")</red>");
        }
    }

    private void bank(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Text.usage(sender, "/guild bank <deposit|withdraw> <amount>");
            return;
        }
        Guild guild = requireGuild(sender);
        Player player = asPlayer(sender);
        if (guild == null || player == null) {
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        switch (action) {
            case "deposit" -> {
                if (args.length != 3) {
                    Text.usage(sender, "/guild bank deposit <amount>");
                    return;
                }
                double amount = parseAmount(args[2]);
                if (amount <= 0) {
                    Text.error(sender, "Amount must be positive.");
                    return;
                }
                if (!plugin.economy().take(player.getUniqueId(), amount)) {
                    Text.error(sender, "You can't afford that.");
                    return;
                }
                guild.bank(guild.bank() + amount);
                guilds().save();
                Text.ok(sender, "Deposited <green>" + plugin.economy().format(amount)
                        + "</green> to the guild bank.");
            }
            case "withdraw" -> {
                if (!guild.isOfficer(player.getUniqueId())) {
                    Text.error(sender, "Only guild officers can withdraw.");
                    return;
                }
                if (args.length != 3) {
                    Text.usage(sender, "/guild bank withdraw <amount>");
                    return;
                }
                double amount = parseAmount(args[2]);
                if (amount <= 0) {
                    Text.error(sender, "Amount must be positive.");
                    return;
                }
                if (guild.bank() < amount) {
                    Text.error(sender, "The guild bank only holds <white>"
                            + plugin.economy().format(guild.bank()) + "</white>.");
                    return;
                }
                guild.bank(guild.bank() - amount);
                plugin.economy().add(player.getUniqueId(), amount);
                guilds().save();
                Text.ok(sender, "Withdrew <green>" + plugin.economy().format(amount)
                        + "</green> from the guild bank.");
            }
            default -> Text.usage(sender, "/guild bank <deposit|withdraw> <amount>");
        }
    }

    private double parseAmount(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException exception) {
            return -1.0;
        }
    }

    private void home(CommandSender sender) {
        Guild guild = requireGuild(sender);
        Player player = asPlayer(sender);
        if (guild == null || player == null) {
            return;
        }
        if (guild.home() == null) {
            Text.error(sender, "Your guild has no home set. An officer can set one with <white>/guild sethome</white>.");
            return;
        }
        player.teleport(guild.home());
        Text.ok(sender, "Teleported to the guild home.");
    }

    private void sethome(CommandSender sender) {
        Guild guild = requireOfficer(sender);
        Player player = asPlayer(sender);
        if (guild == null || player == null) {
            return;
        }
        guild.home(player.getLocation());
        guilds().save();
        Text.ok(sender, "Guild home set.");
    }

    private void list(CommandSender sender) {
        if (guilds().guilds().isEmpty()) {
            Text.send(sender, "No guilds exist yet. Create one with <white>/guild create <name> <tag></white>.");
            return;
        }
        Text.send(sender, "<gold><bold>Guilds</bold></gold>");
        for (Guild guild : guilds().guilds()) {
            Text.send(sender, " <white>" + Text.escape(guild.name()) + "</white> <gray>["
                    + Text.escape(guild.tag()) + "]</gray> — " + guild.size() + " members, "
                    + guilds().claimCount(guild) + " claims");
        }
    }

    private void war(CommandSender sender, String[] args) {
        Guild guild = requireOfficer(sender);
        if (guild == null) {
            return;
        }
        if (args.length != 2) {
            Text.usage(sender, "/guild war <guild>");
            return;
        }
        Guild target = guilds().guild(args[1]);
        if (target == null) {
            Text.error(sender, "No guild named <white>" + Text.escape(args[1]) + "</white>.");
            return;
        }
        if (target.name().equalsIgnoreCase(guild.name())) {
            Text.error(sender, "You cannot declare war on your own guild.");
            return;
        }
        if (!guilds().challenge(guild, target)) {
            Text.error(sender, "Either guild is already at war or has a pending challenge.");
            return;
        }
        Text.ok(sender, "War declared on <white>" + Text.escape(target.name())
                + "</white>! They have " + guilds().challengeSeconds()
                + " seconds to accept with <white>/guild waraccept</white>.");
        guilds().broadcastToGuild(target, "<red><bold>War challenge!</bold></red> <white>"
                + Text.escape(guild.name()) + "</white> [" + Text.escape(guild.tag())
                + "] has challenged you to war. <white>/guild waraccept</white> to fight!");
        Text.broadcast("<gold>Guild <white>" + Text.escape(guild.name())
                + "</white> declared war on <white>" + Text.escape(target.name()) + "</white>!");
    }

    private void warAccept(CommandSender sender) {
        Guild guild = requireOfficer(sender);
        if (guild == null) {
            return;
        }
        String challengerName = guilds().challengerOf(guild.name());
        if (challengerName == null) {
            Text.error(sender, "Your guild has no pending war challenge.");
            return;
        }
        Guild challenger = guilds().guild(challengerName);
        if (challenger == null) {
            Text.error(sender, "The challenging guild no longer exists.");
            return;
        }
        guilds().startWar(challenger, guild);
        Text.broadcast("<red><bold>WAR!</bold></red> <white>" + Text.escape(challenger.name())
                + "</white> vs <white>" + Text.escape(guild.name()) + "</white> — "
                + guilds().warMinutes() + " minutes! Kills inside enemy territory score points. "
                + "Winner takes <green>" + plugin.economy().format(guilds().warPrize()) + "</green>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(List.of("create", "invite", "accept", "deny", "kick",
                    "promote", "demote", "leave", "transfer", "disband", "info", "bank",
                    "home", "sethome", "list", "war", "waraccept"), args);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            switch (sub) {
                case "invite", "kick", "promote", "demote", "transfer" ->
                    Players.filter(Players.onlineNames(), args);
                case "info", "war" -> {
                    List<String> names = new ArrayList<>();
                    for (Guild guild : guilds().guilds()) {
                        names.add(guild.name());
                    }
                    return Players.filter(names, args);
                }
                case "bank" -> {
                    return Players.filter(List.of("deposit", "withdraw"), args);
                }
                default -> {
                }
            }
        }
        return List.of();
    }
}
