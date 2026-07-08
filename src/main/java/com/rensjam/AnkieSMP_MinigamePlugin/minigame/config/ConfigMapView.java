package com.rensjam.AnkieSMP_MinigamePlugin.minigame.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Optional;

public final class ConfigMapView {

    private final Map<?, ?> raw;

    public ConfigMapView(Map<?, ?> raw) {
        this.raw = raw;
    }

    public @NonNull String requireString(String key) {
        return getString(key)
                .orElseThrow(() -> new IllegalArgumentException("Missing string field: " + key));
    }

    public Optional<String> getString(String key) {
        Object value = raw.get(key);
        if (value == null) {
            return Optional.empty();
        }

        String text = value.toString().trim();
        return text.isEmpty() ? Optional.empty() : Optional.of(text);
    }

    public int requireInt(String... keys) {
        return this.getInt(keys)
                .orElseThrow(() -> new IllegalArgumentException("Missing integer field: " + String.join("/", keys)));
    }

    public Optional<Integer> getInt(String @NonNull ... keys) {
        for (String key : keys) {
            Object value = raw.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof Number number) {
                return Optional.of(number.intValue());
            }
            try {
                return Optional.of(Integer.parseInt(value.toString().trim()));
            } catch (NumberFormatException ignored) {
                throw new IllegalArgumentException("Invalid integer for field: " + key);
            }
        }
        return Optional.empty();
    }

    public @NonNull ConfigurationSection settingsSectionOrWholeMap() {
        Object settings = this.raw.get("settings");
        if (settings instanceof Map<?, ?> settingsMap) {
            return this.toSection(settingsMap);
        }
        return this.toSection(this.raw);
    }

    private @NonNull ConfigurationSection toSection(Map<?, ?> map) {
        MemoryConfiguration section = new MemoryConfiguration();
        populate(section, map);
        return section;
    }

    private void populate(ConfigurationSection section, @NonNull Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nestedMap) {
                ConfigurationSection child = section.createSection(key);
                populate(child, nestedMap);
                continue;
            }
            section.set(key, value);
        }
    }
}
