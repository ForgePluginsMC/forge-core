package com.forge.core.quest;

import org.bukkit.Statistic;
import org.jspecify.annotations.NullMarked;

/**
 * A single quest objective: reach a target value for a vanilla statistic.
 *
 * <p>Progress is measured as {@code current - start}, where {@code start} is
 * the statistic value when the quest (or stage) was accepted.
 */
@NullMarked
public final class QuestObjective {
    private final Statistic stat;
    private final int target;
    private final String description;

    public QuestObjective(Statistic stat, int target, String description) {
        this.stat = stat;
        this.target = target;
        this.description = description;
    }

    public Statistic stat() {
        return stat;
    }

    public int target() {
        return target;
    }

    public String description() {
        return description;
    }
}
