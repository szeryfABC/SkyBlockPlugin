package me.lemurxd.skyblockplugin.listeners;

import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;
import java.util.UUID;

public class BlockPlace implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (!StoneGenerator.isStoneGeneratorItem(e.getItemInHand())) return;

        Block block = e.getBlockPlaced();

        BentoBox.getInstance().getIslandsManager().getIslandAt(block.getLocation()).ifPresentOrElse(island -> {
            String islandId = island.getUniqueId();
            StoneGenerator.create(islandId, block.getLocation());

            Location stoneLoc = block.getLocation().clone().add(0, 1, 0);
            Block stoneBlock = stoneLoc.getBlock();

            if (stoneBlock.getType().isAir()) {
                stoneBlock.setType(Material.STONE);
            }
        }, () -> {
            e.setCancelled(true);
        });
    }

}
