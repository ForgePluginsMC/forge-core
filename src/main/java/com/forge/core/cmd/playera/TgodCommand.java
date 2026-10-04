package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.data.UserData;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle god mode for another player. */
public final class TgodCommand extends PlayerACommand {
    public TgodCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tgod";
    }

    @Override
    public String description() {
        return "Toggle god mode for another player.";
    }

    @Override
    public String usage() {
        return "/tgod <player>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target = requiredTarget(sender, args);
        if (target == null) {
            return;
        }
        UserData data = plugin.users().get(target);
        boolean god = !data.god();
        data.setGod(god);
        target.setInvulnerable(god);
        plugin.users().save(target.getUniqueId());
        String state = god ? "<green>enabled</green>" : "<red>disabled</red>";
        Text.ok(sender, "God mode " + state + "<green> for <white>" + Text.escape(target.getName()) + "</white>.");
        Text.send(target, "God mode was " + state + ".");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
