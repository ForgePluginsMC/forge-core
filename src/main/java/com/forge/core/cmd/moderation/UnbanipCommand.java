package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/** Remove an IP ban. */
public final class UnbanipCommand extends ForgeCommand {
    public UnbanipCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "unbanip";
    }

    @Override
    public List<String> aliases() {
        return List.of("pardonip");
    }

    @Override
    public String description() {
        return "Remove an IP ban.";
    }

    @Override
    public String usage() {
        return "/unbanip <ip>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        if (ModerationState.ipBans().unban(args[0])) {
            Text.ok(sender, "Unbanned IP <white>" + Text.escape(args[0]) + "</white>.");
        } else {
            Text.error(sender, "That IP is not banned.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return ModerationState.ipBans().all().keySet().stream()
                    .filter(ip -> ip.startsWith(args[args.length - 1]))
                    .sorted().toList();
        }
        return List.of();
    }
}
