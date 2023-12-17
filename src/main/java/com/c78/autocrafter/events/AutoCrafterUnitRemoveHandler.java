package com.c78.autocrafter.events;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import static com.c78.autocrafter.manager.AutoCrafterUnitRemoveManager.removeIfAutoCrafterUnit;

public class AutoCrafterUnitRemoveHandler implements Listener {

    /**
     * Called when a {@link org.bukkit.entity.Player} breaks a {@link org.bukkit.block.Block}
     * @param event the corresponding {@link BlockBreakEvent}
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        event.setCancelled(removeIfAutoCrafterUnit(event));
    }

    /**
     * Called when a {@link org.bukkit.block.Block} gets destroyed by any means
     * @param event the corresponding {@link BlockDestroyEvent}
     */
    //TODO: doesnt really work that way
    @EventHandler
    public void onBlockDestroy(BlockDestroyEvent event){
        event.setCancelled(removeIfAutoCrafterUnit(event));
    }

}
