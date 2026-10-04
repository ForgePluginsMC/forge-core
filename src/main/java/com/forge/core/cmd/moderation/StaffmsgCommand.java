package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/** Send a message to all online staff. */
public final class StaffmsgCommand extends ForgeCommand {
    public StaffmsgCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "staffmsg";
    }

    @Override
    public List<String> aliases() {
        return List.of("sm");
    }

    @Override
    public String description() {
        return "Send a message to all online staff.";
    }

    @Override
    public String usage() {
        return "/staffmsg <message...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        String message = String.join(" ", args);
        Staff.notify("<dark_aqua><bold>[Staff]</bold></dark_aqua> <white>" + Text.escape(sender.getName())
                + "<gray>: " + Text.escape(message));
        Text.send(sender, "<gray>Message sent to staff.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
