package me.lemurxd.skyblockplugin.tasks;

import me.lemurxd.skyblockplugin.Main;
import me.lemurxd.skyblockplugin.enums.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerY {

    public static void runPlayerYRespawnTimer() {
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            if (!Config.SAFE_SPAWN_ENABLED.getBoolean()) return;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().equals(Config.SAFE_SPAWN_WORLD.getString())) {
                    p.teleport(new Location(Bukkit.getWorld(Config.SAFE_SPAWN_WORLD.getString()), Config.SAFE_SPAWN_X.getInt(), Config.SAFE_SPAWN_Y.getInt(), Config.SAFE_SPAWN_Z.getInt(), Config.SAFE_SPAWN_FACING_X.getInt(), Config.SAFE_SPAWN_FACING_Y.getInt()));
                }
            }
        }, 0L, 100L);
    }


}
