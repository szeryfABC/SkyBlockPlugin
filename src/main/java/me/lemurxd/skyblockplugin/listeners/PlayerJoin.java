package me.lemurxd.skyblockplugin.listeners;

import me.lemurxd.skyblockplugin.SkyBlockPlugin;
import me.lemurxd.skyblockplugin.constructors.SkyBlockUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        boolean existsInMemory = SkyBlockUser.getSkyBlockUsers().stream()
                .anyMatch(user -> user.getPlayerUniqueId().equals(e.getPlayer().getUniqueId()));

        if (!existsInMemory) {
            SkyBlockUser loadedUser = SkyBlockPlugin.getUserDatabase().loadUser(e.getPlayer().getUniqueId());

            if (loadedUser != null) {
                SkyBlockUser.getSkyBlockUsers().add(loadedUser);
            } else {
                SkyBlockUser.createSkyBlockUser(e.getPlayer().getUniqueId(), SkyBlockUser.getDropsForLevel(1), 1, 0, false);
            }
        }
    }

}
