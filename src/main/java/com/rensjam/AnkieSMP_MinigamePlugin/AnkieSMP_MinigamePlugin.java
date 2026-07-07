package com.rensjam.AnkieSMP_MinigamePlugin;

import io.papermc.paper.event.inventory.ItemCraftedEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.CaveVines;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public final class AnkieSMP_MinigamePlugin extends JavaPlugin {

    private final HashMap<UUID, Integer> totalProgress = new HashMap<>();
    Challenge activeChallenge = new Challenge();

    private BukkitTask challengeTask;
    private List<?> challenges;

    int i = 0;
    int v = 0;
    int index = 0;

    private void loadPlugin() {

        reloadConfig();

        int _announcementsInterval = getConfig().getInt("AnnouncementsInterval");
        int challengeInterval = getConfig().getInt("ChallengeInterval");
        int announcementsInterval = challengeInterval - _announcementsInterval;

        challenges = getConfig().getList("challenges");
        if (challengeTask != null) {
            challengeTask.cancel();
        }

        totalProgress.clear();
        activeChallenge = new Challenge();
        i = 0;
        v = 0;
        index = 0;

        challengeTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            i++;
            v++;

            if (i >= announcementsInterval) {
                Bukkit.broadcast(Component.text("Er gaat een nieuwe challenge beginnen!"));
                Bukkit.broadcast(Component.text("Wees voorbereid!"));
                i = 0;
            } else if (v >= challengeInterval) {
                activeChallenge = new Challenge();
                totalProgress.clear();
                Bukkit.broadcast(Component.text("Challenge is begonnen, succes!"));
                Bukkit.broadcast(Component.text("De gekozen challenge is!: "));

                if (challenges == null)
                    return;
                if (index >= challenges.size())
                    index = 0;
                if (index == 0)
                    Collections.shuffle(challenges);

                Map<String, Object> data = (Map<String, Object>) challenges.get(index);

                try {
                    switch ((String) data.get("type")) {

                        case "break_block":
                            activeChallenge.name = (String) data.get("name");
                            activeChallenge.type = (String) data.get("type");
                            activeChallenge.block = Material.getMaterial((String) data.get("block"));
                            activeChallenge.amount = (int) data.get("amount");
                            activeChallenge.claimBlockAmount = (int) data.get("claim_block_reward");
                            break;
                        case "kill_entity":
                            activeChallenge.name = (String) data.get("name");
                            activeChallenge.type = (String) data.get("type");
                            activeChallenge.entity = EntityType.valueOf(data.get("entity").toString());
                            activeChallenge.amount = (int) data.get("amount");
                            activeChallenge.claimBlockAmount = (int) data.get("claim_block_reward");
                            break;
                        case "craft_item":
                            activeChallenge.name = (String) data.get("name");
                            activeChallenge.type = (String) data.get("type");
                            activeChallenge.item = Material.getMaterial((String) data.get("item"));
                            activeChallenge.amount = (int) data.get("amount");
                            activeChallenge.claimBlockAmount = (int) data.get("claim_block_reward");
                            break;
                        case "farm_item":
                            activeChallenge.name = (String) data.get("name");
                            activeChallenge.type = (String) data.get("type");
                            activeChallenge.block = Material.getMaterial((String) data.get("block"));
                            activeChallenge.amount = (int) data.get("amount");
                            activeChallenge.claimBlockAmount = (int) data.get("claim_block_reward");
                            break;
                        case "interaction":
                            activeChallenge.name = (String) data.get("name");
                            activeChallenge.type = (String) data.get("type");
                            activeChallenge.item = Material.getMaterial((String) data.get("item"));
                            activeChallenge.amount = (int) data.get("amount");
                            activeChallenge.claimBlockAmount = (int) data.get("claim_block_reward");
                            break;
                    }
                } catch (Exception ex) {
                    getLogger().warning("Failed to load challenge " + data.get("name") + ": " + ex.getMessage());
                }

                Bukkit.broadcast(Component.text(String.valueOf(challenges.get(index))));
                index++;

                i = 0;
                v = 0;
            }

        }, 0L, 20L);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new ChallengeListener(), this);
        loadPlugin();

        Objects.requireNonNull(getCommand("ankieminigames")).setExecutor(this);
    }

    public class ChallengeListener implements Listener {

        @EventHandler
        public void onBlockBreak(BlockBreakEvent event) {
            if (!activeChallenge.type.equals("break_block"))
                return;

            if (event.getBlock().getType().equals(activeChallenge.block)) {
                Player player = event.getPlayer();
                UUID uuid = player.getUniqueId();
                int temp = totalProgress.getOrDefault(uuid, 0);
                temp++;
                totalProgress.put(uuid, temp);
                player.sendMessage(Component.text("You have broken a total of: " + temp + " " + activeChallenge.block + " / " + activeChallenge.amount));
                if (temp == (activeChallenge.amount)) {
                    temp = 0;
                    totalProgress.clear();
                    player.sendMessage("You have won the challenge");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks " + player.getName() + " " + activeChallenge.claimBlockAmount);
                    activeChallenge = new Challenge();
                }
            }
        }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!activeChallenge.type.equals("kill_entity"))
            return;

        if (event.getEntity().getType().equals(activeChallenge.entity)) {
            if (event.getEntity().getKiller() != null) {
                Player player = event.getEntity().getKiller();
                UUID uuid = player.getUniqueId();
                int temp = totalProgress.getOrDefault(uuid, 0);
                temp++;
                totalProgress.put(uuid, temp);
                player.sendMessage(Component.text("You have killed a total of: " + temp + " " + activeChallenge.entity + " / " + activeChallenge.amount));
                if (temp == (activeChallenge.amount)) {
                    temp = 0;
                    totalProgress.clear();
                    player.sendMessage("You have won the challenge");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks " + player.getName() + " " + activeChallenge.claimBlockAmount);
                    activeChallenge = new Challenge();

                }
            }
        }
    }

    @EventHandler
    public void onItemCraft(ItemCraftedEvent event) {
        if (!activeChallenge.type.equals("craft_item"))
            return;

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
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks " + player.getName() + " " + activeChallenge.claimBlockAmount);
                activeChallenge = new Challenge();

            }
        }
    }

    @EventHandler
    public void onCropHarvest(PlayerHarvestBlockEvent event) {
        if (!activeChallenge.type.equals("farm_item"))
            return;

        Block block = event.getHarvestedBlock();

        if (block.getType() != activeChallenge.block)
            return;

        if (block.getBlockData() instanceof CaveVines vines) {
            if (!vines.hasBerries())
                return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int temp = totalProgress.getOrDefault(uuid, 0);
        temp++;
        totalProgress.put(uuid, temp);
        player.sendMessage(Component.text("You have harvested a total of: " + temp + " " + activeChallenge.block + " / " + activeChallenge.amount));
        if (temp >= activeChallenge.amount) {
            temp = 0;
            totalProgress.clear();
            player.sendMessage(Component.text("You have won the challenge"));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks " + player.getName() + " " + activeChallenge.claimBlockAmount);
            activeChallenge = new Challenge();
        }
    }

    @EventHandler
    public void onCropBreak(BlockBreakEvent event) {
        if (!activeChallenge.type.equals("farm_item"))
            return;
        Block block = event.getBlock();
        if (block.getType() != activeChallenge.block)
            return;
        if (!(block.getBlockData() instanceof Ageable ageable))
            return;
        if (ageable.getAge() != ageable.getMaximumAge())
            return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int temp = totalProgress.getOrDefault(uuid, 0);
        temp++;
        totalProgress.put(uuid, temp);
        player.sendMessage(Component.text("You have harvested a total of: " + temp + " " + activeChallenge.block + " / " + activeChallenge.amount));
        if (temp >= activeChallenge.amount) {
            temp = 0;
            totalProgress.clear();
            player.sendMessage(Component.text("You have won the challenge"));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks " + player.getName() + " " + activeChallenge.claimBlockAmount);
            activeChallenge = new Challenge();
        }
    }

    @EventHandler
    public void onBottleFill(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (!activeChallenge.type.equals("interaction"))
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getItem() == null || event.getItem().getType() != activeChallenge.item)
            return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null)
            return;
        Block waterBlock = clickedBlock.getRelative(event.getBlockFace());
        Bukkit.broadcast(Component.text("Clicked: " + clickedBlock.getType()));
        Bukkit.broadcast(Component.text("Relative: " + waterBlock.getType()));

        if (waterBlock.getType() == Material.WATER || clickedBlock.getType() == Material.CAULDRON) {
            Bukkit.broadcast(Component.text("BLOCK IS WATER"));
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            int temp = totalProgress.getOrDefault(uuid, 0);
            temp++;
            totalProgress.put(uuid, temp);
            player.sendMessage(Component.text("You have filled a total of: " + temp + " " + activeChallenge.item + " / " + activeChallenge.amount));
            if (temp >= activeChallenge.amount) {
                temp = 0;
                totalProgress.clear();
                player.sendMessage(Component.text("You have won the challenge"));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks " + player.getName() + " " + activeChallenge.claimBlockAmount);
                activeChallenge = new Challenge();
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

        public int claimBlockAmount;

        public Challenge() {
            this.name = "";
            this.type = "";

            this.entity = null;
            this.block = null;
            this.item = null;

            this.amount = 0;
            this.claimBlockAmount = 0;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            loadPlugin();
            sender.sendMessage(Component.text("§aAnkieMinigames succesvol herladen!"));
            return true;
        }

        sender.sendMessage(Component.text("Gebruik: /ankieminigames reload"));
        return true;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
