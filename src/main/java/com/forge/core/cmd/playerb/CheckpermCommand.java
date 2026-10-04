package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;

/** /checkperm — test whether you have a permission node. */
public final class CheckpermCommand extends ForgeCommand {
    public CheckpermCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "checkperm";
    }

    @Override
    public String description() {
        return "Test whether you have a permission node.";
    }

    @Override
    public String usage() {
        return "/checkperm <permission>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length != 1) {
            Text.usage(sender, usage());
            return;
        }
        boolean has = sender.hasPermission(args[0]);
        Text.send(sender, "You " + (has ? "<green>have</green>" : "<red>do not have</red>")
                + " permission <white>" + Text.escape(args[0]) + "</white>.");
    }
}
