package com.c78.autocrafter.manager;

import com.c78.autocrafter.data.block.AutoCrafterBlock;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;

public class AutoCrafterUnitRemoveManager {
    private AutoCrafterUnitRemoveManager() {}

    /**
     * Removes the {@link com.c78.autocrafter.data.instance.AutoCrafterUnit} and {@link AutoCrafterBlock},
     * if the given {@link BlockEvent} belongs to an {@link com.c78.autocrafter.data.instance.AutoCrafterUnit}
     * @param event the given {@link BlockEvent}
     * @return true if the operation was successful
     */
    public static boolean removeIfAutoCrafterUnit(BlockEvent event){
        Location location = event.getBlock().getLocation();
        if(!AutoCrafterUnitManager.isAutoCrafterUnit(location)) return false;

        AutoCrafterUnitManager.remove(location);
        if( event instanceof BlockBreakEvent e && e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return false;
        AutoCrafterBlock.breakAutoCrafterBlock(location);
        return true;

    }
}
