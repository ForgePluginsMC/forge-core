package com.forge.core.cmd.moderation;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/** List the server operators. */
public final class OplistCommand extends ForgeCommand {
    public OplistCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "oplist";
    }

    @Override
    public String description() {
        return "List the server operators.";
    }

    @Override
    public String usage() {
        return "/oplist";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        List<String> names = new ArrayList<>();
        for (OfflinePlayer op : Bukkit.getOperators()) {
            String name = op.getName();
            names.add(name == null ? op.getUniqueId().toString() : name);
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        if (names.isEmpty()) {
            Text.send(sender, "There are no operators.");
        } else {
            Text.send(sender, "<gold>Operators (" + names.size() + "):</gold> <white>"
                    + Text.escape(String.join("<gray>, </gray><white>", names)));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
