package com.c78.autocrafter.manager;

import com.c78.autocrafter.data.instance.AutoCrafterUnit;
import com.c78.autocrafter.runtime.Runtime;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Dropper;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AutoCrafterUnitRuntimeManager {
    private AutoCrafterUnitRuntimeManager() {}

    /*
    Fireworks Meta -> Custom Recipes
    Buckets -> return


     */

    public static boolean isAutoCrafterUnitDropEvent(Block block){
        if(!(block.getState() instanceof Dropper dropper) || !AutoCrafterUnitManager.isAutoCrafterUnit(block)) return false;
        AutoCrafterUnit unit = AutoCrafterUnitManager.get(block);
        if(unit.isActive()){
            unit.decreaseState();
            AutoCrafterUnitManager.save(unit);
            if(unit.isActive())
                dropItem(dropper, unit.getTargetItem());
            return false;
        }else
            return unit.isWaiting();
    }

    public static void handleAutoCrafterUnitDrop(Location location){
        Block block = Bukkit.getServer().getWorld(location.getWorld().getUID()).getBlockAt(location);
        if(!(block.getState() instanceof Dropper dropper) || !AutoCrafterUnitManager.isAutoCrafterUnit(block))
            throw new IllegalArgumentException("BlockDispenseEvent must correspond to a AutoCrafterUnit," +
                    " in order to be handled as such!");
        AutoCrafterUnit unit = AutoCrafterUnitManager.get(block);

        //Inventory of the Dropper
        Inventory unitInventory = dropper.getInventory();

        // Acquire available Items
        Map<ItemStack, Integer> available = new HashMap<>();
        for(ItemStack itemStack : unitInventory.getContents()){
            if(itemStack == null || itemStack.getType().equals(Material.AIR)) continue;

            ItemStack key = itemStack.clone();
            key.setAmount(1);
            available.computeIfPresent(key, ((itemStack1, integer) -> integer + itemStack.getAmount() ));
            available.putIfAbsent(key, itemStack.getAmount());
        }

        //TODO: rework to test with availabe

        //Required Items to Craft, null if uncraftable
        Map<ItemStack, Integer> required = new HashMap<>();
        ItemStack result = new ItemStack(Material.AIR);

        //Check for all Recipes, whether the Materials are there
        for(CraftingRecipe recipe : Runtime.getCraftingRecipesOf(unit.getTargetItem())){
            boolean recipeCraftable = true;
            result=recipe.getResult();

            if(recipe instanceof ShapedRecipe shapedRecipe){
                Map<Character, RecipeChoice> choiceMap = shapedRecipe.getChoiceMap();
                String pattern = String.join("", shapedRecipe.getShape());

                for(Character c : pattern.toCharArray()){
                    RecipeChoice recipeChoice = choiceMap.get(c);
                    boolean choiceFullfilled = false;

                    if(recipeChoice == null)
                        continue;


                    for (ItemStack itemStack : available.keySet()){
                        if(recipeChoice.test(itemStack)){
                            if(
                                    (required.containsKey(itemStack) && available.get(itemStack)-required.get(itemStack) > 0) ||
                                            (!required.containsKey(itemStack) && available.get(itemStack) > 0)
                            ){
                                choiceFullfilled = true;
                                required.computeIfPresent(itemStack, ((itemStack1, integer) -> integer + 1 ));
                                required.putIfAbsent(itemStack, 1);
                                break;
                            }
                        }
                    }
                    if(!choiceFullfilled) {
                        recipeCraftable = false;
                        break;
                    }
                }
            } else if (recipe instanceof ShapelessRecipe shapelessRecipe){
                List<RecipeChoice> choices = shapelessRecipe.getChoiceList();
                for (RecipeChoice choice : choices){
                    boolean choiceFullfilled = false;

                    if(choice == null)
                        continue;

                    for (ItemStack itemStack : available.keySet()){
                        if(choice.test(itemStack)){
                            if(
                                    (required.containsKey(itemStack) && available.get(itemStack)-required.get(itemStack) > 0) ||
                                            (!required.containsKey(itemStack) && available.get(itemStack) > 0)
                            ){
                                choiceFullfilled = true;
                                required.computeIfPresent(itemStack, ((itemStack1, integer) -> integer + 1 ));
                                required.putIfAbsent(itemStack, 1);
                                break;
                            }
                        }
                    }
                    if(!choiceFullfilled) {
                        recipeCraftable = false;
                        break;
                    }
                }
            } else {
                // Recipe slot is empty
            }

            if(recipeCraftable) break;
            else {
                required.clear();
                result = new ItemStack(Material.AIR);
            }
        }

        // ACU stays as WAITING if there is no CraftingRecipe and its resources available
        if(required.isEmpty() || result.getType().equals(Material.AIR) ){
            return;
        }
        Runtime.warn("Required: " + required.entrySet().toString());
        Runtime.warn("Available: " + available.entrySet().toString());

        // Remove the Items that are necessary to craft the Item
        if(!removeResourcesFromContainer(dropper, required))
            throw new NoSuchElementException("Items are missing, to execute this Recipe!");

        // ACU enters ACTIVE state when it's crafting an Item
        unit.setActive(result.getAmount());
        AutoCrafterUnitManager.save(unit);

        // Drop first Item
        dropItem(dropper, result);
    }


    /**
     * Determines, whether an Inventory contains a specified Amount of different Resources
     * @param inventory the {@link Inventory} which is examined
     * @param resources the Map of resources, needed to pass
     * @return true if the {@link Inventory} contains the specified Resources
     */
    public static boolean inventoryContainsResources( @NotNull Inventory inventory, @NotNull Map<ItemStack,Integer> resources){
        for ( Map.Entry<ItemStack, Integer> entry : resources.entrySet() ){
            if(!inventory.containsAtLeast(entry.getKey(), entry.getValue())){
                return false;
            }
        }
        return true;
    }

    /**
     * Drops a specified {@link org.bukkit.entity.Item} as an arbitrary {@link Dropper}
     * @param dropper the {@link Dropper} that drops the {@link org.bukkit.entity.Item}
     * @param itemStack the {@link ItemStack} that defines the dropped {@link org.bukkit.entity.Item}
     */
    public static void dropItem(Dropper dropper, ItemStack itemStack){
        // Item drop, done on virtual Inventory, which is not updated until the original is restored,
        // doesn't work otherwise
        Inventory inventory = dropper.getInventory();

        // Save Inv Contents
        ItemStack[] content = inventory.getContents();

        // set up the Inv for dropping the crafted Item
        inventory.clear();
        inventory.addItem(itemStack);

        // Drop the crafted Item (calls another BlockDispenseEvent here)
        dropper.drop();

        // Restore the Units Inventory
        inventory.clear();
        inventory.setContents(content);
        //dropper.update(); // make sure the Items are set and the Inv is written
    }

    /**
     * Removes specified Resources from a {@link Container}, only if all can be removed
     * @param container the {@link Container} the Resources should be removed from
     * @param resources the Resources that should be removed
     * @return true if all Resources could be removed, so the operation was successful
     */
    public static boolean removeResourcesFromContainer(@NotNull Container container, @NotNull Map<ItemStack,Integer> resources){
        Inventory inventory = container.getInventory();
        Map<ItemStack,Integer> validation = new HashMap<>(resources);

        Runtime.warn("ToDelete: " + resources.entrySet().toString());
        Runtime.warn("Validation: " + validation.entrySet().toString());

        // Iterate over every Resource that needs to be removed
        for(Map.Entry<ItemStack, Integer> entry : resources.entrySet()) {
            ItemStack requiredItemStack = entry.getKey();
            int requiredAmount = entry.getValue();

            // Iterate over every Inventory Slot to check if it matches the required Item in the current Iteration
            for (int i = 0; i < inventory.getSize(); i++){
                ItemStack currentInventoryItemStack = inventory.getItem(i);
                if(currentInventoryItemStack == null || currentInventoryItemStack.getType().equals(Material.AIR)) continue;

                if(requiredItemStack.isSimilar(currentInventoryItemStack)){
                    int currentAmount = currentInventoryItemStack.getAmount();
                    if(currentAmount >= requiredAmount){
                        currentAmount -= requiredAmount;
                        requiredAmount = 0;

                    } else {
                        requiredAmount -= currentAmount;
                        currentAmount = 0;
                    }
                    ItemStack key = currentInventoryItemStack.clone();
                    key.setAmount(1);
                    validation.put(key, requiredAmount);
                    Runtime.warn(key.getType().name() + " " + requiredAmount);

                    currentInventoryItemStack.setAmount(currentAmount);
                    inventory.setItem(i, currentInventoryItemStack);

                }
                if(requiredAmount == 0)
                    break;
            }
        }

        Runtime.warn(resources.entrySet().toString());
        Runtime.warn(validation.entrySet().toString());

        for(Map.Entry<ItemStack, Integer> entry : validation.entrySet()){
            if(entry.getKey().getType().equals(Material.AIR) || validation.get(entry.getKey()) == null)
                continue;
            if(validation.get(entry.getKey()) != 0){
                Runtime.warn(entry.getKey().getType().name());
                container.update();
                return false;
            }
        }
        return true;
    }
}
