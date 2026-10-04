package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/** Request help from online staff. */
public final class HelpopCommand extends ForgeCommand {
    public HelpopCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "helpop";
    }

    @Override
    public List<String> aliases() {
        return List.of("hop");
    }

    @Override
    public String description() {
        return "Request help from online staff.";
    }

    @Override
    public String usage() {
        return "/helpop <message...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        String message = String.join(" ", args);
        Staff.notify("<yellow><bold>[HelpOp]</bold></yellow> <white>" + Text.escape(sender.getName())
                + "<gray>: " + Text.escape(message));
        Text.send(sender, "<gray>Your request was sent to online staff.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
