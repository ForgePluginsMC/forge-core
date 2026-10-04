package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Placeholders;
import com.forge.core.util.Players;
import com.forge.core.util.Text;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /ctext — display a named custom text (placeholders expanded). */
public final class CtextCommand extends ForgeCommand {
    public CtextCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "ctext";
    }

    @Override
    public String description() {
        return "Display a custom text created with /editctext.";
    }

    @Override
    public String usage() {
        return "/ctext <name>";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length != 1) {
            Text.usage(sender, usage());
            return;
        }
        CTextManager.CText text = CTextManager.get().get(args[0]);
        if (text == null) {
            Text.error(sender, "No custom text named <white>" + Text.escape(args[0]) + "</white>.");
            return;
        }
        Player player = asPlayer(sender);
        sender.sendMessage(Text.of(Placeholders.apply(player, text.text())));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Players.filter(CTextManager.get().names(), args);
        }
        return List.of();
    }
}
