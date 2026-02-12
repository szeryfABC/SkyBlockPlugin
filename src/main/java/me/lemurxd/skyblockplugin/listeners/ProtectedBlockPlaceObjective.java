package me.lemurxd.skyblockplugin.listeners;

import me.lemurxd.skyblockplugin.utils.NBTUtil;
import me.pikamug.quests.Quests;
import me.pikamug.quests.module.BukkitCustomObjective;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
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
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ProtectedBlockPlaceObjective extends BukkitCustomObjective implements Listener {

    private static final Map<UUID, Set<Location>> placedBlocks = new HashMap<>();

    private static final String NBT_TAG_KEY = "quest:placed";

    public ProtectedBlockPlaceObjective() {
        setName("ProtectedBlockPlace");
        setAuthor("lemurxd_");

        setItem("DIRT", (short) 3);
        setShowCount(true);

        addStringPrompt("Block ID", "Wpisz id bloków które gracz ma postawić, możesz oddzielać przecinkiem", null);
        addStringPrompt("Block Name", "Wpisz nazwę moba wyświetlaną w statusie zadania", null);

        setDisplay("Postaw: %count%: %Block Name%");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        ItemStack itemInHand = event.getItemInHand();

        addProtectedBlock(player.getUniqueId(), block.getLocation());

        if (NBTUtil.hasTag(itemInHand, NBT_TAG_KEY)) {
            return;
        }

        Quests quests = (Quests) Bukkit.getPluginManager().getPlugin("Quests");
        if (quests == null) return;

        Quester quester = quests.getQuester(player.getUniqueId());
        if (quester == null) return;

        for (Quest quest : quester.getCurrentQuests().keySet()) {
            Map<String, Object> dataMap = getDataForPlayer(player.getUniqueId(), this, quest);
            if (dataMap == null) continue;

            String blockIds = (String) dataMap.get("Block ID");
            if (blockIds == null) continue;

            Material material = block.getType();
            if (itemMatches(material, blockIds)) {
                incrementObjective(player.getUniqueId(), this, quest, 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();

        if (isProtectedBlock(player.getUniqueId(), loc)) {

            removeProtectedBlock(player.getUniqueId(), loc);

            event.setDropItems(false);

            Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());

            if (drops.isEmpty()) return;

            for (ItemStack drop : drops) {
                ItemStack taggedDrop = NBTUtil.setString(drop, NBT_TAG_KEY, "true");

                block.getWorld().dropItemNaturally(loc, taggedDrop);
            }

            event.setExpToDrop(0);
        }
    }


    private void addProtectedBlock(UUID uuid, Location loc) {
        placedBlocks.computeIfAbsent(uuid, k -> new HashSet<>()).add(loc);
    }

    private boolean isProtectedBlock(UUID uuid, Location loc) {
        if (!placedBlocks.containsKey(uuid)) return false;
        return placedBlocks.get(uuid).contains(loc);
    }

    private void removeProtectedBlock(UUID uuid, Location loc) {
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
