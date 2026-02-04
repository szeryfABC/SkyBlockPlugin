package me.lemurxd.skyblockplugin.craftings;

import me.lemurxd.skyblockplugin.SkyBlockPlugin;
import me.lemurxd.skyblockplugin.constructors.StoneGenerator;
import me.lemurxd.skyblockplugin.enums.Config;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;

public class Generator {

    public static ShapedRecipe getRecipe() {
        NamespacedKey key = new NamespacedKey(SkyBlockPlugin.getInstance(), "stoneGenerator");
        List<String> shapeStrings = (List<String>) Config.GENERATOR_RECIPE_SHAPE.getStringList();
        List<String> ingredientStrings = (List<String>) Config.GENERATOR_RECIPE_INGREDIENTS.getStringList();

        ShapedRecipe recipe = new ShapedRecipe(key, StoneGenerator.getItemStack());
        recipe.shape(shapeStrings.toArray(new String[0]));

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

}
