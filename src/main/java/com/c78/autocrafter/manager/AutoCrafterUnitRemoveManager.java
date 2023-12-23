package com.c78.autocrafter.manager;

import com.c78.autocrafter.data.block.AutoCrafterBlock;
import com.c78.autocrafter.runtime.Runtime;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;

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
        Block block = location.getBlock();
        if(!AutoCrafterUnitManager.isAutoCrafterUnit(location)) return false;

        for(Player p : location.getWorld().getPlayers()){
            InventoryView view = p.getOpenInventory();
            if(view instanceof CraftingInventory craftingInventory){
                if(craftingInventory.getLocation() == null) continue;
                Runtime.warn(craftingInventory.getLocation().toString());
                Runtime.warn(location.toString());
                if(craftingInventory.getLocation().equals(location)){
                    craftingInventory.clear();
                }
            }
        }


        AutoCrafterUnitManager.remove(block);
        if( event instanceof BlockBreakEvent e && e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return false;
        AutoCrafterBlock.breakAutoCrafterBlock(location);
        return true;

    }
}
