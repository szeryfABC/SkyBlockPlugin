package me.lemurxd.skyblockplugin.listeners;

import me.lemurxd.skyblockplugin.Main;
import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import world.bentobox.bentobox.api.events.island.IslandEnterEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StoneGeneratorLoad implements Listener {


    private final Set<String> loadingQueue = ConcurrentHashMap.newKeySet();


    @EventHandler
    public void onIslandLoad(IslandEnterEvent e) {

        Island island = e.getIsland();
        User user = User.getInstance(e.getPlayerUUID());

        if (loadingQueue.contains(island.getUniqueId())) return;

        if (island.isAllowed(user, Flags.BREAK_BLOCKS)) {
            if (!StoneGenerator.getMap().containsKey(island.getUniqueId())) {

                loadingQueue.add(island.getUniqueId());

                Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {

                    List<StoneGenerator> generators = null;
                    try {
                        generators = Main.getDatabase().loadGeneratorsForIsland(island);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    if (generators == null) generators = new ArrayList<>();

                    final List<StoneGenerator> finalGenerators = generators;

                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        try {
                            if (!StoneGenerator.getMap().containsKey(island.getUniqueId())) {
                                StoneGenerator.getMap().put(island.getUniqueId(), finalGenerators);
                            }
                        } finally {
                            loadingQueue.remove(island.getUniqueId());
                        }
                    });
                });
            }
        }
    }
}
