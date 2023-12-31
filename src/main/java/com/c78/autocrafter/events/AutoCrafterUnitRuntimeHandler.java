package com.c78.autocrafter.events;

import com.c78.autocrafter.data.instance.AutoCrafterUnit;
import com.c78.autocrafter.manager.AutoCrafterUnitManager;
import com.c78.autocrafter.manager.AutoCrafterUnitRuntimeManager;
import com.c78.autocrafter.runtime.Runtime;
import io.papermc.paper.event.block.BlockFailedDispenseEvent;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

public class AutoCrafterUnitRuntimeHandler implements Listener {

    /**
     * Called if a {@link Dropper} drops an {@link org.bukkit.entity.Item} into a {@link org.bukkit.World}
     * @param event the corresponding {@link BlockDispenseEvent}
     */
    @EventHandler
    public void onBlockDispenseEvent(BlockDispenseEvent event){
        final Block block = event.getBlock();
        if(AutoCrafterUnitRuntimeManager.isAutoCrafterUnitDropEvent(block)){
            event.setCancelled(true);
            Runtime.getScheduler().scheduleSyncDelayedTask(
                Runtime.plugin,
                () -> AutoCrafterUnitRuntimeManager.handleAutoCrafterUnitDrop(block.getLocation()),
                1
            );
        }
    }

    /**
     * Called if a {@link Dropper} moves an {@link org.bukkit.inventory.ItemStack}
     * to another {@link org.bukkit.inventory.Inventory}
     * @param event the corresponding {@link InventoryMoveItemEvent}
     */
    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event){
        if(event.getSource().getHolder() instanceof Dropper dropper){
            final Block block = dropper.getBlock();
            if(AutoCrafterUnitRuntimeManager.isAutoCrafterUnitDropEvent(block)){
                event.setCancelled(true);
                AutoCrafterUnit unit = AutoCrafterUnitManager.get(block);
                if(AutoCrafterUnitRuntimeManager.isInventoryFull(event.getDestination(), unit.getTargetItem()))
                    return;
                Runtime.getScheduler().scheduleSyncDelayedTask(
                        Runtime.plugin,
                        () -> AutoCrafterUnitRuntimeManager.handleAutoCrafterUnitDrop(block.getLocation()),
                        1
                );
            }
        }
    }

    /**
     * Called if a {@link Dropper} fails to Drop an Item, bc its {@link org.bukkit.inventory.Inventory} is empty
     * @param event the corresponding {@link BlockFailedDispenseEvent}
     */
    @EventHandler
    public void onFailedDispense(BlockFailedDispenseEvent event){
        if(AutoCrafterUnitManager.isAutoCrafterUnit(event.getBlock())){
            event.shouldPlayEffect(false);
        }
    }

}
