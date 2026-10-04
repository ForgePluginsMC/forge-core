package com.forge.core.cmd.economy;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;

/** Fill missing worth entries with sensible defaults. */
public final class GenerateworthCommand extends ForgeCommand {
    public GenerateworthCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "generateworth";
    }

    @Override
    public String description() {
        return "Fill missing sell prices with default values.";
    }

    @Override
    public String usage() {
        return "/generateworth";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        int before = plugin.economy().worthTable().size();
        plugin.economy().generateWorth();
        int added = plugin.economy().worthTable().size() - before;
        Text.ok(sender, "Added <white>" + added + "</white> default price(s).");
    }
}
