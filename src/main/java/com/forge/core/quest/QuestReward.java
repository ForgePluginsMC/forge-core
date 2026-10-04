package com.forge.core.quest;

import java.util.List;
import org.bukkit.Material;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A quest reward: money, items, XP levels, and/or console commands.
 */
@NullMarked
public final class QuestReward {
    private final double money;
    private final int xpLevels;
    private final List<RewardItem> items;
    private final List<String> commands;

    public QuestReward(double money, int xpLevels, List<RewardItem> items, List<String> commands) {
        this.money = money;
        this.xpLevels = xpLevels;
        this.items = List.copyOf(items);
        this.commands = List.copyOf(commands);
    }

    public double money() {
        return money;
    }

    public int xpLevels() {
        return xpLevels;
    }

    public List<RewardItem> items() {
        return items;
    }

    public List<String> commands() {
        return commands;
    }

    public boolean isEmpty() {
        return money <= 0 && xpLevels <= 0 && items.isEmpty() && commands.isEmpty();
    }

    /** A single item reward. */
    @NullMarked
    public record RewardItem(Material material, int amount, @Nullable String name) {
    }
}
