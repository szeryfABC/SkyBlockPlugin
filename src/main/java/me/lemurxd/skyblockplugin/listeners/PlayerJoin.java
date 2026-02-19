package me.lemurxd.skyblockplugin.listeners;

import me.lemurxd.skyblockplugin.SkyBlockPlugin;
import me.lemurxd.skyblockplugin.constructors.SkyBlockUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        UUID uuid = e.getUniqueId();

        boolean existsInMemory = SkyBlockUser.getSkyBlockUsers().stream()
                .anyMatch(user -> user.getPlayerUniqueId().equals(uuid));

        if (!existsInMemory) {
            try {
                SkyBlockUser loadedUser = SkyBlockPlugin.getUserDatabase().loadUser(uuid);

                if (loadedUser != null) {
                    SkyBlockUser.getSkyBlockUsers().add(loadedUser);
                } else {
                    SkyBlockUser.createSkyBlockUser(uuid, SkyBlockUser.getDropsForLevel(1), 1, 0, false);
                }
            } catch (Exception ex) {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cBłąd bazy danych! Nie udało się załadować profilu.");
                ex.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        SkyBlockPlugin.getDbManager().loadPlayerInventory(player);

    }
}
