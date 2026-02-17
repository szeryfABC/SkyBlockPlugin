package me.lemurxd.skyblockplugin.lore;

import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import io.lumine.mythic.core.utils.jnbt.Tag;
import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.utils.NBTUtil;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.gmail.nossr50.mcmmo.acf.ACFBukkitUtil.color;
import static me.lemurxd.skyblockplugin.lore.CustomLore.*;

public class Rarity {

    public static String getRandom() {
        List<String> rarityList = Config.RARITY_LIST.getStringList();

        if (rarityList == null || rarityList.isEmpty()) {
            return "ZWYKŁY";
        }

        double totalWeight = 0.0;
        for (String entry : rarityList) {
            try {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    double chance = Double.parseDouble(parts[1].replace("%", "").trim());
                    if (chance > 0) {
                        totalWeight += chance;
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("[Błąd] Niepoprawny format procentu w linii: " + entry);
            }
        }

        double randomValue = ThreadLocalRandom.current().nextDouble() * totalWeight;

        double currentWeight = 0.0;
        for (String entry : rarityList) {
            try {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    String name = parts[0];
                    double chance = Double.parseDouble(parts[1].replace("%", "").trim());

                    if (chance > 0) {
                        currentWeight += chance;
                        if (currentWeight >= randomValue) {
                            return name;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return rarityList.get(0).split(":")[0];
    }

    public static void buildDevItem(ItemStack itemToBuild, Player player, int slot) {
        if (itemToBuild == null || itemToBuild.getType().equals(Material.AIR)) return;

        String mythicType = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(itemToBuild);
        if (mythicType == null) return;

        Optional<MythicItem> mythicItemOpt = MythicBukkit.inst().getItemManager().getItem(mythicType);
        if (!mythicItemOpt.isPresent()) return;

        MythicItem mythicItem = mythicItemOpt.get();

        double minModPercent = 0.0;
        double maxModPercent = 0.0;

        List<String> modifiers = Config.RARITY_MODIFIER.getStringList();
        boolean first = true;

        for (String line : modifiers) {
            try {
                String valStr = line.split(":")[1].replace("%", "").replace("+", "").trim();
                double val = Double.parseDouble(valStr);

                if (first) {
                    minModPercent = val;
                    maxModPercent = val;
                    first = false;
                } else {
                    if (val < minModPercent) minModPercent = val;
                    if (val > maxModPercent) maxModPercent = val;
                }
            } catch (Exception ignored) {}
        }

        AbstractItemStack ais = mythicItem.generateItemStack(1);
        ItemStack baseStack = BukkitAdapter.adapt(ais);
        ItemMeta baseMeta = baseStack.getItemMeta();

        String colorFormat = "f:7";
        Map<String, Tag> nbtMap = mythicItem.getItemNBT();
        if (nbtMap != null && nbtMap.containsKey("ColorFormat")) {
            colorFormat = String.valueOf(nbtMap.get("ColorFormat").getValue());
        }

        String uniqueText = null;
        if (nbtMap != null && nbtMap.containsKey("UniqueText")) {
            uniqueText = String.valueOf(nbtMap.get("UniqueText").getValue());
        }

        String abilityTag = null;
        if (nbtMap != null && nbtMap.containsKey("ABILITY")) {
            abilityTag = String.valueOf(nbtMap.get("ABILITY").getValue());
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

        if (baseLore != null) {
            for (String line : baseLore) {
                if (hasAbility && (line.contains("ZDOLNOŚĆ SPECJALNA") || line.contains("ABILITY"))) {
                    abilitySectionFound = true;
                }
                if (abilitySectionFound) abilityLore.add(line);
                else storyLore.add(line);
            }
        }

        for (String s : storyLore) {
            if (!s.trim().isEmpty()) newLore.add(color(s));
        }

        newLore.add(color(c1 + "&lSTATYSTYKI (ZAKRES):"));

        DecimalFormat df = new DecimalFormat("#.##");

        if (baseMeta.hasAttributeModifiers()) {
            List<Map.Entry<Attribute, AttributeModifier>> sortedEntries = new ArrayList<>(baseMeta.getAttributeModifiers().entries());
            sortedEntries.sort(Comparator.comparing(e -> e.getKey().getKey().toString()));

            for (Map.Entry<Attribute, AttributeModifier> entry : sortedEntries) {
                Attribute attr = entry.getKey();
                double baseVal = entry.getValue().getAmount();

                double minVal = applyModifierSigned(baseVal, minModPercent);
                double maxVal = applyModifierSigned(baseVal, maxModPercent);

                if (minVal > maxVal) { double temp = minVal; minVal = maxVal; maxVal = temp; }

                if (baseVal != 0) {
                    String polskaNazwa = getPolishAttributeName(attr);
                    String rangeStr = df.format(minVal) + " - " + df.format(maxVal);
                    newLore.add(color(" " + c2 + "» &7" + polskaNazwa + ": " + c2 + rangeStr.replace(",", ".")));
                }
            }
        }

        for (Map.Entry<Enchantment, Integer> entry : baseStack.getEnchantments().entrySet()) {
            int baseLvl = entry.getValue();

            int minLvl = (int) (baseLvl * (1.0 + (minModPercent / 100.0)));
            if (minLvl < 1) minLvl = 1;

            int maxLvl = (int) (baseLvl * (1.0 + (maxModPercent / 100.0)));
            if (maxLvl < 1) maxLvl = 1;

            String rangeStr = ""+minLvl;
            if (minLvl != maxLvl) {
                rangeStr += " - " + maxLvl;
            }

            newLore.add(color(" " + c2 + "» &7" + getPolishEnchantName(entry.getKey()) + ": " + c2 + rangeStr));
        }

        if (hasAbility && !abilityLore.isEmpty()) {
            newLore.add("");

            int baseCooldown = 30;
            if (nbtMap != null && nbtMap.containsKey("Cooldown")) {
                try {
                    baseCooldown = Integer.parseInt(String.valueOf(nbtMap.get("Cooldown").getValue()));
                } catch (NumberFormatException ignored) {}
            }

            double bestCooldown = baseCooldown / Math.max(0.1, (1.0 + (maxModPercent / 100.0)));
            double worstCooldown = baseCooldown / Math.max(0.1, (1.0 + (minModPercent / 100.0)));

            String bestStr = String.format(java.util.Locale.US, "%.1f", bestCooldown);
            String worstStr = String.format(java.util.Locale.US, "%.1f", worstCooldown);

            String rangeStr = bestStr + "s - " + worstStr;

            for (String line : abilityLore) {
                newLore.add(color(line.replace("<cooldown>", rangeStr + "&7")));
            }
        }

        if (uniqueText != null && !uniqueText.isEmpty()) {
            newLore.add("");
            newLore.add(color(c1 + "\"" + uniqueText + "\""));
        }
        newLore.add(color(c1 + "&m------------------------------"));

        ItemStack finalItem = baseStack.clone();
        ItemMeta finalMeta = finalItem.getItemMeta();

        finalMeta.setLore(newLore);

        finalMeta.setAttributeModifiers(null);
        finalMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        finalItem.setItemMeta(finalMeta);

        finalItem.getEnchantments().keySet().forEach(finalItem::removeEnchantment);

        player.getInventory().setItem(slot, finalItem);
    }

    private static double applyModifierSigned(double base, double percent) {
        if (base >= 0) {
            return base * (1.0 + percent / 100.0);
        } else {
            return base * (1.0 - percent / 100.0);
        }
    }


}
