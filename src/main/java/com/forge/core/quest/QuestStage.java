package com.forge.core.quest;

import java.util.List;
import org.jspecify.annotations.NullMarked;

/**
 * One stage of a quest: a set of objectives that must all be completed.
 */
@NullMarked
public final class QuestStage {
    private final String name;
    private final List<QuestObjective> objectives;
    private final QuestReward reward;

    public QuestStage(String name, List<QuestObjective> objectives, QuestReward reward) {
        this.name = name;
        this.objectives = List.copyOf(objectives);
        this.reward = reward;
    }

    public String name() {
        return name;
    }

    public List<QuestObjective> objectives() {
        return objectives;
    }

    public QuestReward reward() {
        return reward;
    }
}
