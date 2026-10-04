package com.forge.core.merge.chat;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/** /chatreload — reload chat.yml. */
public final class ChatreloadCommand extends ForgeCommand {
    public ChatreloadCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "chatreload";
    }

    @Override
    public String description() {
        return "Reload the chat configuration.";
    }

    @Override
    public String usage() {
        return "/chatreload";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        ChatManager manager = ChatManager.get();
        if (manager == null) {
            Text.error(sender, "Chat is not ready yet.");
            return;
        }
        manager.reload();
        Text.ok(sender, "Chat configuration reloaded.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
