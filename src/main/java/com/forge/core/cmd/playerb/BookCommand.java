package com.forge.core.cmd.playerb;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * /book — write a signed written book. Pages split around 250 characters;
 * {@code ||} in the text forces a page break.
 */
public final class BookCommand extends ForgeCommand {
    private static final int PAGE_CHARS = 250;

    public BookCommand(ForgeCore plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "book";
    }

    @Override
    public String description() {
        return "Create a signed written book (|| forces a page break).";
    }

    @Override
    public String usage() {
        return "/book <title> <text...>";
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
        if (args.length < 2) {
            Text.usage(sender, usage());
            return;
        }
        String title = args[0].length() > 32 ? args[0].substring(0, 32) : args[0];
        String text = String.join(" ", argsRange(args, 1));
        List<String> pages = paginate(text);
        if (pages.isEmpty()) {
            Text.error(sender, "The book text is empty.");
            return;
        }
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) {
            Text.error(sender, "Could not create the book.");
            return;
        }
        meta.setTitle(title);
        meta.setAuthor(player.getName());
        meta.pages(pages.stream().<Component>map(Component::text).toList());
        book.setItemMeta(meta);
        player.getInventory().addItem(book);
        Text.ok(sender, "Wrote <white>" + Text.escape(title) + "</white> (" + pages.size() + " pages).");
    }

    private static String[] argsRange(String[] args, int from) {
        String[] out = new String[args.length - from];
        System.arraycopy(args, from, out, 0, out.length);
        return out;
    }

    /** Split text into pages; "||" forces a break, otherwise hard-cut at 250 chars. */
    static List<String> paginate(String text) {
        List<String> pages = new ArrayList<>();
        for (String forced : text.split("\\|\\|", -1)) {
            String remaining = forced.strip();
            while (remaining.length() > PAGE_CHARS) {
                pages.add(remaining.substring(0, PAGE_CHARS));
                remaining = remaining.substring(PAGE_CHARS).stripLeading();
            }
            if (!remaining.isEmpty() || forced.isEmpty()) {
                pages.add(remaining);
            }
        }
        pages.removeIf(String::isEmpty);
        return pages;
    }
}
