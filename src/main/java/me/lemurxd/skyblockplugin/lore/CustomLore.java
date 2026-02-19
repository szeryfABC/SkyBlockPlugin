package me.lemurxd.skyblockplugin.lore;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import io.lumine.mythic.core.utils.jnbt.Tag;
import me.lemurxd.skyblockplugin.SkyBlockPlugin;
import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.utils.NBTUtil;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.chat.TextComponent;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class CustomLore {

    public static final Set<UUID> developers = new HashSet<>();

    public static void build(ItemStack itemToBuild, Player player, int slot) {
        if (itemToBuild == null || itemToBuild.getType().equals(Material.AIR) || developers.contains(player.getUniqueId())) return;

        String mythicType = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(itemToBuild);
        if (mythicType == null) return;

        Optional<MythicItem> mythicItemOpt = MythicBukkit.inst().getItemManager().getItem(mythicType);
        if (!mythicItemOpt.isPresent()) return;

        MythicItem mythicItem = mythicItemOpt.get();

        Map<String, Tag> nbtMap = mythicItem.getItemNBT();
        boolean hasSkyBlockTag = false;

        if (nbtMap != null) {
            for (Map.Entry<String, Tag> entry : nbtMap.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("SkyBlockPlugin")) {
                    String value = String.valueOf(entry.getValue().getValue());
                    if (value.equalsIgnoreCase("yes")) {
                        hasSkyBlockTag = true;
                        break;
                    }
                }
            }
        }

        if (!hasSkyBlockTag) return;

        AbstractItemStack ais = mythicItem.generateItemStack(1);
        ItemStack baseStack = BukkitAdapter.adapt(ais);
        ItemMeta baseMeta = baseStack.getItemMeta();

        String currentRarityName = NBTUtil.getString(itemToBuild, "Rarity");
        boolean isNewDiscovery = false;
        if (currentRarityName == null) {
            String rawRarityLine = Rarity.getRandom();
            currentRarityName = ChatColor.stripColor(rawRarityLine).split(":")[0].trim();
            isNewDiscovery = true;
        }
        double rarityModifierPercent = getRarityModifier(currentRarityName);

        String colorFormat = NBTUtil.getString(itemToBuild, "ColorFormat");
        if (colorFormat == null && nbtMap != null && nbtMap.containsKey("ColorFormat")) {
            colorFormat = String.valueOf(nbtMap.get("ColorFormat").getValue());
        }
        if (colorFormat == null) colorFormat = "f:7";

        String uniqueText = NBTUtil.getString(itemToBuild, "UniqueText");
        if (uniqueText == null && nbtMap != null && nbtMap.containsKey("UniqueText")) {
            uniqueText = String.valueOf(nbtMap.get("UniqueText").getValue());
        }

        String abilityTag = NBTUtil.getString(itemToBuild, "ABILITY");
        if (abilityTag == null && nbtMap != null && nbtMap.containsKey("ABILITY")) {
            abilityTag = String.valueOf(nbtMap.get("ABILITY").getValue());
        }
        boolean hasAbility = abilityTag != null && !abilityTag.isEmpty();

        String deadMendingTag = NBTUtil.getString(itemToBuild, "deadMending");
        boolean isDeadMending = deadMendingTag != null && !deadMendingTag.isEmpty();

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

        newLore.add(color(c1 + "&lSTATYSTYKI:"));

        Multimap<Attribute, AttributeModifier> newAttributes = ArrayListMultimap.create();
        DecimalFormat df = new DecimalFormat("#.##");

        if (baseMeta.hasAttributeModifiers()) {
            List<Map.Entry<Attribute, AttributeModifier>> sortedEntries = new ArrayList<>(baseMeta.getAttributeModifiers().entries());

            sortedEntries.sort(Comparator.comparing(e -> e.getKey().getKey().toString()));

            for (Map.Entry<Attribute, AttributeModifier> entry : sortedEntries) {
                Attribute attr = entry.getKey();
                AttributeModifier mod = entry.getValue();

                double baseValue = mod.getAmount();
                double finalValue = applyModifier(baseValue, rarityModifierPercent);

                NamespacedKey key = new NamespacedKey(MythicBukkit.inst(), "attr_" + UUID.randomUUID().toString());
                EquipmentSlotGroup slotGroup = (mod.getSlot() != null) ? mod.getSlotGroup() : EquipmentSlotGroup.ANY;

                AttributeModifier newMod = new AttributeModifier(key, finalValue, mod.getOperation(), slotGroup);
                newAttributes.put(attr, newMod);

                if (finalValue != 0) {
                    String polskaNazwa = getPolishAttributeName(attr);
                    newLore.add(color(" " + c2 + "» &7" + polskaNazwa + ": " + c2 + df.format(finalValue).replace(",", ".")));
                }
            }
        }

        Map<Enchantment, Integer> newEnchants = new HashMap<>();

        Map<Enchantment, Integer> enchantsToProcess = new HashMap<>(baseStack.getEnchantments());

        if (itemToBuild.containsEnchantment(Enchantment.MENDING)) {
            enchantsToProcess.put(Enchantment.MENDING, 1);
        }

        for (Map.Entry<Enchantment, Integer> entry : enchantsToProcess.entrySet()) {
            Enchantment ench = entry.getKey();
            int baseLvl = entry.getValue();

            if (ench.equals(Enchantment.MENDING)) {
                if (isDeadMending) {
                    newLore.add(color(" " + c2 + "» &c&m" + getPolishEnchantName(ench)));
                } else {
                    newEnchants.put(ench, 1);
                    newLore.add(color(" " + c2 + "» &7" + getPolishEnchantName(ench) + ": " + c2 + "1"));
                }
                continue;
            }

            int finalLvl = (int) applyModifier((double) baseLvl, rarityModifierPercent);
            if (finalLvl < 1) finalLvl = 1;

            newEnchants.put(entry.getKey(), finalLvl);
            newLore.add(color(" " + c2 + "» &7" + getPolishEnchantName(entry.getKey()) + ": " + c2 + finalLvl));
        }

        if (hasAbility && !abilityLore.isEmpty()) {
            newLore.add("");

            int baseCooldown = 30;
            if (nbtMap != null && nbtMap.containsKey("Cooldown")) {
                try {
                    baseCooldown = Integer.parseInt(String.valueOf(nbtMap.get("Cooldown").getValue()));
                } catch (NumberFormatException ignored) {}
            }

            double modFactor = 1.0 + (rarityModifierPercent / 100.0);
            if (modFactor <= 0.1) modFactor = 0.1;
            double finalCooldown = baseCooldown / modFactor;

            String cooldownStr = String.format(java.util.Locale.US, "%.1f", finalCooldown);

            for (String line : abilityLore) {
                newLore.add(color(line.replace("<cooldown>", c2 + cooldownStr)));
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
        finalMeta.setAttributeModifiers(newAttributes);
        finalMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        String coloredSuffix = "";
        List<String> rarityList = Config.RARITY_LIST.getStringList();
        for (String entry : rarityList) {
            String rawName = ChatColor.stripColor(entry.split(":")[0]).trim();
            if (rawName.equalsIgnoreCase(currentRarityName)) {
                coloredSuffix = entry.split(":")[0];
                break;
            }
        }
        if (coloredSuffix.isEmpty()) coloredSuffix = currentRarityName;

        String baseDisplayName = baseMeta.hasDisplayName() ? baseMeta.getDisplayName() : "&fPrzedmiot";
        if (baseDisplayName.contains(" (")) {
            baseDisplayName = baseDisplayName.substring(0, baseDisplayName.lastIndexOf(" ("));
        }
        finalMeta.setDisplayName(color(baseDisplayName + " &8(" + coloredSuffix + "&8)"));

        finalItem.setItemMeta(finalMeta);

        finalItem.getEnchantments().keySet().forEach(finalItem::removeEnchantment);
        finalItem.addUnsafeEnchantments(newEnchants);

        finalItem = NBTUtil.setString(finalItem, "SkyBlockPlugin", "yes");
        finalItem = NBTUtil.setString(finalItem, "Rarity", currentRarityName);
        finalItem = NBTUtil.setString(finalItem, "ColorFormat", colorFormat);

        if (isDeadMending) {
            finalItem = NBTUtil.setString(finalItem, "deadMending", deadMendingTag);
        }

        if (hasAbility) {
            int baseCooldown = 30;
            if (nbtMap != null && nbtMap.containsKey("Cooldown")) {
                try {
                    baseCooldown = Integer.parseInt(String.valueOf(nbtMap.get("Cooldown").getValue()));
                } catch (NumberFormatException ignored) {}
            }

            double modFactor = 1.0 + (rarityModifierPercent / 100.0);
            if (modFactor <= 0.1) modFactor = 0.1;
            double finalCooldown = baseCooldown / modFactor;

            finalItem = NBTUtil.setInt(finalItem, "Cooldown", (int) finalCooldown);
        }
        if (uniqueText != null) finalItem = NBTUtil.setString(finalItem, "UniqueText", uniqueText);
        if (hasAbility) finalItem = NBTUtil.setString(finalItem, "ABILITY", abilityTag);

        if (isNewDiscovery) {
            playDiscoveryEffect(player, currentRarityName);
        }

        ItemMeta handMeta = itemToBuild.getItemMeta();
        boolean nameChanged = !Objects.equals(handMeta.getDisplayName(), finalMeta.getDisplayName());
        boolean loreChanged = !Objects.equals(handMeta.getLore(), newLore);

        if (nameChanged || loreChanged) {
            if (Config.RARITY_DEBUG.getBoolean()) {
                System.out.println("[DEBUG LOOP] Aktualizacja przedmiotu gracza " + player.getName());
                if (nameChanged) {
                    System.out.println("  Nazwa różna: '" + handMeta.getDisplayName() + "' vs '" + finalMeta.getDisplayName() + "'");
                }
                if (loreChanged) {
                    System.out.println("  Lore różne! Porównanie linii:");
                    List<String> oldL = handMeta.getLore() != null ? handMeta.getLore() : new ArrayList<>();
                    int max = Math.max(oldL.size(), newLore.size());
                    for (int i = 0; i < max; i++) {
                        String l1 = i < oldL.size() ? oldL.get(i) : "BRAK";
                        String l2 = i < newLore.size() ? newLore.get(i) : "BRAK";
                        if (!l1.equals(l2)) {
                            System.out.println("  Linia " + i + ":");
                            System.out.println("    Stara: '" + l1 + "'");
                            System.out.println("    Nowa:  '" + l2 + "'");
                        }
                    }
                }
            }

            player.getInventory().setItem(slot, finalItem);
        }
    }


    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private static double getRarityModifier(String rarityName) {
        List<String> modifiers = Config.RARITY_MODIFIER.getStringList();
        for (String line : modifiers) {
            String[] parts = line.split(":");
            if (parts[0].trim().equalsIgnoreCase(rarityName)) {
                String val = parts[1].replace("+", "").replace("%", "").trim();
                try { return Double.parseDouble(val); } catch (NumberFormatException e) { return 0; }
            }
        }
        return 0;
    }

    private static double applyModifier(double value, double percent) {
        if (Double.isNaN(value) || Double.isNaN(percent)) return value;
        if (value >= 0) {
            return value * (1.0 + (percent / 100.0));
        } else {
            return value * (1.0 - (percent / 100.0));
        }
    }

    public static String getPolishAttributeName(Attribute attr) {
        if (attr == Attribute.ATTACK_DAMAGE) return "Obrażenia";
        if (attr == Attribute.ATTACK_SPEED) return "Prędkość Ataku";
        if (attr == Attribute.MAX_HEALTH) return "Zdrowie";
        if (attr == Attribute.MOVEMENT_SPEED) return "Szybkość";
        if (attr == Attribute.ARMOR) return "Pancerz";
        if (attr == Attribute.ARMOR_TOUGHNESS) return "Wytrzymałość Pancerza";
        if (attr == Attribute.KNOCKBACK_RESISTANCE) return "Odporność na Odepchnięcie";
        if (attr == Attribute.LUCK) return "Szczęście";
        if (attr == Attribute.SCALE) return "Wielkość";
        if (attr == Attribute.BLOCK_INTERACTION_RANGE) return "Zasięg Budowania/Kopania";
        if (attr == Attribute.ENTITY_INTERACTION_RANGE) return "Zasięg Interakcji";
        if (attr == Attribute.MINING_EFFICIENCY) return "Szybkość Kopania";
        if (attr == Attribute.BLOCK_BREAK_SPEED) return "Szybkość Kopania";
        if (attr == Attribute.TEMPT_RANGE) return "Zasięg Oswajania";

        String name = attr.getKey().asString().replace("GENERIC_", "").replace("_", " ").toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public  static String getPolishEnchantName(Enchantment ench) {
        String key = ench.getKey().getKey().toLowerCase();

        switch (key) {
            case "protection":
                return "Ochrona";
            case "fire_protection":
                return "Ochrona przed ogniem";
            case "feather_falling":
                return "Powolne opadanie";
            case "blast_protection":
                return "Ochrona przed wybuchem";
            case "projectile_protection":
                return "Ochrona przed pociskami";
            case "respiration":
                return "Oddychanie";
            case "aqua_affinity":
                return "Wydajność pod wodą";
            case "thorns":
                return "Ciernie";
            case "depth_strider":
                return "Głębinowy wędrowiec";
            case "frost_walker":
                return "Mroźny piechur";
            case "binding_curse":
                return "Klątwa uwiązania";
            case "soul_speed":
                return "Prędkość dusz";
            case "swift_sneak":
                return "Szybkie skradanie";

            case "sharpness":
                return "Ostrość";
            case "smite":
                return "Pogromca nieumarłych";
            case "bane_of_arthropods":
                return "Zjawa stawonogów";
            case "knockback":
                return "Odrzut";
            case "fire_aspect":
                return "Zaklęty ogień";
            case "looting":
                return "Grabież";
            case "sweeping_edge":
                return "Szerokie ostrze";

            case "efficiency":
                return "Wydajność";
            case "silk_touch":
                return "Jedwabny dotyk";
            case "unbreaking":
                return "Niezniszczalność";
            case "fortune":
                return "Szczęście";

            case "power":
                return "Moc";
            case "punch":
                return "Uderzenie";
            case "flame":
                return "Płomień";
            case "infinity":
                return "Nieskończoność";

            case "luck_of_the_sea":
                return "Morska fortuna";
            case "lure":
                return "Przynęta";

            case "loyalty":
                return "Lojalność";
            case "impaling":
                return "Przebicie";
            case "riptide":
                return "Torpeda";
            case "channeling":
                return "Porażenie";

            case "multishot":
                return "Wielostrzył";
            case "quick_charge":
                return "Szybkie ładowanie";
            case "piercing":
                return "Przeszycie";

            case "density":
                return "Gęstość";
            case "breach":
                return "Przełamanie";
            case "wind_burst":
                return "Podmuch wiatru";

            case "mending":
                return "Naprawa";
            case "vanishing_curse":
                return "Klątwa zniknięcia";

            default:
                String formatted = key.replace("_", " ");
                StringBuilder sb = new StringBuilder();
                for (String part : formatted.split(" ")) {
                    if (part.length() > 0) {
                        sb.append(Character.toUpperCase(part.charAt(0)))
                                .append(part.substring(1).toLowerCase())
                                .append(" ");
                    }
                }
                return sb.toString().trim();
        }
    }

    private static void playDiscoveryEffect(Player player, String rarityName) {
        String coloredDisplayName = rarityName;
        for (String entry : Config.RARITY_LIST.getStringList()) {
            String raw = ChatColor.stripColor(entry.split(":")[0]).trim();
            if (raw.equalsIgnoreCase(rarityName)) {
                coloredDisplayName = entry.split(":")[0];
                break;
            }
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(color("&7Zdobyłeś: " + coloredDisplayName)));

        switch (rarityName.toUpperCase()) {
            case "LEGENDARNY":
                player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0));
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

                player.sendTitle(color(coloredDisplayName), color("&7Niesamowite znalezisko!"), 10, 40, 20);
                break;

            case "BOSKI":
            case "7 BOSKI 7":
                player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0));
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.5f);
                player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0f);

                playMatrixAnimation(player);
                break;

            default:
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
                break;
        }
    }

    private static void playMatrixAnimation(Player player) {
        final String coreWord = "BOSKI";
        final String matrixSeven = "&4&k&l7";
        final String revealColor = "&4&l";
        final String hiddenColor = "&4&k&l";

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (!player.isOnline() || index > coreWord.length()) {
                    this.cancel();
                    return;
                }

                StringBuilder titleBuilder = new StringBuilder();

                titleBuilder.append(matrixSeven).append("&r ");

                if (index > 0) {
                    titleBuilder.append(revealColor).append(coreWord.substring(0, index));
                }

                if (index < coreWord.length()) {
                    titleBuilder.append(hiddenColor).append(coreWord.substring(index));
                }

                titleBuilder.append("&r ").append(matrixSeven);

                player.sendTitle(color(titleBuilder.toString()), color("&8&oBoska interwencja..."), 0, 40, 10);

                player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 0.5f, 1.5f);

                index++;

                if (index > coreWord.length()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                }
            }
        }.runTaskTimer(SkyBlockPlugin.getInstance(), 0L, 5L);
    }

    public static void forceReroll(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;

        item = NBTUtil.remove(item, "Rarity");

        player.getInventory().setItemInMainHand(item);

        build(item, player, player.getInventory().getHeldItemSlot());
    }


}
