package me.lemurxd.skyblockplugin.listeners;

import me.pikamug.quests.Quests;
import me.pikamug.quests.module.BukkitCustomObjective;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class DeliverItemToNPCObjective extends BukkitCustomObjective implements Listener {

    public DeliverItemToNPCObjective() {
        setName("DeliverItemToNPC");
        setAuthor("lemurxd_");

        setItem("EMERALD", (short) 0);
        setShowCount(true);

        addStringPrompt("NPC IDs", "Wpisz ID NPC z Citizens (oddzielone przecinkiem, np. 1,4,12)", null);
        addStringPrompt("Item Materials", "Wpisz nazwy materiałów do oddania (oddzielone przecinkiem, np. DIAMOND,GOLD_INGOT)", null);
        addStringPrompt("Item Name", "Nazwa przedmiotu wyświetlana w celu (np. Diamenty)", null);

        setDisplay("Dostarcz %Item Name% do NPC: %count%");
    }

    @EventHandler
    public void onNPCClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        int clickedNpcId = event.getNPC().getId();

        Quests quests = (Quests) Bukkit.getPluginManager().getPlugin("Quests");
        if (quests == null) return;

        Quester quester = quests.getQuester(player.getUniqueId());
        if (quester == null) return;

        for (Quest quest : quester.getCurrentQuests().keySet()) {

            Map<String, Object> dataMap = getDataForPlayer(player.getUniqueId(), this, quest);
            if (dataMap == null) continue;

            String npcIdsStr = (String) dataMap.get("NPC IDs");
            if (npcIdsStr == null || !isNpcIdValid(clickedNpcId, npcIdsStr)) {
                continue;
            }

            String materialNames = (String) dataMap.get("Item Materials");
            if (materialNames == null) continue;

            ItemStack handItem = player.getInventory().getItemInMainHand();

            if (isMaterialValid(handItem.getType(), materialNames)) {

                int amount = handItem.getAmount();
                if (amount > 1) {
                    handItem.setAmount(amount - 1);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }

                incrementObjective(player.getUniqueId(), this, quest, 1);

                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

                return;
            }
        }
    }

    private boolean isNpcIdValid(int clickedId, String configIds) {
        String[] split = configIds.split(",");
        for (String s : split) {
            try {
                if (Integer.parseInt(s.trim()) == clickedId) {
                    return true;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    private boolean isMaterialValid(Material handMaterial, String configMaterials) {
        if (handMaterial == Material.AIR) return false;

        String[] split = configMaterials.split(",");
        for (String s : split) {
            if (handMaterial.name().equalsIgnoreCase(s.trim())) {
                return true;
            }
        }
        return false;
    }
}
