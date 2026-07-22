package com.rensjam.AnkieSMP_MinigamePlugin.minigame.type;

import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameDefinition;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameManager;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

public final class KillEntityMinigame implements MinigameType<KillEntityMinigame.Settings> {

    private final MinigameManager manager;

    public KillEntityMinigame(MinigameManager manager) {
        this.manager = manager;
    }

    @Contract(pure = true)
    @Override
    public @NonNull String key() {
        return "kill_entity";
    }

    @Contract("_, _, _, _, _ -> new")
    @Override
    public @NonNull MinigameDefinition<Settings> load(
            String id,
            String displayName,
            int amount,
            int reward,
            ConfigurationSection settings
    ) {
        return new MinigameDefinition<>(id, displayName, key(), amount, reward, new Settings(this.readEntityType(settings, "entity")));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        this.manager.<Settings>getActiveDefinition(key()).ifPresent(definition -> {
            if (event.getEntity().getType() != definition.settings().entity()) {
                return;
            }
            if (event.getEntity().getKiller() == null) {
                return;
            }

            this.manager.incrementProgress(event.getEntity().getKiller(), 1);
        });
    }
    
    private EntityType readEntityType(@NonNull ConfigurationSection settings, String field) {
        String raw = settings.getString(field);
        if (raw == null) {
            throw new IllegalArgumentException("Missing entity for " + key() + "." + field);
        }

        try {
            return EntityType.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid entity for " + key() + "." + field + ": " + raw);
        }
    }

    public record Settings(EntityType entity) {
    }
}
