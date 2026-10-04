package com.forge.core.cmd.systemsa;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Open the custom recipe creator GUI. */
public final class CustomrecipeCommand extends ForgeCommand {
    public CustomrecipeCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "customrecipe";
    }

    @Override
    public List<String> aliases() {
        return List.of("crecipe");
    }

    @Override
    public String description() {
        return "Create custom crafting recipes in a GUI.";
    }

    @Override
    public String usage() {
        return "/customrecipe";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return;
        }
        SystemsASetup.customRecipes().open(player);
        Text.ok(sender, "Place ingredients, set a result, click <green>Save Recipe</green>.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
