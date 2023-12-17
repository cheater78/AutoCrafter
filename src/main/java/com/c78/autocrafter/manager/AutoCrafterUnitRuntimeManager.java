package com.c78.autocrafter.manager;

import org.bukkit.block.Container;
import org.bukkit.block.Dropper;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AutoCrafterUnitRuntimeManager {
    private AutoCrafterUnitRuntimeManager() {}


    /**
     * Acquires all Materials needed for a {@link ShapedRecipe}
     * @param recipe the {@link ShapedRecipe} for {@link ItemStack} acquisition
     * @return a Map with {@link ItemStack}s and their needed {@link Integer} amount,
     *         the amount is not stored inside the {@link ItemStack},
     *         in case that it's larger that the corresponding maxStackSize
     */
    public static Map<ItemStack, Integer> getMaterialsOf(@NotNull ShapedRecipe recipe){
        Map<ItemStack, Integer> required = new HashMap<>();
        String pattern = String.join("", recipe.getShape());
        Map<Character, RecipeChoice> choiceMap = recipe.getChoiceMap();
        for (Map.Entry<Character, RecipeChoice> entry : choiceMap.entrySet() ){
            ItemStack itemStack;
            if(entry.getValue() instanceof RecipeChoice.ExactChoice exactChoice)
                itemStack = exactChoice.getItemStack();
            else if (entry.getValue() instanceof RecipeChoice.MaterialChoice materialChoice)
                itemStack = materialChoice.getItemStack();
            else throw new IllegalStateException("BIGGEST OUF!");

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
            ItemStack itemStack;
            if(recipeChoice instanceof RecipeChoice.ExactChoice exactChoice)
                itemStack = exactChoice.getItemStack();
            else if (recipeChoice instanceof RecipeChoice.MaterialChoice materialChoice)
                itemStack = materialChoice.getItemStack();
            else throw new IllegalStateException("BIGGEST OUF!");

            int finalAmount = itemStack.getAmount();
            required.computeIfPresent(itemStack, ((iStack, integer) -> integer + finalAmount));
        }
        return required;
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
        dropper.update(); // make sure the Items are set and the Inv is written
    }

    /**
     * Removes specified Resources from a {@link Container}, only if all can be removed
     * @param container the {@link Container} the Resources should be removed from
     * @param resources the Resources that should be removed
     * @return true if all Resources could be removed, so the operation was successful
     */
    public static boolean removeResourcesFromContainer(@NotNull Container container, @NotNull Map<ItemStack,Integer> resources){
        Inventory inventory = container.getInventory();
        Map<ItemStack,Integer> validation = new HashMap<>();

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
                    currentInventoryItemStack.setAmount(currentAmount);
                    inventory.setItem(i, currentInventoryItemStack);
                    validation.put(currentInventoryItemStack, requiredAmount);
                }
            }
        }

        for(Map.Entry<ItemStack, Integer> entry : validation.entrySet()){
            if(validation.get(entry.getKey()) != 0){
                return false;
            }
        }
        container.update();
        return true;
    }
}
