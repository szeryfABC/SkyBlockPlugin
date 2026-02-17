package me.lemurxd.skyblockplugin.nether;

import me.lemurxd.skyblockplugin.SkyBlockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public class NetherAcces implements Listener {

    public void setPortalLimit(Island island, String flagName, Integer amount) {
        Optional<Flag> flagOpt = BentoBox.getInstance().getFlagsManager().getFlag(flagName);

        flagOpt.ifPresent(flag -> {

            island.setFlag(flag, amount, false);
        });
    }

    @EventHandler
    public void onPortalEnter(EntityPortalEnterEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!event.getLocation().getWorld().getName().equals("bskyblock_world")) {
            return;
        }

        Material blockType = event.getLocation().getBlock().getType();
        boolean isAllowed = false;
        String targetFlag = "";

        Optional<Island> islandOpt = BentoBox.getInstance().getIslands().getIslandAt(event.getLocation());

        if (islandOpt.isPresent()) {

            Island island = islandOpt.get();
            String islandId = island.getUniqueId().toString();

            if (blockType == Material.NETHER_PORTAL) {
                targetFlag = "NETHER_PORTAL";
                isAllowed = SkyBlockPlugin.getData().isNetherUnlocked(islandId);
            } else if (blockType == Material.END_PORTAL) {
                targetFlag = "END_PORTAL";
                isAllowed = SkyBlockPlugin.getData().isEndUnlocked(islandId);
            } else {
                return;
            }

            if (isAllowed) {
                setPortalLimit(island, targetFlag, 500);
            } else {
                setPortalLimit(island, targetFlag, 2000);
            }

        }
    }
}
