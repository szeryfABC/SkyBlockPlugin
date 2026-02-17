package me.lemurxd.skyblockplugin.objectives;

import me.pikamug.quests.Quests;
import me.pikamug.quests.module.BukkitCustomObjective;
import me.pikamug.quests.player.Quester;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EatSomethingObjective extends BukkitCustomObjective implements Listener {

    public EatSomethingObjective() {
        setName("EatSomething");
        setAuthor("lemurxd_");

        setItem("COOKED_BEEF", (short) 1);
        setShowCount(true);

        addStringPrompt("Item Material", "Wpisz nazwy materiałów (np. APPLE,COOKED_BEEF) oddzielone przecinkami", null);
        addStringPrompt("Item Display Name", "Wpisz nazwę wyświetlaną (np. 'Jedzenie' lub 'Jabłka')", null);

        setDisplay("Zjedz: %Item Display Name%: %count%");
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack consumedItem = event.getItem();

        Quests quests = (Quests) Bukkit.getPluginManager().getPlugin("Quests");
        if (quests == null) return;

        Quester quester = quests.getQuester(player.getUniqueId());
        if (quester == null) return;

        String consumedMaterialName = consumedItem.getType().name();

        for (me.pikamug.quests.quests.Quest quest : quester.getCurrentQuests().keySet()) {

            Map<String, Object> dataMap = getDataForPlayer(player.getUniqueId(), this, quest);

            if (dataMap != null) {
                String configuredMaterials = (String) dataMap.get("Item Material");

                if (configuredMaterials != null) {
                    String[] acceptedItems = configuredMaterials.split(",");

                    boolean matchFound = false;

                    for (String materialName : acceptedItems) {
                        if (materialName.trim().equalsIgnoreCase(consumedMaterialName)) {
                            matchFound = true;
                            break;
                        }
                    }

                    if (matchFound) {
                        incrementObjective(player.getUniqueId(), this, quest, 1);
                    }
                }
            }
        }
    }
}