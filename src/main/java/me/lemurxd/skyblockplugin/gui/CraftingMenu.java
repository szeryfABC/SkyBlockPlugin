package me.lemurxd.skyblockplugin.gui;

import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.api.items.ItemManager;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import me.lemurxd.skyblockplugin.SkyBlockPlugin;
import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.lore.CustomLore;
import me.lemurxd.skyblockplugin.lore.Rarity;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.*;

public class CraftingMenu implements Listener {

    private static final Map<Integer, CraftingRecipe> slotRecipes = new HashMap<>();

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, color("&8&lSystem Tworzenia"));
        slotRecipes.clear();

        List<String> list = Config.CRAFTING_LIST.getStringList();
        int slot = 10;

        for (String line : list) {
            if (slot > 43) break;

            CraftingRecipe recipe = parseRecipe(line);
            if (recipe != null) {
                ItemStack icon = generateDisplayItem(recipe);
                inv.setItem(slot, icon);
                slotRecipes.put(slot, recipe);

                slot++;
                if ((slot + 1) % 9 == 0) slot += 2;
            }
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(color("&8&lSystem Tworzenia"))) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        Player p = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        if (slotRecipes.containsKey(slot)) {
            CraftingRecipe recipe = slotRecipes.get(slot);
            handleCrafting(p, recipe);
        }
    }

    private void handleCrafting(Player p, CraftingRecipe recipe) {
        if (recipe.moneyCost > 0) {
            if (SkyBlockPlugin.getEconomy().getBalance(p) < recipe.moneyCost) {
                p.sendMessage(color("&cNie masz wystarczająco pieniędzy! Potrzebujesz: &e$" + recipe.moneyCost));
                return;
            }
        }

        for (Map.Entry<String, Integer> req : recipe.itemCosts.entrySet()) {
            if (!hasMythicItem(p, req.getKey(), req.getValue())) {
                String mythicName = getMythicDisplayName(req.getKey());
                p.sendMessage(color("&cBrakuje Ci przedmiotu: &7" + mythicName + " &cx" + req.getValue()));
                return;
            }
        }

        if (recipe.moneyCost > 0) {
            SkyBlockPlugin.getEconomy().withdrawPlayer(p, recipe.moneyCost);
        }
        for (Map.Entry<String, Integer> req : recipe.itemCosts.entrySet()) {
            removeMythicItem(p, req.getKey(), req.getValue());
        }

        Optional<MythicItem> opt = MythicBukkit.inst().getItemManager().getItem(recipe.resultId);
        if (opt.isPresent()) {
            ItemStack reward = BukkitAdapter.adapt(opt.get().generateItemStack(recipe.resultAmount));

            HashMap<Integer, ItemStack> leftOvers = p.getInventory().addItem(reward);
            if (!leftOvers.isEmpty()) {
                p.getWorld().dropItemNaturally(p.getLocation(), reward);
                p.sendMessage(color("&eTwój ekwipunek był pełny! Przedmiot upadł na ziemię."));
            }
            p.sendMessage(color("&aPomyślnie wytworzono przedmiot!"));
        }
    }


    private static ItemStack generateDisplayItem(CraftingRecipe recipe) {
        Optional<MythicItem> mythicItemOpt = MythicBukkit.inst().getItemManager().getItem(recipe.resultId);
        if (!mythicItemOpt.isPresent()) return new ItemStack(Material.STONE);

        MythicItem mythicItem = mythicItemOpt.get();
        AbstractItemStack ais = mythicItem.generateItemStack(recipe.resultAmount);
        ItemStack baseStack = BukkitAdapter.adapt(ais);
        ItemMeta meta = baseStack.getItemMeta();

        boolean hasSkyBlockTag = false;
        if (mythicItem.getItemNBT() != null) {
            hasSkyBlockTag = mythicItem.getItemNBT().containsKey("SkyBlockPlugin")
                    && String.valueOf(mythicItem.getItemNBT().get("SkyBlockPlugin").getValue()).equalsIgnoreCase("yes");
        }

        List<String> lore = new ArrayList<>();

        if (hasSkyBlockTag) {
            lore = applyDevStatsLogic(mythicItem, baseStack);
        } else {
            if (meta.hasLore()) lore.addAll(meta.getLore());
        }

        lore.add("");
        lore.add(color("&8&m------------------------------"));
        lore.add(color("&c&lWymagane do stworzenia:"));

        if (recipe.moneyCost > 0) {
            lore.add(color(" &8» &aPieniądze: &2$" + recipe.moneyCost));
        }

        for (Map.Entry<String, Integer> req : recipe.itemCosts.entrySet()) {
            String displayName = getMythicDisplayName(req.getKey());
            lore.add(color(" &8» &7" + displayName + "&f x" + req.getValue()));
        }
        lore.add(color("&8&m------------------------------"));
        lore.add("");
        lore.add(color("&eKliknij, aby wytworzyć!"));

        meta.setLore(lore);
        meta.setAttributeModifiers(null);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        baseStack.setItemMeta(meta);

        if (hasSkyBlockTag) {
            baseStack.getEnchantments().keySet().forEach(baseStack::removeEnchantment);
        }

        return baseStack;
    }

    private static List<String> applyDevStatsLogic(MythicItem mythicItem, ItemStack baseStack) {
        ItemMeta baseMeta = baseStack.getItemMeta();
        double minModPercent = 0.0, maxModPercent = 0.0;
        List<String> modifiers = Config.RARITY_MODIFIER.getStringList();
        boolean first = true;

        for (String line : modifiers) {
            try {
                double val = Double.parseDouble(line.split(":")[1].replace("%", "").replace("+", "").trim());
                if (first) { minModPercent = maxModPercent = val; first = false; }
                else {
                    if (val < minModPercent) minModPercent = val;
                    if (val > maxModPercent) maxModPercent = val;
                }
            } catch (Exception ignored) {}
        }

        String colorFormat = "f:7";
        if (mythicItem.getItemNBT() != null && mythicItem.getItemNBT().containsKey("ColorFormat")) {
            colorFormat = String.valueOf(mythicItem.getItemNBT().get("ColorFormat").getValue());
        }

        String uniqueText = null;
        if (mythicItem.getItemNBT() != null && mythicItem.getItemNBT().containsKey("UniqueText")) {
            uniqueText = String.valueOf(mythicItem.getItemNBT().get("UniqueText").getValue());
        }

        String abilityTag = null;
        if (mythicItem.getItemNBT() != null && mythicItem.getItemNBT().containsKey("ABILITY")) {
            abilityTag = String.valueOf(mythicItem.getItemNBT().get("ABILITY").getValue());
        }
        boolean hasAbility = abilityTag != null && !abilityTag.isEmpty();

        String[] colors = colorFormat.split(":");
        String c1 = "&" + (colors.length > 0 ? colors[0] : "f");
        String c2 = "&" + (colors.length > 1 ? colors[1] : "7");

        List<String> newLore = new ArrayList<>();
        List<String> baseLore = baseMeta.hasLore() ? baseMeta.getLore() : new ArrayList<>();

        newLore.add(color(c1 + "&m------------------------------"));
        List<String> storyLore = new ArrayList<>();
        List<String> abilityLore = new ArrayList<>();
        boolean abilitySectionFound = false;

        for (String line : baseLore) {
            if (hasAbility && (line.contains("ZDOLNOŚĆ SPECJALNA") || line.contains("ABILITY"))) {
                abilitySectionFound = true;
            }
            if (abilitySectionFound) abilityLore.add(line);
            else storyLore.add(line);
        }

        for (String s : storyLore) {
            if (!s.trim().isEmpty()) newLore.add(color(s));
        }

        newLore.add(color(c1 + "&lSTATYSTYKI (ZAKRES):"));
        DecimalFormat df = new DecimalFormat("#.##");

        if (baseMeta.hasAttributeModifiers()) {
            List<Map.Entry<Attribute, AttributeModifier>> sortedEntries = new ArrayList<>(baseMeta.getAttributeModifiers().entries());
            sortedEntries.sort(Comparator.comparing(e -> e.getKey().getKey().getKey()));

            for (Map.Entry<Attribute, AttributeModifier> entry : sortedEntries) {
                double baseVal = entry.getValue().getAmount();
                double minVal = baseVal * (1.0 + (minModPercent / 100.0));
                double maxVal = baseVal * (1.0 + (maxModPercent / 100.0));
                if (minVal > maxVal) { double temp = minVal; minVal = maxVal; maxVal = temp; }

                if (baseVal != 0) {
                    String rangeStr = df.format(minVal) + " - " + df.format(maxVal);

                    String displayName = CustomLore.getPolishAttributeName(entry.getKey());

                    newLore.add(color(" " + c2 + "» &7" + displayName + ": " + c2 + rangeStr.replace(",", ".")));
                }
            }
        }

        for (Map.Entry<Enchantment, Integer> entry : baseStack.getEnchantments().entrySet()) {
            int baseLvl = entry.getValue();
            int minLvl = Math.max(1, (int) (baseLvl * (1.0 + (minModPercent / 100.0))));
            int maxLvl = Math.max(1, (int) (baseLvl * (1.0 + (maxModPercent / 100.0))));

            String rangeStr = minLvl == maxLvl ? String.valueOf(minLvl) : minLvl + " - " + maxLvl;


            String displayEnchant = CustomLore.getPolishEnchantName(entry.getKey());

            newLore.add(color(" " + c2 + "» &7" + displayEnchant + ": " + c2 + rangeStr));
        }

        if (hasAbility && !abilityLore.isEmpty()) {
            newLore.add("");
            int baseCooldown = 30;
            if (mythicItem.getItemNBT() != null && mythicItem.getItemNBT().containsKey("Cooldown")) {
                try { baseCooldown = Integer.parseInt(String.valueOf(mythicItem.getItemNBT().get("Cooldown").getValue())); } catch (NumberFormatException ignored) {}
            }

            double bestCooldown = baseCooldown / Math.max(0.1, (1.0 + (maxModPercent / 100.0)));
            double worstCooldown = baseCooldown / Math.max(0.1, (1.0 + (minModPercent / 100.0)));
            String rangeStr = String.format(java.util.Locale.US, "%.1f", bestCooldown) + "s - " + String.format(java.util.Locale.US, "%.1f", worstCooldown);

            for (String line : abilityLore) {
                newLore.add(color(line.replace("<cooldown>", rangeStr + "&7")));
            }
        }

        if (uniqueText != null && !uniqueText.isEmpty()) {
            newLore.add("");
            newLore.add(color(c1 + "\"" + uniqueText + "\""));
        }
        newLore.add(color(c1 + "&m------------------------------"));

        return newLore;
    }


    private static CraftingRecipe parseRecipe(String line) {
        try {
            String[] parts = line.split("\\|");
            String[] resultData = parts[0].trim().split(":");
            String[] reqData = parts[1].trim().split(",");

            CraftingRecipe recipe = new CraftingRecipe();
            recipe.resultId = resultData[0];
            recipe.resultAmount = Integer.parseInt(resultData[1]);

            for (String req : reqData) {
                String[] reqSplit = req.split(":");
                String id = reqSplit[0];
                int amount = Integer.parseInt(reqSplit[1]);

                if (id.equalsIgnoreCase("money")) {
                    recipe.moneyCost = amount;
                } else {
                    recipe.itemCosts.put(id, amount);
                }
            }
            return recipe;
        } catch (Exception e) {
            System.out.println("Blad parsowania craftingu: " + line);
            return null;
        }
    }

    private boolean hasMythicItem(Player p, String mythicId, int amountNeeded) {
        int found = 0;
        for (ItemStack is : p.getInventory().getContents()) {
            if (is == null || is.getType() == Material.AIR) continue;
            String type = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(is);
            if (type != null && type.equalsIgnoreCase(mythicId)) {
                found += is.getAmount();
            }
        }
        return found >= amountNeeded;
    }

    private void removeMythicItem(Player p, String mythicId, int amountToRemove) {
        for (ItemStack is : p.getInventory().getContents()) {
            if (is == null || is.getType() == Material.AIR) continue;
            String type = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(is);

            if (type != null && type.equalsIgnoreCase(mythicId)) {
                int amount = is.getAmount();
                if (amount <= amountToRemove) {
                    amountToRemove -= amount;
                    p.getInventory().remove(is);
                } else {
                    is.setAmount(amount - amountToRemove);
                    amountToRemove = 0;
                }
                if (amountToRemove <= 0) break;
            }
        }
    }

    private static String getMythicDisplayName(String internalName) {
        Optional<MythicItem> item = MythicBukkit.inst().getItemManager().getItem(internalName);
        if (item.isPresent()) {
            return color(item.get().getDisplayName());
        }
        return internalName;
    }

    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private static class CraftingRecipe {
        String resultId;
        int resultAmount;
        double moneyCost = 0;
        Map<String, Integer> itemCosts = new HashMap<>();
    }
}