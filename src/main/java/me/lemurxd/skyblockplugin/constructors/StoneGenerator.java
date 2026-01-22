package me.lemurxd.skyblockplugin.constructors;

import org.bukkit.Location;
import world.bentobox.bentobox.database.objects.Island;

import java.util.HashMap;

public class StoneGenerator {

    private static HashMap<Island, StoneGenerator> generatorMap = new HashMap<>();

    private Location location;
    private Island island;


    public StoneGenerator(Island island, Location location) {
        this.island = island;
        this.location = location;
        generatorMap.put(island, new StoneGenerator(island, location));
    }

    public static void create(Island island, Location location) {
        StoneGenerator generator = new StoneGenerator(island, location);
        getMap().put(island, generator);
    }

    public static HashMap<Island, StoneGenerator> getMap() {
        return generatorMap;
    }

}
