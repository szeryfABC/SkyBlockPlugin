package me.lemurxd.skyblockplugin.tasks;

import me.lemurxd.skyblockplugin.SkyBlockPlugin;
import me.lemurxd.skyblockplugin.constructors.SkyBlockUser;
import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import me.lemurxd.skyblockplugin.utils.DropSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DataBaseTask extends BukkitRunnable {

    @Override
    public void run() {
        List<UserSnapshot> usersToSave = new ArrayList<>();
        Iterator<SkyBlockUser> iterator = SkyBlockUser.getSkyBlockUsers().iterator();

        while (iterator.hasNext()) {
            SkyBlockUser user = iterator.next();
            Player player = Bukkit.getPlayer(user.getPlayerUniqueId());

            String serializedDrops = DropSerializer.serialize(user.getDrops());

            usersToSave.add(new UserSnapshot(
                    user.getPlayerUniqueId().toString(),
                    user.getDropLevel(),
                    serializedDrops,
                    user.getLastOrbUsage(),
                    user.isMagnetEnabled()
            ));

            if (player == null) {
                iterator.remove();
            }
        }

        Map<String, List<StoneGenerator>> generatorsSnapshot = new HashMap<>(StoneGenerator.getMap());

        SkyBlockPlugin.getInstance().getLogger().info("[AutoSave] Przygotowano do zapisu: " + usersToSave.size() + " graczy.");

        new BukkitRunnable() {
            @Override
            public void run() {
                saveUsersAsync(usersToSave);
                saveGeneratorsAsync(generatorsSnapshot);
            }
        }.runTaskAsynchronously(SkyBlockPlugin.getInstance());
    }

    private void saveUsersAsync(List<UserSnapshot> users) {
        try {
            SkyBlockPlugin.getUserDatabase().saveUsersBatch(users);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveGeneratorsAsync(Map<String, List<StoneGenerator>> generators) {
        SkyBlockPlugin.getDatabase().saveGenerators(generators);
    }

    public record UserSnapshot(String uuid, int level, String data, long lastOrbUsage, boolean magnetEnabled) {}
}