package com.rensjam.AnkieSMP_MinigamePlugin.minigame.commands;

import com.rensjam.AnkieSMP_MinigamePlugin.minigame.core.MinigameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class ReloadCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final MinigameManager minigameManager;

    public ReloadCommand(@NonNull JavaPlugin plugin, @NonNull MinigameManager minigameManager) {
        this.plugin = plugin;
        this.minigameManager = minigameManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label, String
            @NotNull [] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            this.plugin.reloadConfig();
            this.minigameManager.initialize(this.plugin.getConfig());
            sender.sendMessage(Component.text("§aAnkieMinigames succesvol herladen!"));
            return true;
        }

        sender.sendMessage(Component.text("Gebruik: /ankieminigames reload"));
        return true;
    }
}
