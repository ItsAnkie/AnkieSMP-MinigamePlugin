package com.rensjam.AnkieSMP_MinigamePlugin.minigame.type;

import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;

public abstract class AbstractMaterialMinigame<T> implements MinigameType<T> {

    protected final @NonNull Material readMaterial(@NonNull ConfigurationSection settings, @NonNull String field) {
        String raw = settings.getString(field);
        Material material = raw == null ? null : Material.matchMaterial(raw);
        if (material == null) {
            throw new IllegalArgumentException("Invalid material for " + key() + "." + field + ": " + raw);
        }
        return material;
    }
}
