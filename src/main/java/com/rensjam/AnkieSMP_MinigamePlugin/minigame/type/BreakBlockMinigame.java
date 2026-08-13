package com.rensjam.AnkieSMP_MinigamePlugin.minigame.type;

import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameDefinition;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public final class BreakBlockMinigame extends AbstractMaterialMinigame<BreakBlockMinigame.Settings> {

    private final MinigameManager manager;

    public BreakBlockMinigame(MinigameManager manager) {
        this.manager = manager;
    }

    @Contract(pure = true)
    @Override
    public @NonNull String key() {
        return "break_block";
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
        return new MinigameDefinition<>(id, displayName, key(), amount, reward, new Settings(this.readMaterial(settings, "block")));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        this.manager.<Settings>getActiveDefinition(key()).ifPresent(definition -> {
            if (event.getBlock().getType() == definition.settings().block()) {
                this.manager.incrementProgress(event.getPlayer(), 1);
            }
        });
    }

    public record Settings(org.bukkit.Material block) {
    }
}
