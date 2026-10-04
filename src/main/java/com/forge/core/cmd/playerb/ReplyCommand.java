package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /reply — reply to the player you last exchanged a private message with. */
public final class ReplyCommand extends ForgeCommand {
    public ReplyCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "reply";
    }

    @Override
    public List<String> aliases() {
        return List.of("r");
    }

    @Override
    public String description() {
        return "Reply to your last private-message partner.";
    }

    @Override
    public String usage() {
        return "/reply <message...>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Text.usage(sender, usage());
            return;
        }
        Player fromPlayer = asPlayer(sender);
        UUID fromUuid = MsgManager.uuidOf(fromPlayer);
        UUID targetUuid = MsgManager.get().getLast(fromUuid);
        if (targetUuid == null) {
            Text.error(sender, "You have nobody to reply to.");
            return;
        }
        Player target;
        if (targetUuid.equals(MsgManager.CONSOLE_UUID)) {
            Text.error(sender, "You cannot reply to console that way.");
            return;
        }
        target = plugin.getServer().getPlayer(targetUuid);
        if (target == null) {
            Text.error(sender, "That player is no longer online.");
            return;
        }
        String fromName = fromPlayer == null ? "Console"
                : plugin.users().get(fromPlayer).nickOrName(fromPlayer);
        MsgCommand.deliver(plugin, sender, fromName, fromUuid, target, String.join(" ", args));
    }
}
