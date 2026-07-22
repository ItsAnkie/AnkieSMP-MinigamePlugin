package com.rensjam.AnkieSMP_MinigamePlugin.minigame.type;

import com.rensjam.AnkieSMP_MinigamePlugin.AnkieSMP_MinigamePlugin;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameDefinition;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameManager;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public final class InteractionMinigame extends AbstractMaterialMinigame<InteractionMinigame.Settings> {

    private final MinigameManager manager;

    public InteractionMinigame(MinigameManager manager) {
        this.manager = manager;
    }

    @Contract(pure = true)
    @Override
    public @NonNull String key() {
        return "interaction";
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
    public void onInteract(@NonNull PlayerBucketFillEvent event) {
       this.manager.<Settings>getActiveDefinition(this.key()).ifPresent(definition -> {
            ItemStack item = event.getItemStack();

            if (item.getType() != definition.settings().item()) {
                return;
            }

            if (event.getBlock().getType() == Material.WATER) {
                this.manager.incrementProgress(event.getPlayer(), 1);
            }
        });
    }

    public record Settings(Material item) {
    }
}
