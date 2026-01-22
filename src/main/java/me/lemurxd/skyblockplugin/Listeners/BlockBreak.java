package me.lemurxd.skyblockplugin.Listeners;

import me.lemurxd.skyblockplugin.Main;
import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import me.lemurxd.skyblockplugin.utils.SafeGive;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public class BlockBreak implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;

        Location blockLoc = e.getBlock().getLocation();

        if (StoneGenerator.isStoneGenerator(blockLoc)) {
            if (e.getPlayer().isSneaking()) {
                Optional<Island> island = BentoBox.getInstance().getIslandsManager().getIslandAt(blockLoc);
                StoneGenerator.remove(island, blockLoc);

                e.setDropItems(false);
                SafeGive.giv(StoneGenerator.getItemStack(), e.getPlayer());
                e.getPlayer().sendMessage("§aZdemontowałeś stoniarkę.");
            } else {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cMusisz kucać, aby podnieść stoniarkę!");
                return;
            }
        }

        Location underLoc = blockLoc.clone().subtract(0, 1, 0);

        if (StoneGenerator.isStoneGenerator(underLoc)) {
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                blockLoc.getBlock().setType(Material.STONE);
            }, 1L);
        }
    }

}
