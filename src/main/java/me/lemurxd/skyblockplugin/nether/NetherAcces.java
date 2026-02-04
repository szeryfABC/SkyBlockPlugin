package me.lemurxd.skyblockplugin.nether;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public class NetherAcces {

    public void unlock(Island island) {
        Optional<Flag> netherFlagOpt = BentoBox.getInstance().getFlagsManager().getFlag("NETHER_PORTAL");

        netherFlagOpt.ifPresent(netherFlag -> island.setFlag(netherFlag, 500, false));
    }
}
