package me.lemurxd.skyblockplugin.craftings;

import me.lemurxd.skyblockplugin.Main;
import me.lemurxd.skyblockplugin.enums.Config;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Generator {

    public static ShapedRecipe getRecipe() {
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "stoneGenerator");
        List<String> shapeStrings = (List<String>) Config.GENERATOR_RECIPE_SHAPE.getStringList();
        List<String> ingredientStrings = (List<String>) Config.GENERATOR_RECIPE_SHAPE.getStringList();

        ShapedRecipe recipe = new ShapedRecipe(key, getItemStack());

        for (String entry : ingredientStrings) {
            try {
                String[] parts = entry.split(": ");
                char symbol = parts[0].charAt(0);
                Material material = Material.valueOf(parts[1].trim());

                recipe.setIngredient(symbol, material);
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        return recipe;

    }

    public static ItemStack getItemStack() {
        ItemStack stoniarka = new ItemStack(Material.END_STONE);
        ItemMeta meta = stoniarka.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6§l⭐ MAGICZNA STONIARKA ⭐");

            List<String> lore = new ArrayList<>();
            lore.add("§8§m-----------------------");
            lore.add("§7To urządzenie generuje");
            lore.add("§fNieskończone pokłady kamienia.");
            lore.add("");
            lore.add("§e§lINSTRUKCJA:");
            lore.add(" §8» §fPostaw na ziemi, aby zacząć.");
            lore.add(" §8» §fZniszcz kilofem, by odzyskać.");
            lore.add("");
            lore.add("§c§lUWAGA: §7Działa natychmiastowo!");
            lore.add("§8§m-----------------------");
            meta.setLore(lore);

            meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);

            stoniarka.setItemMeta(meta);
        }

        return stoniarka;
    }

}
