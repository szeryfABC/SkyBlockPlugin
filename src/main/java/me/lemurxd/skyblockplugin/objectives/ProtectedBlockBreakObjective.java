package me.lemurxd.skyblockplugin.objectives;

import me.pikamug.quests.Quests;
import me.pikamug.quests.events.quester.BukkitQuesterPostCompleteQuestEvent;
import me.pikamug.quests.module.BukkitCustomObjective;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.*;

public class ProtectedBlockBreakObjective extends BukkitCustomObjective implements Listener {

    private final Map<UUID, Set<Location>> placedBlocks = new HashMap<>();

    public ProtectedBlockBreakObjective() {
        Bukkit.getPlayer("asd").getInventory().getItemInMainHand().getAmount();
        setName("ProtectedBlockBreak");
        setAuthor("lemurxd_");

        setItem("DIAMOND_PICKAXE", (short) 0);
        setShowCount(true);

        addStringPrompt("Block ID", "Wpisz id bloków do wykopania (np. STONE,DIAMOND_ORE), oddzielaj przecinkiem", null);
        addStringPrompt("Display Name", "Nazwa wyświetlana w celu (np. Wykop Kamień)", null);

        setDisplay("Wykop %Display Name%: %count%");
    }

    @EventHandler
    public void onQuestFinish(BukkitQuesterPostCompleteQuestEvent e) {
        for (Stage stage : e.getQuest().getStages()) {
            if (!stage.getCustomObjectives().isEmpty()) {
                placedBlocks.remove(e.getQuester().getPlayer().getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        Quests quests = (Quests) Bukkit.getPluginManager().getPlugin("Quests");
        if (quests == null) return;

        Quester quester = quests.getQuester(player.getUniqueId());
        if (quester == null) return;

        for (Quest quest : quester.getCurrentQuests().keySet()) {
            Map<String, Object> dataMap = getDataForPlayer(player.getUniqueId(), this, quest);
            if (dataMap == null) continue;

            String blockIds = (String) dataMap.get("Block ID");
            if (blockIds == null) continue;

            if (itemMatches(block.getType(), blockIds)) {
                addPlacedBlock(player.getUniqueId(), block.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();

        Quests quests = (Quests) Bukkit.getPluginManager().getPlugin("Quests");
        if (quests == null) return;

        Quester quester = quests.getQuester(player.getUniqueId());
        if (quester == null) return;

        for (Quest quest : quester.getCurrentQuests().keySet()) {
            Map<String, Object> dataMap = getDataForPlayer(player.getUniqueId(), this, quest);
            if (dataMap == null) continue;

            String blockIds = (String) dataMap.get("Block ID");
            if (blockIds == null) continue;

            if (itemMatches(block.getType(), blockIds)) {

                if (isPlacedBlock(player.getUniqueId(), loc)) {
                    removePlacedBlock(player.getUniqueId(), loc);
                } else {
                    incrementObjective(player.getUniqueId(), this, quest, 1);
                }
            }
        }
    }


    private void addPlacedBlock(UUID uuid, Location loc) {
        placedBlocks.computeIfAbsent(uuid, k -> new HashSet<>()).add(loc);
    }

    private boolean isPlacedBlock(UUID uuid, Location loc) {
        if (!placedBlocks.containsKey(uuid)) return false;
        return placedBlocks.get(uuid).contains(loc);
    }

    private void removePlacedBlock(UUID uuid, Location loc) {
        if (placedBlocks.containsKey(uuid)) {
            Set<Location> locations = placedBlocks.get(uuid);
            locations.remove(loc);

            if (locations.isEmpty()) {
                placedBlocks.remove(uuid);
            }
        }
    }

    private boolean itemMatches(Material material, String blockIds) {
        String[] split = blockIds.split(",");
        for (String s : split) {
            if (material.name().equalsIgnoreCase(s.trim())) {
                return true;
            }
        }
        return false;
    }
}
