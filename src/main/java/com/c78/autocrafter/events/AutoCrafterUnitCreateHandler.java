package com.c78.autocrafter.events;

import com.c78.autocrafter.data.block.AutoCrafterBlock;
import com.c78.autocrafter.data.item.AutoCrafterItem;
import com.c78.autocrafter.manager.AutoCrafterUnitManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class AutoCrafterUnitCreateHandler implements Listener {

    /**
     * Called when a {@link org.bukkit.block.Block} is placed by a {@link org.bukkit.entity.Player}
     * @param event the corresponding {@link BlockPlaceEvent}
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(!AutoCrafterItem.isACItem(event.getItemInHand())) return;
        Location unitLocation = event.getBlock().getLocation();

        AutoCrafterBlock.createAutoCrafterBlock(unitLocation, event.getPlayer().getFacing().getOppositeFace());
        AutoCrafterUnitManager.create(unitLocation);
    }

}
