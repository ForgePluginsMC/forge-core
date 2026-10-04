package com.forge.core.cmd.playera;

import com.forge.core.ForgeCore;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Toggle the glowing outline. */
public final class GlowCommand extends PlayerACommand {
    public GlowCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "glow";
    }

    @Override
    public String description() {
        return "Toggle the glowing outline.";
    }

    @Override
    public String usage() {
        return "/glow [player]";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player target = target(sender, args);
        if (target == null) {
            return;
        }
        boolean glow = !target.isGlowing();
        target.setGlowing(glow);
        String state = glow ? "<green>on</green>" : "<red>off</red>";
        Text.ok(sender, "Glow " + state + "<green> for <white>" + Text.escape(target.getName()) + "</white>.");
        if (!target.equals(sender)) {
            Text.send(target, "Your glow was turned " + state + ".");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return playerNames(args);
    }
}
