package com.rensjam.AnkieSMP_MinigamePlugin.minigame.core;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

public interface MinigameType<T> extends Listener {

    String key();

    MinigameDefinition<T> load(
            String id,
            String displayName,
            int amount,
            int reward,
            ConfigurationSection settings
    );
}
