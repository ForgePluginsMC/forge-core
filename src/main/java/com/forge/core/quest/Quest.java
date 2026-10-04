package com.forge.core.quest;

import java.util.List;
import org.jspecify.annotations.NullMarked;

/**
 * A quest definition loaded from {@code quests.yml}.
 */
@NullMarked
public final class Quest {
    /** Quest type: normal, daily (resets midnight), or weekly (resets Monday). */
    public enum Type {
        NORMAL, DAILY, WEEKLY
    }

    private final String id;
    private final String name;
    private final String description;
    private final Type type;
    private final List<String> prerequisites;
    private final List<QuestStage> stages;

    public Quest(String id, String name, String description, Type type,
            List<String> prerequisites, List<QuestStage> stages) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.prerequisites = List.copyOf(prerequisites);
        this.stages = List.copyOf(stages);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Type type() {
        return type;
    }

    public List<String> prerequisites() {
        return prerequisites;
    }

    public List<QuestStage> stages() {
        return stages;
    }
}
