package com.rensjam.AnkieSMP_MinigamePlugin.minigame.type;

import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameDefinition;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameManager;
import io.papermc.paper.event.inventory.ItemCraftedEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public final class CraftItemMinigame extends AbstractMaterialMinigame<CraftItemMinigame.Settings> {

    private final MinigameManager manager;

    public CraftItemMinigame(MinigameManager manager) {
        this.manager = manager;
    }

    @Contract(pure = true)
    @Override
    public @NonNull String key() {
        return "craft_item";
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
        return new MinigameDefinition<>(id, displayName, key(), amount, reward, new Settings(this.readMaterial(settings, "item")));
    }

    @EventHandler
    public void onItemCraft(ItemCraftedEvent event) {
        this.manager.<Settings>getActiveDefinition(key()).ifPresent(definition -> {
            if (event.getCraftedItem().getType() != definition.settings().item()) {
                return;
            }

            Player player = event.getPlayer();
            this.manager.incrementProgress(player, 1);
        });
    }

    public record Settings(org.bukkit.Material item) {
    }
}
