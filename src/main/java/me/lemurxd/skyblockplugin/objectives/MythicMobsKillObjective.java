package me.lemurxd.skyblockplugin.objectives;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.pikamug.quests.Quests;
import me.pikamug.quests.module.BukkitCustomObjective;
import me.pikamug.quests.player.Quester;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Map;

public class MythicMobsKillObjective extends BukkitCustomObjective implements Listener {

    public MythicMobsKillObjective() {

        setName("KillMythicMob");
        setAuthor("lemurxd_");

        setItem("DIAMOND_SWORD", (short) 0);
        setShowCount(true);

        addStringPrompt("MythicMob ID", "Wpisz nazwy wewnętrzne mobów (ID) oddzielone przecinkami, np. Mob1,Mob2", null);
        addStringPrompt("MythicMob Name", "Wpisz nazwę wyświetlaną (możesz wpisać np. 'Zombie lub Szkielet')", null);

        setDisplay("Zabij: %MythicMob Name%: %count%");
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }
        Player killer = event.getEntity().getKiller();

        if (!Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            return;
        }

        ActiveMob activeMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(event.getEntity());
        if (activeMob == null) {
            return;
        }

        String mobInternalName = activeMob.getType().getInternalName();

        Quests quests = (Quests) Bukkit.getPluginManager().getPlugin("Quests");
        if (quests == null) return;

        Quester quester = quests.getQuester(killer.getUniqueId());
        if (quester == null) return;

        for (me.pikamug.quests.quests.Quest quest : quester.getCurrentQuests().keySet()) {

            Map<String, Object> dataMap = getDataForPlayer(killer.getUniqueId(), this, quest);

            if (dataMap != null) {
                String configuredMobIDs = (String) dataMap.get("MythicMob ID");

                if (configuredMobIDs != null) {
                    String[] acceptedMobs = configuredMobIDs.split(",");

                    boolean matchFound = false;

                    for (String id : acceptedMobs) {
                        if (id.trim().equalsIgnoreCase(mobInternalName)) {
                            matchFound = true;
                            break;
                        }
                    }

                    if (matchFound) {
                        incrementObjective(killer.getUniqueId(), this, quest, 1);
                    }
                }
            }
        }
    }
}
