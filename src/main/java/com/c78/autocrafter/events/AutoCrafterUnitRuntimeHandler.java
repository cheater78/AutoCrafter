package com.c78.autocrafter.events;

import com.c78.autocrafter.data.instance.AutoCrafterUnit;
import com.c78.autocrafter.manager.AutoCrafterUnitManager;
import com.c78.autocrafter.runtime.Runtime;
import io.papermc.paper.event.block.BlockFailedDispenseEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.c78.autocrafter.manager.AutoCrafterRuntimeManager.getMaterialsOf;
import static com.c78.autocrafter.manager.AutoCrafterRuntimeManager.inventoryContainsResources;
import static com.c78.autocrafter.manager.AutoCrafterRuntimeManager.removeResourcesFromContainer;
import static com.c78.autocrafter.manager.AutoCrafterRuntimeManager.dropItem;

public class AutoCrafterUnitRuntimeHandler implements Listener {

    private static String indent = "";

    @EventHandler
    public void onBlockDispenseEvent(BlockDispenseEvent event){

        //TODO: DEBUG event Begin
        Runtime.warn(indent +"###BlockDispenseEvent###");
        indent += "  ";

        Block block = event.getBlock();
        if(!(block.getState() instanceof Dropper dropper) || !AutoCrafterUnitManager.isAutoCrafterUnit(block)) return;
        AutoCrafterUnit unit = AutoCrafterUnitManager.get(block);


        //TODO: DEBUG unit state
        Runtime.warn(indent +"Unit State: " + unit.getState().toString());


        if(unit.isActive()){
            //TODO: DEBUG Item Dropped
            Runtime.warn(indent +"Dropped Item(No Cancel)");
            unit.setWaiting();
            AutoCrafterUnitManager.save(unit);
            //TODO - DEBUG new state
            Runtime.warn(indent +"New Unit State: " + AutoCrafterUnitManager.get(block).getState().toString());
            //TODO - DEBUG
            indent = indent.substring(0, Math.max(0, indent.length()-3));
            Runtime.warn(indent +"~~~BlockDispenseEvent~~~");
            return;
        }else if(!unit.isWaiting()) return;
        event.setCancelled(true);

        //Inventory of the Dropper
        Inventory unitInventory = dropper.getInventory();





        //TODO: DEBUG - Inventory
        {
            String debug = "Inventory: (free: ";
            String slots = ") {";
            int freeSlots = 0;
            for(ItemStack itemStack : unitInventory.getContents()){
                if(itemStack == null || itemStack.getType().equals(Material.AIR)){
                    freeSlots++;
                    continue;
                }
                slots += " {" + itemStack.getType() + "," + itemStack.getAmount() + "} ";
            }
            Runtime.warn(indent +debug + freeSlots + slots + "}");
        }

        //Compensate Dropped Item
        //unitInventory.addItem(event.getItem());
        //dropper.update(); //make sure the Item is added and the Inventory is written

        //Delete EventItem so it doesn't get put back - DANGER - AIR might not fit?!
        event.setItem(new ItemStack(Material.AIR));

        //TODO: DEBUG - Inventory
        {
            String debug = "Inventory: (free: ";
            String slots = ") {";
            int freeSlots = 0;
            for(ItemStack itemStack : unitInventory.getContents()){
                if(itemStack == null || itemStack.getType().equals(Material.AIR)){
                    freeSlots++;
                    continue;
                }
                slots += " {" + itemStack.getType() + "," + itemStack.getAmount() + "} ";
            }
            Runtime.warn(indent +debug + freeSlots + slots + "}");

        }






        //Required Items to Craft, null if uncraftable
        Map<ItemStack, Integer> required = new HashMap<>();

        //Check for all Recipes, whether the Materials are there
        {
            for(CraftingRecipe recipe : Runtime.getCraftingRecipesOf(unit.getTargetItem())){
                required = (recipe instanceof ShapedRecipe shapedRecipe) ? getMaterialsOf(shapedRecipe) :
                                                                           getMaterialsOf((ShapelessRecipe) recipe) ;
                if(inventoryContainsResources(unitInventory, required)) break;
                else required.clear();
            }
        }

        //TODO: DEBUG required Materials
        if(required.isEmpty())
            Runtime.warn(indent +"No Recipe matching!");
        else
            Runtime.warn(indent +required.entrySet().toString());

        // ACU stays as WAITING if there is no CraftingRecipe and its resources available
        if(required.isEmpty()){
            //TODO - DEBUG
            indent = indent.substring(0, Math.max(0, indent.length()-3));
            Runtime.warn(indent +"~~~BlockDispenseEvent~~~");
            return;
        }

        // ACU enters ACTIVE state when it's crafting an Item
        unit.setActive();
        AutoCrafterUnitManager.save(unit);

        // Remove the Items that are necessary to craft the Item
        if(!removeResourcesFromContainer(dropper, required))
            throw new NoSuchElementException("Items are missing, to execute this Recipe!");

        // Drop Item
        dropItem(dropper, unit.getTargetItem());

        //TODO - DEBUG
        indent = indent.substring(0, Math.max(0, indent.length()-3));
        Runtime.warn(indent +"~~~BlockDispenseEvent~~~");

    }


    @EventHandler
    public void onFailedDispense(BlockFailedDispenseEvent event){
        Runtime.warn(indent +"FailedDispenseEvent");

    }

}
