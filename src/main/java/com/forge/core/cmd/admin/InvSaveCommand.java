package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.CommandRegistry.CommandFailure;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /invsave — save your current inventory under a name. */
public final class InvSaveCommand extends ForgeCommand {
    private static final Pattern NAME = Pattern.compile("[a-z0-9_-]{1,32}");

    public InvSaveCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "invsave";
    }

    @Override
    public String description() {
        return "Save your current inventory under a name.";
    }

    @Override
    public String usage() {
        return "/invsave <name>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            Text.usage(sender, usage());
            return;
        }
        String name = args[0].toLowerCase(Locale.ROOT);
        if (!NAME.matcher(name).matches()) {
            throw new CommandFailure("Name must be 1-32 chars: a-z, 0-9, _ or -.");
        }
        Player player = (Player) sender;
        AdminSetup.inventories().put(name, player.getUniqueId(), player.getInventory());
        Text.ok(sender, "Inventory saved as <white>" + Text.escape(name) + "</white>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
