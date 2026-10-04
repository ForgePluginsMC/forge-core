package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/** /setmotd — set the server MOTD stored in config.yml. */
public final class SetMotdCommand extends ForgeCommand {
    public SetMotdCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "setmotd";
    }

    @Override
    public String description() {
        return "Set the server MOTD (MiniMessage supported).";
    }

    @Override
    public String usage() {
        return "/setmotd <text...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        String motd = String.join(" ", args);
        plugin.getConfig().set("motd", motd);
        plugin.saveConfig();
        Text.ok(sender, "MOTD set to:");
        Text.send(sender, motd);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
