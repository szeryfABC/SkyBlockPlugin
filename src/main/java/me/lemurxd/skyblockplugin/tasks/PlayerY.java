package me.lemurxd.skyblockplugin.tasks;

import me.lemurxd.skyblockplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerY {

    public void runPlayerYRespawnTimer() {
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                ///p.teleport();
            }
        }, 0L, 100L);
    }


}
