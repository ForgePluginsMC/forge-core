package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;

/** /placeholders — list ForgeCore's built-in placeholders. */
public final class PlaceholdersCommand extends ForgeCommand {
    public PlaceholdersCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "placeholders";
    }

    @Override
    public String description() {
        return "List ForgeCore's built-in placeholders.";
    }

    @Override
    public String usage() {
        return "/placeholders";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Text.send(sender, "<white>ForgeCore placeholders</white> <gray>(PlaceholderAPI expands these too when installed):</gray>");
        placeholder(sender, "%player_name%", "the player's name");
        placeholder(sender, "%player_uuid%", "the player's UUID");
        placeholder(sender, "%forgecore_balance%", "economy balance");
        placeholder(sender, "%forgecore_playtime%", "total playtime");
        placeholder(sender, "%forgecore_nick%", "nickname, or name when unset");
        placeholder(sender, "%forgecore_rank%", "rank name (empty unless the rank system set one)");
    }

    private static void placeholder(CommandSender sender, String token, String meaning) {
        Text.send(sender, "<gold>" + Text.escape(token) + "</gold> <gray>— " + Text.escape(meaning) + "</gray>");
    }
}
