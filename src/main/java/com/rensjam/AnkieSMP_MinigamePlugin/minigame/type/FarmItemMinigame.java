package com.rensjam.AnkieSMP_MinigamePlugin.minigame.type;

import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameDefinition;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.CaveVines;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class FarmItemMinigame extends AbstractMaterialMinigame<FarmItemMinigame.Settings> {

    private final MinigameManager manager;

    public FarmItemMinigame(MinigameManager manager) {
        this.manager = manager;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String key() {
        return "farm_item";
    }

    @Contract("_, _, _, _, _ -> new")
    @Override
    public @NotNull MinigameDefinition<Settings> load(
            String id,
            String displayName,
            int amount,
            int reward,
            ConfigurationSection settings
    ) {
        return new MinigameDefinition<>(id, displayName, key(), amount, reward, new Settings(this.readMaterial(settings, "block")));
    }

    @EventHandler
    public void onCropHarvest(PlayerHarvestBlockEvent event) {
        this.manager.<Settings>getActiveDefinition(key()).ifPresent(definition -> {
            Block block = event.getHarvestedBlock();
            if (block.getType() != definition.settings().block()) {
                return;
            }

            boolean harvestedGlowBerries = event.getItemsHarvested().stream()
                    .anyMatch(item -> item.getType() == Material.GLOW_BERRIES);

            if (!harvestedGlowBerries) {
                return;
            }

            this.manager.incrementProgress(event.getPlayer(), 1);
        });
    }

    @EventHandler
    public void onCropBreak(BlockBreakEvent event) {
        this.manager.<Settings>getActiveDefinition(key()).ifPresent(definition -> {
            Block block = event.getBlock();
            if (block.getType() != definition.settings().block()) {
                return;
            }
            if (!(block.getBlockData() instanceof Ageable ageable)) {
                return;
            }
            if (ageable.getAge() != ageable.getMaximumAge()) {
                return;
            }

            this.manager.incrementProgress(event.getPlayer(), 1);
        });
    }

    public record Settings(org.bukkit.Material block) {
    }
}
