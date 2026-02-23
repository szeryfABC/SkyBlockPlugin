package me.lemurxd.skyblockplugin.listeners;

import io.lumine.mythic.bukkit.MythicBukkit;
import me.lemurxd.skyblockplugin.SkyBlockPlugin;
import me.lemurxd.skyblockplugin.enums.Config;
import me.lemurxd.skyblockplugin.gui.HellHordesMenu;
import me.lemurxd.skyblockplugin.gui.HellSmithMenu;
import me.lemurxd.skyblockplugin.utils.MythicUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;


public class Npc implements Listener {

    @EventHandler
    public void onIslandLoad(PlayerTeleportEvent e) {

        if (e.getTo().getWorld().getName().equals("bskyblock_world_nether") && BentoBox.getInstance().getIslandsManager().isIslandAt(e.getTo())) {


            Island island = BentoBox.getInstance().getIslandsManager().getIslandAt(e.getTo()).get();

            if (SkyBlockPlugin.getData().isKowalUnlocked(island.getUniqueId()) && Config.NPCS_KOWAL_ENABLED.getBoolean() == true) {

                Location location = island.getCenter();
                location.setWorld(Bukkit.getWorld("bskyblock_world_nether"));
                location.add(Config.NPCS_KOWAL_CORDS_X.getInt(), Config.NPCS_KOWAL_CORDS_Y.getInt(), Config.NPCS_KOWAL_CORDS_Z.getInt());

                MythicBukkit.inst().getMobManager().spawnMob(Config.NPCS_KOWAL_MOBNAME.getString(), location);
            }

            if (SkyBlockPlugin.getData().isHordyUnlocked(island.getUniqueId()) && Config.NPCS_HORDY_ENABLED.getBoolean() == true) {

                Location location = island.getCenter();
                location.setWorld(Bukkit.getWorld("bskyblock_world_nether"));
                location.add(Config.NPCS_HORDY_CORDS_X.getInt(), Config.NPCS_HORDY_CORDS_Y.getInt(), Config.NPCS_HORDY_CORDS_Z.getInt());

                MythicBukkit.inst().getMobManager().spawnMob(Config.NPCS_HORDY_MOBNAME.getString(), location);
            }
        }
    }

    @EventHandler
    public void onNpcClick(PlayerInteractEntityEvent e) {
        Entity clickedEntity = e.getRightClicked();
        Player player = e.getPlayer();

        if (MythicUtils.isMob(clickedEntity, Config.NPCS_KOWAL_MOBNAME.getString())) {
            e.setCancelled(true);

            if (!isMemberOfIsland(player, clickedEntity)) {
                player.sendMessage("§cNie jesteś członkiem wyspy, na której stoi ten NPC!");
                return;
            }

            if (!Config.NPCS_KOWAL_ENABLED.getBoolean()) return;

            HellSmithMenu.open(player);

        }
        else if (MythicUtils.isMob(clickedEntity, Config.NPCS_HORDY_MOBNAME.getString())) {
            e.setCancelled(true);

            if (!isMemberOfIsland(player, clickedEntity)) {
                player.sendMessage("§cNie jesteś członkiem wyspy, na której stoi ten NPC!");
                return;
            }

            if (!Config.NPCS_HORDY_ENABLED.getBoolean()) return;

            HellHordesMenu.open(player);
        }
    }

    /**
     * Metoda pomocnicza sprawdzająca czy gracz należy do wyspy, na której znajduje się entity.
     */
    private boolean isMemberOfIsland(Player player, Entity entity) {
        Optional<Island> islandOpt = BentoBox.getInstance().getIslands().getIslandAt(entity.getLocation());

        return islandOpt.isPresent() && islandOpt.get().getMemberSet().contains(player.getUniqueId());
    }

}
