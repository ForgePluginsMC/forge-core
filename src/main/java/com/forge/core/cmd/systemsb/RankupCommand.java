package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.systemsb.rank.RankManager;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Rank up when you meet the next rank's requirements. */
public final class RankupCommand extends ForgeCommand {
    public RankupCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "rankup";
    }

    @Override
    public String description() {
        return "Rank up when you meet the next rank's requirements.";
    }

    @Override
    public String usage() {
        return "/rankup";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        RankManager ranks = SystemsBSetup.ranks();
        RankManager.RankDef next = ranks.nextOf(player);
        if (next == null) {
            throw new CommandRegistry.CommandFailure("You are already at the highest rank.");
        }
        List<String> missing = ranks.missing(player, next);
        if (!missing.isEmpty()) {
            throw new CommandRegistry.CommandFailure(
                    "You still need: " + String.join(", ", missing) + ".");
        }
        ranks.promote(player, next);
        Text.ok(sender, "Promoted to " + next.display() + "!");
    }
}
