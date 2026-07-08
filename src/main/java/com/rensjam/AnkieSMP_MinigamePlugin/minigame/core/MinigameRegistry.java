package com.rensjam.AnkieSMP_MinigamePlugin.minigame.core;

import com.rensjam.AnkieSMP_MinigamePlugin.minigame.config.ConfigMapView;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MinigameRegistry {

    private final JavaPlugin plugin;
    private final Map<String, MinigameType<?>> registeredTypes = new LinkedHashMap<>();

    public MinigameRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(MinigameType<?> type) {
        MinigameType<?> previous = this.registeredTypes.putIfAbsent(type.key(), type);
        if (previous != null) {
            throw new IllegalStateException("Duplicate minigame type: " + type.key());
        }
    }

    public @NonNull MinigameDefinition<?> load(Map<?, ?> rawGame) {
        ConfigMapView config = new ConfigMapView(rawGame);
        String id = config.getString("id").or(() -> config.getString("name"))
                .orElseThrow(() -> new IllegalArgumentException("Missing string field: id/name"));
        String typeKey = config.requireString("type");
        String displayName = config.getString("display_name").or(() -> config.getString("name")).orElse(id);
        int amount = config.requireInt("amount");
        int reward = config.requireInt("reward", "claim_block_reward");
        ConfigurationSection settings = config.settingsSectionOrWholeMap();

        MinigameType<?> rawType = this.registeredTypes.get(typeKey);
        if (rawType == null) {
            throw new IllegalArgumentException("Unknown minigame type '" + typeKey + "' for id '" + id + "'");
        }

        return this.loadTyped(rawType, id, displayName, amount, reward, settings);
    }

    public @NonNull ActiveMinigame<?> activate(@NonNull MinigameDefinition<?> definition) {
        MinigameType<?> rawType = this.registeredTypes.get(definition.typeKey());
        if (rawType == null) {
            throw new IllegalStateException("Minigame type is no longer registered: " + definition.typeKey());
        }

        return this.activateTyped(rawType, definition);
    }

    private <T> @NonNull MinigameDefinition<T> loadTyped(
            MinigameType<?> rawType,
            String id,
            String displayName,
            int amount,
            int reward,
            ConfigurationSection settings
    ) {
        @SuppressWarnings("unchecked")
        MinigameType<T> type = (MinigameType<T>) rawType;
        MinigameDefinition<T> definition = type.load(id, displayName, amount, reward, settings);
        plugin.getLogger().info("Loaded minigame '" + definition.id() + "' of type '" + definition.typeKey() + "'");
        return definition;
    }

    @Contract("_, _ -> new")
    private <T> @NonNull ActiveMinigame<T> activateTyped(MinigameType<?> rawType, MinigameDefinition<?> rawDefinition) {
        @SuppressWarnings("unchecked")
        MinigameType<T> type = (MinigameType<T>) rawType;
        @SuppressWarnings("unchecked")
        MinigameDefinition<T> definition = (MinigameDefinition<T>) rawDefinition;
        return new ActiveMinigame<>(definition, type);
    }
}
