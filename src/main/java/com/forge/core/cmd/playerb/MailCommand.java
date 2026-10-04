package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import com.forge.core.util.Time;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /mail — send and read offline mail.
 * <ul>
 *   <li>/mail &lt;player&gt; &lt;message...&gt; — send mail</li>
 *   <li>/mail read — read your mail</li>
 *   <li>/mail clear — delete all your mail</li>
 * </ul>
 */
public final class MailCommand extends ForgeCommand {
    public MailCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "mail";
    }

    @Override
    public String description() {
        return "Send and read offline player mail.";
    }

    @Override
    public String usage() {
        return "/mail <player> <message...> | /mail read | /mail clear";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        MailManager mail = PlayerBState.mail();
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        if (args[0].equalsIgnoreCase("read")) {
            Player player = asPlayer(sender);
            if (player == null) {
                Text.error(sender, "Only players can read mail.");
                return;
            }
            var messages = mail.read(player.getUniqueId());
            if (messages.isEmpty()) {
                Text.send(sender, "Your inbox is empty.");
                return;
            }
            Text.send(sender, "<white>Your mail (" + messages.size() + "):</white>");
            for (var m : messages) {
                Text.send(sender, "<gray>From <white>" + Text.escape(m.from()) + "</white>:</gray> "
                        + Text.escape(m.message()));
            }
            return;
        }
        if (args[0].equalsIgnoreCase("clear")) {
            Player player = asPlayer(sender);
            if (player == null) {
                Text.error(sender, "Only players can clear mail.");
                return;
            }
            mail.clear(player.getUniqueId());
            Text.ok(sender, "Mail cleared.");
            return;
        }
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target.getUniqueId() == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            Text.error(sender, "Player <white>" + Text.escape(args[0]) + "</white> has never joined.");
            return;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String from = sender instanceof Player p ? p.getName() : "Console";
        mail.send(target.getUniqueId(), from, message);
        Text.ok(sender, "Mail sent to <white>" + Text.escape(args[0]) + "</white>.");
        Player online = target.getPlayer();
        if (online != null) {
            Text.send(online, "New mail from <white>" + Text.escape(from) + "</white>. Use <white>/mail read</white>.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(
                    java.util.stream.Stream.concat(
                            java.util.stream.Stream.of("read", "clear"),
                            Players.onlineNames().stream()).toList(),
                    args);
        }
        return List.of();
    }
}
