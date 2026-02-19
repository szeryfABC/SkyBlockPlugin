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
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.*;

public class ProtectedBlockBreakObjective extends BukkitCustomObjective implements Listener {

    private final Map<Location, Material> placedBlocksCache = new HashMap<>();

    public ProtectedBlockBreakObjective() {
        setName("ProtectedBlockBreak");
        setAuthor("lemurxd_");

        setItem("DIAMOND_PICKAXE", (short) 0);
        setShowCount(true);

        addStringPrompt("Block ID", "Wpisz id bloków do wykopania (np. STONE,DIAMOND_ORE), oddzielaj przecinkiem", null);
        addStringPrompt("Display Name", "Nazwa wyświetlana w celu (np. Wykop Kamień)", null);

        setDisplay("Wykop %Display Name%: %count%");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        placedBlocksCache.put(block.getLocation(), block.getType());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();

        boolean wasPlaced = false;
        if (placedBlocksCache.containsKey(loc)) {
            if (placedBlocksCache.get(loc) == block.getType()) {
                wasPlaced = true;
            }
            placedBlocksCache.remove(loc);
        }

        if (wasPlaced) return;

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
                incrementObjective(player.getUniqueId(), this, quest, 1);
            }
        }
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        handlePistonMove(event.getBlocks(), event.getDirection());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        handlePistonMove(event.getBlocks(), event.getDirection());
    }

    private void handlePistonMove(List<Block> blocks, BlockFace direction) {
        if (blocks.isEmpty()) return;

        Map<Location, Material> toAdd = new HashMap<>();
        List<Location> toRemove = new ArrayList<>();

        for (Block block : blocks) {
            Location oldLoc = block.getLocation();

            if (placedBlocksCache.containsKey(oldLoc)) {
                Material material = placedBlocksCache.get(oldLoc);
                toRemove.add(oldLoc);

                Location newLoc = oldLoc.clone().add(direction.getModX(), direction.getModY(), direction.getModZ());
                toAdd.put(newLoc, material);
            }
        }

        for (Location loc : toRemove) {
            placedBlocksCache.remove(loc);
        }
        placedBlocksCache.putAll(toAdd);
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
