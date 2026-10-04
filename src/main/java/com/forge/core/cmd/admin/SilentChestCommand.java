package com.forge.core.cmd.admin;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.data.UserData;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /silentchest — toggle your quiet-container preference.
 *
 * <p>Vanilla container open/close sounds are broadcast by the server and
 * cannot be suppressed per-player without packets, so this is stored as a
 * personal preference flag that ForgeCore's own container features respect.
 */
public final class SilentChestCommand extends ForgeCommand {
    public SilentChestCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "silentchest";
    }

    @Override
    public String description() {
        return "Toggle your quiet-container preference.";
    }

    @Override
    public String usage() {
        return "/silentchest";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        UserData data = plugin.users().get(player);
        boolean on = !data.getBoolean("silent-chest", false);
        data.setBoolean("silent-chest", on);
        plugin.users().save(player.getUniqueId());
        Text.ok(sender, "Quiet containers " + (on ? "<green>enabled</green>" : "<red>disabled</red>") + "<green>.</green>");
        if (on) {
            Text.send(sender, "<gray>ForgeCore features will keep container interactions quiet for you.</gray>");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
