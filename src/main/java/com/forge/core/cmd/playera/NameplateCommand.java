package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

/**
 * Set a prefix/suffix shown above your head via a scoreboard team.
 * Usage: /nameplate <prefix> | <suffix> — use "none" to clear a side.
 */
public final class NameplateCommand extends PlayerACommand {
    private static final String TEAM_PREFIX = "forgecore_plate_";

    public NameplateCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "nameplate";
    }

    @Override
    public String description() {
        return "Set prefix/suffix above your head.";
    }

    @Override
    public String usage() {
        return "/nameplate <prefix> | <suffix>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        Player player = (Player) sender;
        String joined = String.join(" ", args);
        String[] parts = joined.split("\\|", 2);
        String prefix = parts[0].trim();
        String suffix = parts.length > 1 ? parts[1].trim() : "";

        var board = Bukkit.getScoreboardManager().getMainScoreboard();
        // Remove player from any previous nameplate team
        for (Team t : board.getTeams()) {
            if (t.getName().startsWith(TEAM_PREFIX) && t.hasPlayer(player)) {
                t.removePlayer(player);
            }
        }
        if ((prefix.equalsIgnoreCase("none") || prefix.isEmpty())
                && (suffix.equalsIgnoreCase("none") || suffix.isEmpty())) {
            Text.ok(sender, "Nameplate cleared.");
            return;
        }
        String teamName = TEAM_PREFIX + player.getUniqueId().toString().substring(0, 8);
        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
        }
        if (!prefix.equalsIgnoreCase("none")) {
            team.prefix(Text.of(prefix + (prefix.isEmpty() ? "" : " ")));
        } else {
            team.prefix(net.kyori.adventure.text.Component.empty());
        }
        if (!suffix.equalsIgnoreCase("none")) {
            team.suffix(Text.of((suffix.isEmpty() ? "" : " ") + suffix));
        } else {
            team.suffix(net.kyori.adventure.text.Component.empty());
        }
        team.addPlayer(player);
        Text.ok(sender, "Nameplate updated.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
