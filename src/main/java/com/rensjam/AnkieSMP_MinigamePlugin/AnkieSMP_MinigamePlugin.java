package com.rensjam.AnkieSMP_MinigamePlugin;

import io.papermc.paper.event.inventory.ItemCraftedEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public final class AnkieSMP_MinigamePlugin extends JavaPlugin {

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

                        switch((String) data.get("type")) {
                            case "break_block":
                                activeChallenge.name = (String) data.get("name");
                                activeChallenge.type = (String) data.get("type");
                                activeChallenge.block = Material.getMaterial((String) data.get("block"));
                                activeChallenge.amount = (int) data.get("amount");
                                break;
                            case "kill_entity":
                                activeChallenge.name = (String) data.get("name");
                                activeChallenge.type = (String) data.get("type");
                                activeChallenge.entity = EntityType.valueOf(data.get("entity").toString().toUpperCase());
                                activeChallenge.amount = (int) data.get("amount");
                                break;
                            case "craft_item":
                                activeChallenge.name = (String) data.get("name");
                                activeChallenge.type = (String) data.get("type");
                                activeChallenge.item = Material.getMaterial((String) data.get("item"));
                                activeChallenge.amount = (int) data.get("amount");
                                break;
                            case "farm_item":
                                activeChallenge.name = (String) data.get("name");
                                activeChallenge.type = (String) data.get("type");
                                activeChallenge.block = Material.getMaterial((String) data.get("block"));
                                activeChallenge.amount = (int) data.get("amount");
                                break;
                            case "fill_item":
                                activeChallenge.name = (String) data.get("name");
                                activeChallenge.type = (String) data.get("type");
                                activeChallenge.item = Material.getMaterial((String) data.get("item"));
                                activeChallenge.amount = (int) data.get("amount");
                                break;
                            default:
                                Bukkit.broadcast(Component.text("Challenge type could not be loaded!"));
                        }

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
                if (event.getBlock().getBlockData().getMaterial().equals(activeChallenge.block)) {
                    Player player = event.getPlayer();
                    UUID uuid = player.getUniqueId();
                    int temp = totalProgress.getOrDefault(uuid, 0);
                    temp++;
                    totalProgress.put(uuid, temp);
                    player.sendMessage(Component.text("You have broken a total of: " + temp + " " + activeChallenge.block + " / " + activeChallenge.amount));
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
                if (event.getEntity().getType().equals(activeChallenge.entity)) {
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

        @EventHandler
        public void onItemCraft(ItemCraftedEvent event) {
            if (activeChallenge.type.equals("craft_item")) {
                if (event.getCraftedItem().getType().equals(activeChallenge.item)) {
                    Player player = event.getPlayer();
                    UUID uuid = player.getUniqueId();
                    int temp = totalProgress.getOrDefault(uuid, 0);
                    temp++;
                    totalProgress.put(uuid, temp);
                    player.sendMessage(Component.text("You have crafted a total of: " + temp + " " + activeChallenge.item + " / " + activeChallenge.amount));
                    if (temp == (activeChallenge.amount)) {
                        temp = 0;
                        totalProgress.clear();
                        player.sendMessage("You have won the challenge");
                        activeChallenge = new Challenge();
                    }
                }
            }
        }

        // Werkt nog niet idk why kut ding xD
        @EventHandler
        public void onCropHarvest(PlayerHarvestBlockEvent event) {
            if (activeChallenge.type.equals("farm_item")) {
                if (event.getHarvestedBlock().getBlockData().getMaterial().equals(activeChallenge.block)) {
                    Player player = event.getPlayer();
                    UUID uuid = player.getUniqueId();
                    int temp = totalProgress.getOrDefault(uuid, 0);
                    temp++;
                    totalProgress.put(uuid, temp);
                    player.sendMessage(Component.text("You have harvested a total of: " + temp + " " + activeChallenge.block + " / " + activeChallenge.amount));
                    if (temp == (activeChallenge.amount)) {
                        temp = 0;
                        totalProgress.clear();
                        player.sendMessage(("You have won the challenge"));
                        activeChallenge = new Challenge();
                    }
                }
            }
        }
    }

    static public class Challenge {

        public String name;
        public String type;

        public EntityType entity;
        public Material block;
        public Material item;

        public int amount;

        public Challenge() {
            this.name = "";
            this.type = "";

            this.entity = null;
            this.block = null;
            this.item = null;

            this.amount = 0;
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}