package me.lemurxd.skyblockplugin.Listeners;

import me.lemurxd.skyblockplugin.Main;
import me.lemurxd.skyblockplugin.constructors.DropEntry;
import me.lemurxd.skyblockplugin.constructors.SkyBlockUser;
import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PlayerJoin implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        boolean existsInMemory = SkyBlockUser.getSkyBlockUsers().stream()
                .anyMatch(user -> user.getPlayerUniqueId().equals(e.getPlayer().getUniqueId()));

        if (!existsInMemory) {
            SkyBlockUser loadedUser = Main.getUserDatabase().loadUser(e.getPlayer().getUniqueId());

            if (loadedUser != null) {
                SkyBlockUser.getSkyBlockUsers().add(loadedUser);
            } else {
                SkyBlockUser.createSkyBlockUser(e.getPlayer().getUniqueId(), SkyBlockUser.getDropsForLevel(1), 1);
            }
        }
        if (BentoBox.getInstance().getIslandsManager().hasIsland(Bukkit.getWorld("bskyblock_world"), User.getInstance(e.getPlayer().getUniqueId()))) {
            if (!StoneGenerator.getMap().containsKey(BentoBox.getInstance().getIslandsManager().getIsland(Bukkit.getWorld("bskyblock_world"), User.getInstance(e.getPlayer().getUniqueId())))) {

                Island island = BentoBox.getInstance().getIslandsManager().getIsland(Bukkit.getWorld("bskyblock_world"), User.getInstance(e.getPlayer().getUniqueId()));
                Optional<Island> islandOpt = Optional.of(island);

                Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {

                    List<StoneGenerator> generators = Main.getDatabase().loadGeneratorsForIsland(island);

                    if (generators == null) generators = new ArrayList<>();

                    final List<StoneGenerator> finalGenerators = generators;

                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {

                        if (!StoneGenerator.getMap().containsKey(islandOpt)) {
                            StoneGenerator.getMap().put(islandOpt, finalGenerators);
                        }

                    });

                });

            }
        }
    }

}
