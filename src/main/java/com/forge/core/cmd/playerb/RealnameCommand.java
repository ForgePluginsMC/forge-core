package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Resolve a nickname back to the real username. */
public final class RealnameCommand extends ForgeCommand {
    public RealnameCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "realname";
    }

    @Override
    public List<String> aliases() {
        return List.of("whoisnick");
    }

    @Override
    public String description() {
        return "Find the real username behind a nickname.";
    }

    @Override
    public String usage() {
        return "/realname <nickname>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        String query = Text.strip(args[0]).toLowerCase(java.util.Locale.ROOT);
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            String nick = plugin.users().get(online).nick();
            if (nick != null && Text.strip(nick).equalsIgnoreCase(query)) {
                Text.ok(sender, "<white>" + Text.escape(args[0]) + "</white> is <white>"
                        + Text.escape(online.getName()) + "</white>.");
                return;
            }
        }
        // Also try a direct player-name match in case they typed the real name.
        Player direct = Players.findQuiet(args[0]);
        if (direct != null) {
            Text.ok(sender, "<white>" + Text.escape(direct.getName()) + "</white> is their real name (no nickname set).");
            return;
        }
        Text.error(sender, "No online player has that nickname.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
