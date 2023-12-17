package com.c78.autocrafter.events;

import com.c78.autocrafter.data.instance.AutoCrafterUnit;
import com.c78.autocrafter.manager.AutoCrafterUnitSetupManager;
import com.c78.autocrafter.manager.AutoCrafterUnitManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;

public class AutoCrafterUnitSetupHandler implements Listener {

    /**
     * Called when a {@link Player} (L,R-Clicks) (AIR, BLOCK)
     * @param event the corresponding {@link PlayerInteractEvent}
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Block block = event.getClickedBlock();
        if(block == null || !AutoCrafterUnitManager.isAutoCrafterUnit(block)) return;
        Player p = event.getPlayer();
        AutoCrafterUnit unit = AutoCrafterUnitManager.get(block);

        boolean hasItemInHand = event.getItem() != null;
        boolean isSneaking = p.isSneaking();
        boolean inSetup = unit.isInSetup();

        if( (hasItemInHand && isSneaking) || (!isSneaking && !inSetup) ) return;

        AutoCrafterUnitSetupManager.showAutoCrafterUnitSetupUI(p, unit);
        event.setCancelled(true);
    }

    /**
     * Called when a {@link Player} (L,R,SHIFT-L,R-Clicks) a Field in an {@link Inventory}
     * @param event the corresponding {@link InventoryClickEvent}
     */
    @EventHandler
    public void onInvClick(InventoryClickEvent event){
        if(AutoCrafterUnitSetupManager.isAutoCrafterUnitSetupUIClickEvent(event)
            && AutoCrafterUnitSetupManager.handleAutoCrafterUnitSetupUIClickEvent(event)){
            if(!(event.getClickedInventory() instanceof CraftingInventory craftingInventory))
                throw new IllegalArgumentException("Check InventoryClickEvent before passing to Manager!");
            Player p = (Player) event.getWhoClicked();
            AutoCrafterUnit unit = AutoCrafterUnitManager.get(craftingInventory.getLocation());
            AutoCrafterUnitSetupManager.showAutoCrafterUnitSetupUI(p, unit);
        }


    }

    /**
     * Called when a {@link Player} closes an {@link Inventory}
     * @param event the corresponding {@link InventoryCloseEvent}
     */
    @EventHandler
    public void onInvClose(InventoryCloseEvent event){
        AutoCrafterUnitSetupManager.onAutoCrafterUnitSetupUIClose(event);
    }
}
