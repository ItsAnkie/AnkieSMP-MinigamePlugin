package com.rensjam.AnkieSMP_MinigamePlugin.minigame.core;

public record MinigameDefinition<T>(
        String id,
        String displayName,
        String typeKey,
        int amount,
        int reward,
        T settings
) {
}
