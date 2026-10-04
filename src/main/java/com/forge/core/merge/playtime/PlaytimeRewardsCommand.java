package com.forge.core.merge.playtime;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /playtimerewards} — opens the claimable playtime milestone GUI.
 * Admins get {@code /playtimerewards reload} (hot-reloads playtime.yml),
 * folding in the old standalone {@code /fplaytime reload}.
 */
public final class PlaytimeRewardsCommand extends ForgeCommand {
    private final MilestoneManager manager;

    public PlaytimeRewardsCommand(ForgeCore plugin, MilestoneManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @Override
    public String name() {
        return "playtimerewards";
    }

    @Override
    public List<String> aliases() {
        return List.of("prewards", "milestones");
    }

    @Override
    public String description() {
        return "Open the playtime milestone rewards GUI.";
    }

    @Override
    public String usage() {
        return "/playtimerewards [reload]";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length >= 1 && args[0].toLowerCase(Locale.ROOT).equals("reload")) {
            if (!sender.hasPermission("forgecore.playtimerewards.reload")) {
                throw new CommandRegistry.CommandFailure("You don't have permission to do that.");
            }
            manager.reload();
            Text.ok(player, "Playtime milestones reloaded: <white>" + manager.milestones().size()
                    + "</white> milestone(s).");
            return;
        }
        if (args.length >= 1) {
            throw new CommandRegistry.CommandFailure("Unknown subcommand.");
        }
        manager.openGui(player);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("forgecore.playtimerewards.reload")) {
            return List.of("reload");
        }
        return List.of();
    }
}
