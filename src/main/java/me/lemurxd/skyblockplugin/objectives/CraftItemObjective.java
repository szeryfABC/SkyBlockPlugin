package me.lemurxd.skyblockplugin.objectives;

import me.pikamug.quests.Quests;
import me.pikamug.quests.module.BukkitCustomObjective;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CraftItemObjective extends BukkitCustomObjective implements Listener {

    public CraftItemObjective() {
        setName("CraftItem");
        setAuthor("lemurxd_");
        setItem("WORKBENCH", (short) 0);
        setShowCount(true);

        addStringPrompt("Item Materials", "Wpisz nazwy materiałów do scraftowania (oddzielone przecinkiem, np. TORCH,STICK)", null);
        addStringPrompt("Item Name", "Nazwa przedmiotu wyświetlana w celu", null);

        setDisplay("Wytwórz %Item Name%: %count%");
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack resultItem = event.getCurrentItem();
        if (resultItem == null || resultItem.getType() == Material.AIR) return;

        int amountCrafted = calculateCraftedAmount(event);
        if (amountCrafted <= 0) return;

        Quests quests = (Quests) Bukkit.getPluginManager().getPlugin("Quests");
        if (quests == null) return;

        Quester quester = quests.getQuester(player.getUniqueId());
        if (quester == null) return;

        for (Quest quest : quester.getCurrentQuests().keySet()) {
            Map<String, Object> dataMap = getDataForPlayer(player.getUniqueId(), this, quest);
            if (dataMap == null) continue;

            String materialsStr = (String) dataMap.get("Item Materials");
            if (materialsStr == null) continue;

            if (isMaterialValid(resultItem.getType(), materialsStr)) {
                incrementObjective(player.getUniqueId(), this, quest, amountCrafted);
            }
        }
    }

    private boolean isMaterialValid(Material mat, String configString) {
        String[] split = configString.split(",");
        for (String s : split) {
            if (mat.name().equalsIgnoreCase(s.trim())) {
                return true;
            }
        }
        return false;
    }

    private int calculateCraftedAmount(CraftItemEvent event) {
        ItemStack result = event.getCurrentItem();
        if (result == null) return 0;

        int resultAmount = result.getAmount();

        if (event.isShiftClick()) {
            CraftingInventory inv = event.getInventory();
            int maxCrafts = Integer.MAX_VALUE;

            for (ItemStack item : inv.getMatrix()) {
                if (item != null && item.getType() != Material.AIR) {
                    if (item.getAmount() < maxCrafts) {
                        maxCrafts = item.getAmount();
                    }
                }
            }

            if (maxCrafts == Integer.MAX_VALUE) maxCrafts = 0;

            int totalToCreate = maxCrafts * resultAmount;

            int spaceInInventory = getFreeSpace(event.getWhoClicked().getInventory(), result.getType());

            return Math.min(totalToCreate, spaceInInventory);
        } else {
            return resultAmount;
        }
    }

    private int getFreeSpace(org.bukkit.inventory.Inventory inv, Material type) {
        int freeSpace = 0;
        int maxStack = type.getMaxStackSize();

        for (ItemStack i : inv.getStorageContents()) {
            if (i == null || i.getType() == Material.AIR) {
                freeSpace += maxStack;
            } else if (i.getType() == type) {
                freeSpace += (maxStack - i.getAmount());
            }
        }
        return freeSpace;
    }
}
