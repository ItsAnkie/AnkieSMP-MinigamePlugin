package com.rensjam.AnkieSMP_MinigamePlugin.minigame.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public final class MinigameManager {

    private static final TextColor DARK_PURPLE = NamedTextColor.DARK_PURPLE;
    private static final TextColor LIGHT_PURPLE = NamedTextColor.LIGHT_PURPLE;
    private static final TextColor AQUA = NamedTextColor.AQUA;
    private static final TextColor MUTED_GRAY = TextColor.color(163, 163, 178);
    private static final TextColor WHITE = NamedTextColor.WHITE;

    private static Component prefix() {
        return Component.text("[", DARK_PURPLE)
                .append(Component.text("AnkieSMP ", DARK_PURPLE).decorate(TextDecoration.BOLD))
                .append(Component.text("Minigames", LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                .append(Component.text("] ", DARK_PURPLE));
    }

    private final JavaPlugin plugin;
    private final MinigameRegistry registry;
    private final RewardService rewardService;
    private final Random random = new Random();

    private final Map<UUID, Integer> progressByPlayer = new HashMap<>();
    private final List<MinigameDefinition<?>> configuredGames = new ArrayList<>();
    private final Deque<MinigameDefinition<?>> rotationQueue = new ArrayDeque<>();

    private ActiveMinigame<?> activeGame;
    private BukkitTask schedulerTask;

    private int challengeIntervalSeconds;
    private int announcementLeadTimeSeconds;
    private int countdownSeconds;

    public MinigameManager(JavaPlugin plugin, MinigameRegistry registry, RewardService rewardService) {
        this.plugin = plugin;
        this.registry = registry;
        this.rewardService = rewardService;
    }

    public void loadGames(@NonNull FileConfiguration config) {
        this.configuredGames.clear();
        this.rotationQueue.clear();
        this.activeGame = null;
        this.progressByPlayer.clear();

        List<Map<?, ?>> rawGames = config.getMapList("games");

        if (rawGames.isEmpty()) {
            rawGames = config.getMapList("challenges");
        }

        for (Map<?, ?> rawGame : rawGames) {
            try {
                this.configuredGames.add(this.registry.load(rawGame));
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Skipping minigame config entry: " + exception.getMessage());
            }
        }

        if (this.configuredGames.isEmpty()) {
            this.plugin.getLogger().warning("No valid minigames configured.");
        }
    }

    public void initialize(@NonNull FileConfiguration config) {
        this.loadGames(config);
        this.startScheduler(
                config.getInt("challengeIntervalSeconds", config.getInt("ChallengeInterval", 30)),
                config.getInt("announcementLeadTimeSeconds", config.getInt("AnnouncementsInterval", 10))
        );
    }

    public void startScheduler(int challengeIntervalSeconds, int announcementLeadTimeSeconds) {
        this.challengeIntervalSeconds = Math.max(1, challengeIntervalSeconds);
        this.announcementLeadTimeSeconds = Math.max(0, announcementLeadTimeSeconds);

        if (this.announcementLeadTimeSeconds >= this.challengeIntervalSeconds) {
            this.plugin.getLogger().warning(
                    "announcementLeadTimeSeconds must be lower than challengeIntervalSeconds. Clamping value."
            );
            this.announcementLeadTimeSeconds = Math.max(0, this.challengeIntervalSeconds - 1);
        }

        this.countdownSeconds = this.challengeIntervalSeconds;

        if (this.schedulerTask != null) {
            this.schedulerTask.cancel();
        }

        this.schedulerTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tickScheduler, 20L, 20L);
    }

    public void shutdown() {
        if (this.schedulerTask != null) {
            this.schedulerTask.cancel();
            this.schedulerTask = null;
        }

        this.activeGame = null;
        this.progressByPlayer.clear();
        this.rotationQueue.clear();
    }

    public <T> Optional<MinigameDefinition<T>> getActiveDefinition(String typeKey) {
        if (this.activeGame == null || !this.activeGame.definition().typeKey().equals(typeKey)) {
            return Optional.empty();
        }

        @SuppressWarnings("unchecked")
        MinigameDefinition<T> definition = (MinigameDefinition<T>) this.activeGame.definition();

        return Optional.of(definition);
    }

    public void incrementProgress(Player player, int amount) {
        if (this.activeGame == null || amount <= 0) {
            return;
        }

        MinigameDefinition<?> definition = this.activeGame.definition();
        int newProgress = this.progressByPlayer.getOrDefault(player.getUniqueId(), 0) + amount;

        this.progressByPlayer.put(player.getUniqueId(), newProgress);

        player.sendActionBar(
                Component.text("▍", DARK_PURPLE)
                        .append(Component.text(" Voortgang ", MUTED_GRAY))
                        .append(Component.text(newProgress + "/" + definition.amount(), AQUA).decorate(TextDecoration.BOLD))
        );

        if (newProgress >= definition.amount()) {
            this.completeFor(player);
        }
    }

    public void completeFor(Player player) {
        if (this.activeGame == null) {
            return;
        }

        MinigameDefinition<?> completedGame = this.activeGame.definition();

        Bukkit.broadcast(
                prefix()
                        .append(Component.text(player.getName(), LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                        .append(Component.text(" won ", MUTED_GRAY))
                        .append(Component.text(completedGame.displayName(), WHITE).decorate(TextDecoration.BOLD))
        );

        this.rewardService.grantReward(player, completedGame.reward());

        this.progressByPlayer.clear();
        this.activeGame = null;
    }

    private void tickScheduler() {
        if (this.configuredGames.isEmpty()) {
            return;
        }

        this.countdownSeconds--;

        if (this.announcementLeadTimeSeconds > 0 && this.countdownSeconds == this.announcementLeadTimeSeconds) {
            Bukkit.broadcast(
                    prefix()
                            .append(Component.text("Nieuwe minigame over ", MUTED_GRAY))
                            .append(Component.text(this.announcementLeadTimeSeconds + "s", AQUA).decorate(TextDecoration.BOLD))
            );
        }

        if (this.countdownSeconds > 0) {
            return;
        }

        this.startNextGame();
        this.countdownSeconds = this.challengeIntervalSeconds;
    }

    private void startNextGame() {
        MinigameDefinition<?> nextGame = nextRotationGame();

        if (nextGame == null) {
            this.plugin.getLogger().warning("Unable to start a minigame because none are configured.");
            return;
        }

        if (this.activeGame != null) {
            Bukkit.broadcast(
                    prefix().append(Component.text("Niemand won de vorige ronde.", MUTED_GRAY))
            );
        }

        this.progressByPlayer.clear();
        this.activeGame = this.registry.activate(nextGame);

        MinigameDefinition<?> activeDefinition = this.activeGame.definition();

        Bukkit.broadcast(
                prefix()
                        .append(Component.text(activeDefinition.displayName(), WHITE).decorate(TextDecoration.BOLD))
                        .append(Component.text(" is gestart! ", MUTED_GRAY))
                        .append(Component.text("\n", MUTED_GRAY))
                        .append(Component.text("Reward: ", MUTED_GRAY))
                        .append(Component.text(String.valueOf(activeDefinition.reward()), AQUA).decorate(TextDecoration.BOLD))
        );
    }

    private MinigameDefinition<?> nextRotationGame() {
        if (this.rotationQueue.isEmpty()) {
            List<MinigameDefinition<?>> shuffledGames = new ArrayList<>(this.configuredGames);
            Collections.shuffle(shuffledGames, this.random);
            this.rotationQueue.addAll(shuffledGames);
        }

        return this.rotationQueue.pollFirst();
    }

    private <T> @NonNull Component describeObjective(@NonNull ActiveMinigame<T> activeMinigame) {
        return Component.text("Doel: ").append(activeMinigame.type().describeObjective(activeMinigame.definition()));
    }
}