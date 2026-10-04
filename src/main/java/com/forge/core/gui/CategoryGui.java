package com.forge.core.gui;

import com.forge.core.ForgeCore;
import com.forge.core.command.ForgeCommand;
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
 * Auto-generated browser for a category's commands.
 *
 * <p>Click a command: no-arg commands run immediately; commands with required
 * args prompt for chat input. Permission-filtered: commands the viewer can't
 * use show grayed out.
 */
@NullMarked
public final class CategoryGui extends ForgeGui {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final int PAGE_SIZE = 28;

    private final ForgeCore plugin;
    private final MenuCategory category;
    private final ForgeGui parent;
    private final int page;

    public CategoryGui(ForgeCore plugin, MenuCategory category, ForgeGui parent) {
        this(plugin, category, parent, 0);
    }

    private CategoryGui(ForgeCore plugin, MenuCategory category, ForgeGui parent, int page) {
        this.plugin = plugin;
        this.category = category;
        this.parent = parent;
        this.page = page;
    }

    @Override
    protected Component title() {
        return MM.deserialize("<gold><bold>Menu <gray>» " + category.displayName());
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
        List<ForgeCommand> all = new ArrayList<>(category.commands().apply(plugin));
        all.sort((a, b) -> a.name().compareTo(b.name()));

        int totalPages = Math.max(1, (all.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int safePage = Math.min(page, totalPages - 1);
        int start = safePage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, all.size());

        int[] slots = gridSlots();
        int slotIdx = 0;
        for (int i = start; i < end && slotIdx < slots.length; i++) {
            ForgeCommand cmd = all.get(i);
            set(slots[slotIdx++], commandItem(viewer, cmd));
        }

        // Pagination.
        if (safePage > 0) {
            int prev = safePage - 1;
            set(45, GuiItem.of(Material.ARROW)
                    .name("<yellow>Previous Page")
                    .lore("<gray>Page " + (prev + 1) + " of " + totalPages)
                    .action(p -> new CategoryGui(plugin, category, parent, prev).open(p)));
        }
        if (safePage < totalPages - 1) {
            int next = safePage + 1;
            set(53, GuiItem.of(Material.ARROW)
                    .name("<yellow>Next Page")
                    .lore("<gray>Page " + (next + 1) + " of " + totalPages)
                    .action(p -> new CategoryGui(plugin, category, parent, next).open(p)));
        }
        if (totalPages > 1) {
            set(49, GuiItem.of(Material.PAPER)
                    .name("<gray>Page " + (safePage + 1) + " / " + totalPages)
                    .lore("<gray>" + all.size() + " commands"));
        }
    }

    private static int[] gridSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                slots.add(row * 9 + col);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    private GuiItem commandItem(Player viewer, ForgeCommand cmd) {
        boolean allowed = cmd.permission().isEmpty()
                || plugin.permissions().hasPermission(viewer, cmd.permission());
        boolean needsArgs = usageHintsArgs(cmd.usage());

        Material icon = allowed ? Material.LIME_DYE : Material.GRAY_DYE;
        String nameColor = allowed ? "<green>" : "<gray><strikethrough>";
        List<String> lore = new ArrayList<>();
        lore.add("<gray>" + Text.escape(cmd.description()));
        lore.add("");
        lore.add("<dark_gray>/" + cmd.name());
        if (!cmd.aliases().isEmpty()) {
            lore.add("<dark_gray>Aliases: " + String.join(", ", cmd.aliases()));
        }
        lore.add("");
        if (!allowed) {
            lore.add("<red>No permission.");
        } else if (needsArgs) {
            lore.add("<yellow>Click to enter arguments in chat.");
        } else {
            lore.add("<green>Click to run.");
        }

        GuiItem item = GuiItem.of(icon)
                .name(nameColor + "/" + cmd.name())
                .lore(lore);
        if (allowed) {
            if (needsArgs) {
                String base = "/" + cmd.name();
                item.action(p -> {
                    p.closeInventory();
                    Text.send(p, "<yellow>Type arguments for <white>" + base
                            + " <gray>(or 'cancel'). Usage: <white>" + cmd.usage());
                    ChatInput.request(p, input -> {
                        if (input.equalsIgnoreCase("cancel")) {
                            Text.send(p, "<gray>Cancelled.");
                            return;
                        }
                        p.performCommand(cmd.name() + " " + input);
                    });
                });
            } else {
                item.action(p -> runCommand(p, cmd.name()));
            }
        }
        return item;
    }

    /** Heuristic: usage contains [...] or <...> or a space after the command. */
    private static boolean usageHintsArgs(String usage) {
        String trimmed = usage.trim();
        return trimmed.contains("[") || trimmed.contains("<")
                || trimmed.contains("|") || trimmed.contains(" ");
    }
}
