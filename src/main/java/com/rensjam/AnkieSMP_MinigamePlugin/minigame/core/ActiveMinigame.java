package com.rensjam.AnkieSMP_MinigamePlugin.minigame.core;

public record ActiveMinigame<T>(
        MinigameDefinition<T> definition,
        MinigameType<T> type
) {
}
