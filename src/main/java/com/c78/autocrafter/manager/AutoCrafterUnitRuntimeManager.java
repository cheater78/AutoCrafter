package com.c78.autocrafter.manager;

import com.c78.autocrafter.data.instance.AutoCrafterUnit;
import com.c78.autocrafter.runtime.Runtime;
import com.destroystokyo.paper.inventory.ItemStackRecipeChoice;
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
            available.computeIfPresent(itemStack, ((itemStack1, integer) -> integer + itemStack.getAmount() ));
            available.putIfAbsent(itemStack, itemStack.getAmount());
        }

        //TODO: rework to test with availabe

        //Required Items to Craft, null if uncraftable
        Map<ItemStack, Integer> required = new HashMap<>();
        ItemStack result = new ItemStack(Material.AIR);

        //Check for all Recipes, whether the Materials are there
        for(CraftingRecipe recipe : Runtime.getCraftingRecipesOf(unit.getTargetItem())){
            required = (recipe instanceof ShapedRecipe shapedRecipe) ? getMaterialsOf(shapedRecipe) :
                    getMaterialsOf((ShapelessRecipe) recipe) ;
            result=recipe.getResult();
            Runtime.warn(required.toString());
            if(inventoryContainsResources(unitInventory, required)) break;
            else {
                required.clear();
                result = new ItemStack(Material.AIR);
            }
        }

        // ACU stays as WAITING if there is no CraftingRecipe and its resources available
        if(required.isEmpty() || result.getType().equals(Material.AIR) ){
            return;
        }

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
     * Acquires all Materials needed for a {@link ShapedRecipe}
     * @param recipe the {@link ShapedRecipe} for {@link ItemStack} acquisition
     * @return a Map with {@link ItemStack}s and their needed {@link Integer} amount,
     *         the amount is not stored inside the {@link ItemStack},
     *         in case that it's larger that the corresponding maxStackSize
     */
    public static List<Map<ItemStack, Integer>> getMaterialsOf(@NotNull ShapedRecipe recipe){
        List<Map<ItemStack, Integer>> requirements = new ArrayList<>();
        String pattern = String.join("", recipe.getShape());
        Map<Character, RecipeChoice> choiceMap = recipe.getChoiceMap();
        for (Map.Entry<Character, RecipeChoice> entry : choiceMap.entrySet() ){
            ItemStack itemStack = getItemStackFromRecipeChoice(entry.getValue());
            if(itemStack == null) continue;

            int amount = 0;
            for (char c : pattern.toCharArray())
                if(entry.getKey().equals(c))
                    amount++;

            int finalAmount = amount*itemStack.getAmount();
            required.computeIfPresent(itemStack, ((iStack, integer) -> integer + finalAmount));
            required.putIfAbsent(itemStack, finalAmount);
        }
        return required;
    }

    /**
     * Acquires all Materials needed for a {@link ShapelessRecipe}
     * @param recipe the {@link ShapelessRecipe} for {@link ItemStack} acquisition
     * @return a Map with {@link ItemStack}s and their needed {@link Integer} amount,
     *         the amount is not stored inside the {@link ItemStack},
     *         in case that it's larger that the corresponding maxStackSize
     */
    public static Map<ItemStack, Integer> getMaterialsOf(@NotNull ShapelessRecipe recipe){
        Map<ItemStack, Integer> required = new HashMap<>();
        for (RecipeChoice recipeChoice : recipe.getChoiceList() ){
            ItemStack itemStack = getItemStackFromRecipeChoice(recipeChoice);
            if(itemStack == null) continue;

            int finalAmount = itemStack.getAmount();
            required.computeIfPresent(itemStack, ((iStack, integer) -> integer + finalAmount));
            required.putIfAbsent(itemStack, finalAmount);
        }
        return required;
    }

    private static List<ItemStack> getItemStackFromRecipeChoice(RecipeChoice recipeChoice){
        if(recipeChoice instanceof RecipeChoice.ExactChoice exactChoice) {
            Runtime.warn(exactChoice.getChoices().toString());
            return exactChoice.getChoices();
        }
        else if (recipeChoice instanceof RecipeChoice.MaterialChoice materialChoice) {
            Runtime.warn(materialChoice.getChoices().toString());
            List<ItemStack> items = new ArrayList<>();
            for(Material material : materialChoice.getChoices()){
                items.add(new ItemStack(material));
            }
            return items;
        }
        else if (recipeChoice instanceof ItemStackRecipeChoice choice){
            throw new IllegalStateException("PaperOnly doodoo Recipe! dafuq u doin");
        } else return null;
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

        // Iterate over every Resource that needs to be removed
        for(Map.Entry<ItemStack, Integer> entry : resources.entrySet()) {
            ItemStack requiredItemStack = entry.getKey();
            int requiredAmount = entry.getValue();

            // Iterate over every Inventory Slot to check if it matches the required Item in the current Iteration
            for (int i = 0; i < inventory.getSize(); i++){
                ItemStack currentInventoryItemStack = inventory.getItem(i);
                if(currentInventoryItemStack == null) continue;

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
                    currentInventoryItemStack.setAmount(currentAmount);
                    inventory.setItem(i, currentInventoryItemStack);
                    key.setAmount(1);
                    validation.put(key, requiredAmount);
                }
                if(requiredAmount == 0)
                    break;
            }
        }
        for(Map.Entry<ItemStack, Integer> entry : validation.entrySet()){
            if(validation.get(entry.getKey()) != 0){
                container.update();
                return false;
            }
        }
        return true;
    }
}
