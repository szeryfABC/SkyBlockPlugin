package me.lemurxd.skyblockplugin.utils;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;

import java.util.Optional;

public class MythicUtils {

    public static boolean isMob(Entity entity, String mobId) {
        if (entity == null || mobId == null) {
            return false;
        }

        Optional<ActiveMob> activeMobOpt = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId());

        if (activeMobOpt.isPresent()) {
            ActiveMob activeMob = activeMobOpt.get();
            return activeMob.getType().getInternalName().equalsIgnoreCase(mobId);
        }

        return false;
    }

}
