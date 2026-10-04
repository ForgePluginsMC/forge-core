package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /tpaall — send a teleport-here request to every online player. */
public final class TpaAllCommand extends TeleportCommand {
    public TpaAllCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tpaall";
    }

    @Override
    public String description() {
        return "Request every online player to teleport to you.";
    }

    @Override
    public String usage() {
        return "/tpaall";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player from = requirePlayer(sender);
        int sent = 0;
        for (Player target : plugin.getServer().getOnlinePlayers()) {
            if (target.equals(from)) {
                continue;
            }
            if (plugin.tpa().request(from, target, true)) {
                sent++;
                Text.send(target, "<gold><white>" + Text.escape(from.getName()) + "</white> asks you to teleport to them. "
                        + "<gray>Type <white>/tpaccept</white> to accept or <white>/tpdeny</white> to deny.");
            }
        }
        if (sent == 0) {
            throw fail("Nobody is available to receive your request.");
        }
        Text.ok(sender, "Teleport request sent to <white>" + sent + "</white> player" + (sent == 1 ? "" : "s") + ".");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
