package com.rensjam.AnkieSMP_MinigamePlugin.minigame.type;

import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.CustomColors;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameDefinition;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameManager;
import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameType;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.*;

public final class ChatWordMinigame implements MinigameType<ChatWordMinigame.Settings> {

    private static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();

    private final JavaPlugin plugin;
    private final MinigameManager manager;
    private final Random random = new Random();

    public ChatWordMinigame(@NonNull JavaPlugin plugin, @NonNull MinigameManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public @NonNull String key() {
        return "chat_word";
    }

    @Contract("_, _, _, _, _ -> new")
    @Override
    public @NonNull MinigameDefinition<Settings> load(
            String id,
            String displayName,
            int amount,
            int reward,
            @NonNull ConfigurationSection settings
    ) {
        List<String> configuredWords = new ArrayList<>(settings.getStringList("words"));
        if (configuredWords.isEmpty()) {
            throw new IllegalArgumentException("Missing words for " + key() + ".words");
        }

        List<String> normalizedWords = configuredWords.stream()
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .toList();

        if (normalizedWords.isEmpty()) {
            throw new IllegalArgumentException("No valid words configured for " + key() + ".words");
        }

        boolean ignoreCase = settings.getBoolean("ignore_case", true);
        boolean trimInput = settings.getBoolean("trim_input", true);

        Settings runtimeSettings = new Settings(normalizedWords, null, ignoreCase, trimInput);
        return new MinigameDefinition<>(id, displayName, key(), amount, reward, runtimeSettings);
    }

    @Override
    public @NonNull MinigameDefinition<Settings> activate(@NonNull MinigameDefinition<Settings> definition) {
        List<String> words = definition.settings().words();
        String selectedWord = words.get(this.random.nextInt(words.size()));
        Settings runtimeSettings = definition.settings().withSelectedWord(selectedWord);
        return new MinigameDefinition<>(
                definition.id(),
                definition.displayName(),
                definition.typeKey(),
                definition.amount(),
                definition.reward(),
                runtimeSettings
        );
    }

    @Override
    public @NonNull Component describeObjective(@NonNull MinigameDefinition<Settings> definition) {
        String selectedWord = definition.settings().selectedWord();
        if (selectedWord == null) {
            selectedWord = "(geen woord geselecteerd)";
        }

        return Component
                .text("Typ als eerste ", CustomColors.MUTED_GRAY)
                .append(Component.text(selectedWord, CustomColors.WHITE)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" in de chat!", CustomColors.MUTED_GRAY));
    }

    @EventHandler
    public void onChat(@NonNull AsyncChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        String message = PLAIN_TEXT.serialize(event.message());
        Bukkit.getScheduler().runTask(this.plugin, () -> this.handleChatMessage(event.getPlayer().getUniqueId(), message));
    }

    private void handleChatMessage(UUID playerId, @NonNull String message) {
        this.manager.<Settings>getActiveDefinition(key()).ifPresent(definition -> {
            String selectedWord = definition.settings().selectedWord();
            if (selectedWord == null) {
                return;
            }

            String normalizedMessage = normalize(message, definition.settings());
            String normalizedWord = normalize(selectedWord, definition.settings());
            if (!normalizedWord.equals(normalizedMessage)) {
                return;
            }

            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                return;
            }

            this.manager.incrementProgress(player, 1);
        });
    }

    private @NonNull String normalize(@NonNull String input, @NonNull Settings settings) {
        String normalized = settings.trimInput() ? input.trim() : input;
        return settings.ignoreCase() ? normalized.toLowerCase(Locale.ROOT) : normalized;
    }

    public record Settings(
            @NonNull List<String> words,
            String selectedWord,
            boolean ignoreCase,
            boolean trimInput
    ) {
        public @NonNull Settings withSelectedWord(@NonNull String word) {
            return new Settings(this.words, word, this.ignoreCase, this.trimInput);
        }
    }
}
