package com.rensjam.AnkieSMP_MinigamePlugin.minigame.core;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NonNull;

public interface MinigameType<T> extends Listener {

    String key();

    MinigameDefinition<T> load(
            String id,
            String displayName,
            int amount,
            int reward,
            ConfigurationSection settings
    );

    default @NonNull MinigameDefinition<T> activate(@NonNull MinigameDefinition<T> definition) {
        return definition;
    }

    default @NonNull Component describeObjective(@NonNull MinigameDefinition<T> definition) {
        return Component.text(definition.displayName());
    }
}
