package me.lemurxd.skyblockplugin.listeners;

import me.lemurxd.skyblockplugin.Main;
import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public class IslandDelete  implements Listener{

    @EventHandler
    public void onIslandDeletion(IslandDeleteEvent e) {
        var island = e.getIsland();
        if (island == null) return;

        String islandId = island.getUniqueId().toString();

        StoneGenerator.getMap().keySet().removeIf(optIsland -> optIsland.equals(islandId));

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            Main.getDatabase().deleteGeneratorsForIsland(islandId);
        });
    }
}
