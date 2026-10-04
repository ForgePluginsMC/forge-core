package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;

/** Show the server rules. */
public final class RulesCommand extends ForgeCommand {
    public RulesCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "rules";
    }

    @Override
    public List<String> aliases() {
        return List.of("rule");
    }

    @Override
    public String description() {
        return "Show the server rules.";
    }

    @Override
    public String usage() {
        return "/rules";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        List<String> rules = plugin.getConfig().getStringList("rules");
        if (rules.isEmpty()) {
            Text.send(sender, "No rules configured.");
            return;
        }
        Text.send(sender, "<white><bold>Server rules:</bold></white>");
        int i = 1;
        for (String rule : rules) {
            Text.send(sender, "<gray>" + i + ".</gray> " + rule);
            i++;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
