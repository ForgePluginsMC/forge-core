package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

/** Toggle your name tag visibility via a scoreboard team. */
public final class TagtoggleCommand extends PlayerACommand {
    private static final String TEAM = "forgecore_notag";

    public TagtoggleCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tagtoggle";
    }

    @Override
    public String description() {
        return "Toggle your name tag visibility.";
    }

    @Override
    public String usage() {
        return "/tagtoggle";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        var board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam(TEAM);
        if (team == null) {
            team = board.registerNewTeam(TEAM);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
        if (team.hasPlayer(player)) {
            team.removePlayer(player);
            Text.ok(sender, "Your name tag is <green>visible</green>.");
        } else {
            team.addPlayer(player);
            Text.ok(sender, "Your name tag is <red>hidden</red>.");
        }
    }
}
