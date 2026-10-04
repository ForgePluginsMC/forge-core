package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /me — emote broadcast: "* <name> <message>". */
public final class MeCommand extends ForgeCommand {
    public MeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "me";
    }

    @Override
    public String description() {
        return "Broadcast an emote: * <name> <message>.";
    }

    @Override
    public String usage() {
        return "/me <message...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        String name = sender instanceof Player player
                ? plugin.users().get(player).nickOrName(player)
                : "Console";
        String message = String.join(" ", args);
        plugin.getServer().broadcast(Text.of(
                "<gray>* <white>" + Text.escape(name) + "</white> " + Text.escape(message)));
    }
}
