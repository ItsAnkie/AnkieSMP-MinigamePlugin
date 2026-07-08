package com.rensjam.AnkieSMP_MinigamePlugin;

import com.rensjam.AnkieSMP_MinigamePlugin.minigame.commands.ReloadCommand;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameManager;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameRegistry;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameType;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.RewardService;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.type.BreakBlockMinigame;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.type.FarmItemMinigame;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.type.CraftItemMinigame;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.type.KillEntityMinigame;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.type.InteractionMinigame;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public final class AnkieSMP_MinigamePlugin extends JavaPlugin {

    private MinigameManager minigameManager;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        String rewardCommand = this.getConfig().getString(
                "rewardCommand",
                "adjustbonusclaimblocks {player} {reward}"
        );

        MinigameRegistry registry = new MinigameRegistry(this);
        RewardService rewardService = new RewardService(this, rewardCommand);
        this.minigameManager = new MinigameManager(this, registry, rewardService);

        Objects.requireNonNull(getCommand("ankieminigames")).setExecutor(new ReloadCommand(this.minigameManager, this.getConfig()));

        registerType(registry, new BreakBlockMinigame(this.minigameManager));
        registerType(registry, new KillEntityMinigame(this.minigameManager));
        registerType(registry, new CraftItemMinigame(this.minigameManager));
        registerType(registry, new FarmItemMinigame(this.minigameManager));
        registerType(registry, new InteractionMinigame(this.minigameManager));

        this.minigameManager.loadGames(this.getConfig());

        int challengeIntervalSeconds = this.getConfig().getInt(
                "challengeIntervalSeconds",
                this.getConfig().getInt("ChallengeInterval", 30)
        );
        int announcementLeadTimeSeconds = this.getConfig().getInt(
                "announcementLeadTimeSeconds",
                this.getConfig().getInt("AnnouncementsInterval", 10)
        );

        this.minigameManager.startScheduler(challengeIntervalSeconds, announcementLeadTimeSeconds);
    }

    private void registerType(@NonNull MinigameRegistry registry, MinigameType<?> type) {
        registry.register(type);
        this.getServer().getPluginManager().registerEvents(type, this);
    }

    @Override
    public void onDisable() {
        if (this.minigameManager != null) {
            this.minigameManager.shutdown();
        }
    }
}
