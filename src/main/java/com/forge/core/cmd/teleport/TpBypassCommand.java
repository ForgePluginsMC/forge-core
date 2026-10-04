package com.forge.core.cmd.teleport;

import com.forge.core.ForgeCore;
import com.forge.core.data.UserData;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /tpbypass — toggle ignoring other players' teleport-request toggles. */
public final class TpBypassCommand extends TeleportCommand {
    public TpBypassCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tpbypass";
    }

    @Override
    public String description() {
        return "Toggle ignoring players who disabled teleport requests.";
    }

    @Override
    public String usage() {
        return "/tpbypass";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = requirePlayer(sender);
        UserData data = plugin.users().get(player);
        boolean bypass = !data.getBoolean("tpbypass", false);
        data.setBoolean("tpbypass", bypass);
        plugin.users().save(player.getUniqueId());
        Text.send(sender, bypass
                ? "<gray>Teleport-request bypass <green>enabled</green>."
                : "<gray>Teleport-request bypass <red>disabled</red>.");
    }
}
