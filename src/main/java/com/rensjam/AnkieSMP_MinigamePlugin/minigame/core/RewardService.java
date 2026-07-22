package com.rensjam.AnkieSMP_MinigamePlugin.minigame.core;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public final class RewardService {

    private final JavaPlugin plugin;
    private final String rewardCommandTemplate;

    public RewardService(JavaPlugin plugin, String rewardCommandTemplate) {
        this.plugin = plugin;
        this.rewardCommandTemplate = rewardCommandTemplate;
    }

    public void grantReward(@NonNull Player player, int rewardAmount) {
        String command = this.rewardCommandTemplate
                .replace("{player}", player.getName())
                .replace("{reward}", String.valueOf(rewardAmount));

        boolean executed = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        if (!executed) {
            this.plugin.getLogger().warning("Reward command failed: " + command);
            return;
        }

        player.sendMessage(Component.text("Je reward is uitgekeerd: " + rewardAmount));
    }
}
