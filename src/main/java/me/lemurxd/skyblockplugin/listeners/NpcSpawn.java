package me.lemurxd.skyblockplugin.listeners;

import io.lumine.mythic.bukkit.MythicBukkit;
import me.lemurxd.skyblockplugin.SkyBlockPlugin;
import me.lemurxd.skyblockplugin.enums.Config;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import world.bentobox.bentobox.api.events.island.IslandEnterEvent;
import world.bentobox.bentobox.database.objects.Island;

public class NpcSpawn implements Listener {

    @EventHandler
    public void onIslandLoad(IslandEnterEvent e) {

        Island island= e.getIsland();

        if (e.getLocation().getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            if (SkyBlockPlugin.getData().isKowalUnlocked(island.getUniqueId()) && Config.NPCS_KOWAL_ENABLED.getBoolean() == true) {

                Location location = island.getCenter();
                location.add(Config.NPCS_KOWAL_CORDS_X.getInt(), Config.NPCS_KOWAL_CORDS_Y.getInt(), Config.NPCS_KOWAL_CORDS_Z.getInt());

                MythicBukkit.inst().getMobManager().spawnMob(Config.NPCS_KOWAL_MOBNAME.getString(), location);
            }

            if (SkyBlockPlugin.getData().isHordyUnlocked(island.getUniqueId()) && Config.NPCS_HORDY_ENABLED.getBoolean() == true) {

                Location location = island.getCenter();
                location.add(Config.NPCS_HORDY_CORDS_X.getInt(), Config.NPCS_HORDY_CORDS_Y.getInt(), Config.NPCS_HORDY_CORDS_Z.getInt());

                MythicBukkit.inst().getMobManager().spawnMob(Config.NPCS_HORDY_MOBNAME.getString(), location);
            }

        }
    }
}
