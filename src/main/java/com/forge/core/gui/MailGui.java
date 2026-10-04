package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.cmd.playerb.MailManager;
import com.forge.core.cmd.playerb.PlayerBState;
import com.forge.core.util.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Mail inbox: click a message to read it, buttons to clear and compose.
 */
@NullMarked
public final class MailGui extends ForgeGui {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final ForgeCore plugin;
    private final ForgeGui parent;

    public MailGui(ForgeCore plugin, ForgeGui parent) {
        this.plugin = plugin;
        this.parent = parent;
    }

    @Override
    protected Component title() {
        return MM.deserialize("<yellow><bold>Mail Inbox");
    }

    @Override
    protected int size() {
        return 54;
    }

    @Override
    protected @Nullable ForgeGui parent() {
        return parent;
    }

    @Override
    protected void build(Player viewer) {
        MailManager mail = PlayerBState.mail();
        List<MailManager.Mail> messages = mail.read(viewer.getUniqueId());

        int[] slots = gridSlots();
        for (int i = 0; i < messages.size() && i < slots.length; i++) {
            MailManager.Mail msg = messages.get(i);
            String preview = msg.message().length() > 30
                    ? msg.message().substring(0, 30) + "..."
                    : msg.message();
            set(slots[i], GuiItem.of(Material.PAPER)
                    .name("<yellow>From: <white>" + Text.escape(msg.from()))
                    .lore(
                            "<gray>" + Text.escape(preview),
                            "",
                            "<green>Click to read full message")
                    .action(p -> {
                        p.closeInventory();
                        Text.send(p, "<yellow><bold>From " + Text.escape(msg.from()) + ":");
                        Text.send(p, "<white>" + Text.escape(msg.message()));
                    }));
        }

        set(47, GuiItem.of(Material.WRITABLE_BOOK)
                .name("<green>Compose Mail")
                .lore("<gray>Send mail to a player.", "", "<yellow>Click to enter recipient")
                .action(p -> {
                    p.closeInventory();
                    Text.send(p, "<yellow>Type the recipient's name (or 'cancel'):");
                    ChatInput.request(p, recipient -> {
                        if (recipient.equalsIgnoreCase("cancel")) {
                            Text.send(p, "<gray>Cancelled.");
                            return;
                        }
                        Text.send(p, "<yellow>Type your message (or 'cancel'):");
                        ChatInput.request(p, message -> {
                            if (message.equalsIgnoreCase("cancel")) {
                                Text.send(p, "<gray>Cancelled.");
                                return;
                            }
                            p.performCommand("mail " + recipient + " " + message);
                        });
                    });
                }));
        set(49, GuiItem.of(Material.BARRIER)
                .name("<red>Clear Inbox")
                .lore("<gray>Delete all messages.", "", "<red>Click to clear")
                .action(p -> runCommand(p, "mail clear")));
    }

    private static int[] gridSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                slots.add(row * 9 + col);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }
}
