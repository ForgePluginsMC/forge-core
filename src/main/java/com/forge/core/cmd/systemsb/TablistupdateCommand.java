package com.forge.core.cmd.systemsb;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.playera.Nicks;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import org.bukkit.command.CommandSender;

/** Force-refresh the animated tablist header/footer for everyone. */
public final class TablistupdateCommand extends ForgeCommand {
    public TablistupdateCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tablistupdate";
    }

    @Override
    public String description() {
        return "Force-refresh the animated tablist and player tab entries.";
    }

    @Override
    public String usage() {
        return "/tablistupdate";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        SystemsBSetup.tablist().refresh();
        var online = org.bukkit.Bukkit.getOnlinePlayers();
        for (var player : online) {
            Nicks.apply(plugin, player);
        }
        Text.ok(sender, "Tablist refreshed for <white>" + online.size() + "</white> player(s).");
    }
}
