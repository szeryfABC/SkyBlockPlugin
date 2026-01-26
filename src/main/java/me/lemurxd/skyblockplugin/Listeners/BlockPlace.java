package me.lemurxd.skyblockplugin.listeners;

import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public class BlockPlace implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;

        Optional<Island> island = BentoBox.getInstance().getIslandsManager().getIslandAt(e.getBlockPlaced().getLocation());

        if (StoneGenerator.isStoneGeneratorItem(e.getItemInHand())) {

            if (!BentoBox.getInstance().getIslandsManager().isIslandAt(e.getBlockPlaced().getLocation())) {
                e.setCancelled(true);
                return;
            }

            StoneGenerator.create(island, e.getBlockPlaced().getLocation());

            Location location = e.getBlockPlaced().getLocation().add(0, 1,0);
            StoneGenerator.create(island, e.getBlockPlaced().getLocation());

            if (e.getBlockPlaced().getLocation().getWorld().getBlockAt(location).getType().isAir()) {
                e.getBlockPlaced().getLocation().getWorld().getBlockAt(location).setBlockData(Material.STONE.createBlockData());
            }
        }

    }

}
