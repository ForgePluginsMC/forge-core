package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Send mail to every player who has ever joined. */
public final class MailallCommand extends ForgeCommand {
    public MailallCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "mailall";
    }

    @Override
    public String description() {
        return "Send mail to all players.";
    }

    @Override
    public String usage() {
        return "/mailall <message...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        String message = String.join(" ", args);
        String from = sender instanceof Player p ? p.getName() : "Console";
        MailManager mail = PlayerBState.mail();
        int count = 0;
        for (var offline : plugin.getServer().getOfflinePlayers()) {
            if (offline.getUniqueId() == null || !offline.hasPlayedBefore()) {
                continue;
            }
            mail.send(offline.getUniqueId(), from, message);
            count++;
            Player online = offline.getPlayer();
            if (online != null) {
                Text.send(online, "New mail from <white>" + Text.escape(from) + "</white>. Use <white>/mail read</white>.");
            }
        }
        Text.ok(sender, "Mail sent to <white>" + count + "</white> players.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
