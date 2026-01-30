package me.lemurxd.skyblockplugin.listeners;

import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

public class StoneGeneratorProtection implements Listener {

    @EventHandler
    public void onPistonPush(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (StoneGenerator.isStoneGenerator(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonPull(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (StoneGenerator.isStoneGenerator(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        Iterator<Block> iterator = e.blockList().iterator();

        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (StoneGenerator.isStoneGenerator(block.getLocation())) {
                iterator.remove();
            }
        }
    }
}
