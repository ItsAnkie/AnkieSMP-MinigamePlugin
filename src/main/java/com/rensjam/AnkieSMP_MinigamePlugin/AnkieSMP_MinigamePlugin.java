package com.rensjam.survivalChallengeSystem;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public final class SurvivalChallengeSystem extends JavaPlugin {

    private final HashMap<UUID, Integer> totalProgress = new HashMap<>();
    Challenge activeChallenge = new Challenge();

    int i = 0;
    int v = 0;
    int index = 0;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ChallengeListener(), this);

        saveDefaultConfig();

        int _announcementsInterval = getConfig().getInt("AnnouncementsInterval");
        int challengeInterval = getConfig().getInt("ChallengeInterval");

        int announcementsInterval = challengeInterval - _announcementsInterval;

        List<?> challenges = getConfig().getList("challenges");

        BukkitScheduler scheduler = this.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                i++;
                v++;
                if (i >= announcementsInterval) {
                    Bukkit.broadcast(Component.text("Er gaat een nieuwe challenge beginnen!"));
                    Bukkit.broadcast(Component.text("Wees voorbereid!"));
                    i = 0;
                }
                else if (v >= challengeInterval) {
                    activeChallenge = new Challenge();
                    totalProgress.clear();
                    Bukkit.broadcast(Component.text("Challenge is begonnen, succes!"));
                    Bukkit.broadcast(Component.text("De gekozen challenge is!: "));

                    if (challenges != null) {
                        if (index == challenges.size())
                            index = 0;
                        if (index == 0)
                            Collections.shuffle(challenges);

                        Map<String, Object> data = (Map<String, Object>) challenges.get(index);

                        activeChallenge.name = (String) data.get("name");
                        activeChallenge.type = (String) data.get("type");
                        activeChallenge.entity = (String) data.get("entity");
                        activeChallenge.amount = (int) data.get("amount");

                        Bukkit.broadcast(Component.text(String.valueOf(challenges.get(index))));
                        index++;

                        i = 0;
                        v = 0;
                    }
                }
            }
        }, 0L, 20L);
    }

    public class ChallengeListener implements Listener {

        @EventHandler
        public void onBlockBreak(BlockBreakEvent event) {
            if (activeChallenge.type.equals("break_block")) {
                if (event.getBlock().getBlockData().getMaterial().equals(Material.getMaterial(activeChallenge.entity))) {
                    Player player = event.getPlayer();
                    UUID uuid = player.getUniqueId();
                    int temp = totalProgress.getOrDefault(uuid, 0);
                    temp++;
                    totalProgress.put(uuid, temp);
                    player.sendMessage(Component.text("You have broken a total of: " + temp + " " + activeChallenge.entity + " / " + activeChallenge.amount));
                    if (temp == (activeChallenge.amount)){
                        temp = 0;
                        totalProgress.clear();
                        player.sendMessage("You have won the challenge");
                        activeChallenge = new Challenge();
                    }
                }
            }
        }

        @EventHandler
        public void onEntityDeath(EntityDeathEvent event) {
            if (activeChallenge.type.equals("kill_entity")) {
                if (event.getEntity().getType().equals(EntityType.fromName(activeChallenge.entity))) {
                    if (event.getEntity().getKiller() != null)
                    {
                        Player player = event.getEntity().getKiller();
                        UUID uuid = player.getUniqueId();
                        int temp = totalProgress.getOrDefault(uuid, 0);
                        temp++;
                        totalProgress.put(uuid, temp);
                        player.sendMessage(Component.text("You have killed a total of: " + temp + " " + activeChallenge.entity + " / " + activeChallenge.amount));
                        if (temp == (activeChallenge.amount)){
                            temp = 0;
                            totalProgress.clear();
                            player.sendMessage("You have won the challenge");
                            activeChallenge = new Challenge();
                        }
                    }
                }
            }
        }
    }

    static public class Challenge {
        public String name, type, entity;
        public int amount;

        public Challenge() {
            this.name = "";
            this.type = "";
            this.entity = "";
            this.amount = 0;
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
