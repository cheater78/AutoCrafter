package com.c78.autocrafter.manager;

import com.c78.autocrafter.data.AutoCrafterConstants;
import com.c78.autocrafter.data.instance.AutoCrafterUnit;
import com.c78.autocrafter.runtime.Runtime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class AutoCrafterUnitSetupManager {
    private AutoCrafterUnitSetupManager() {}

    private static final ItemStack DISABLED = createDisabledFieldPlaceholder();

    // UI SLOTS (WORKBENCH -> 0 must be set last!)
    private static final int ACU_TARGET_ITEM_UI_SLOT = 0;
    private static final int ACU_ENABLE_UI_SLOT = 9;

    private static final ItemStack UI_BUTTON_ENABLE_ON = createOptionEnableOn();
    private static final ItemStack UI_BUTTON_ENABLE_OFF = createOptionEnableOff();

    /**
     * Determines, whether a {@link InventoryClickEvent} is triggert inside an AutoCrafterUnitSetupUI
     * @param event the {@link InventoryClickEvent} in question
     * @return true if all conditions are matched
     */
    public static boolean isAutoCrafterUnitSetupUIClickEvent(InventoryClickEvent event){
        return event.getClickedInventory() != null
                && event.getClickedInventory().getType().equals(InventoryType.WORKBENCH)
                && event.getClickedInventory() instanceof CraftingInventory craftingInventory
                && craftingInventory.getLocation() != null
                && AutoCrafterUnitManager.isAutoCrafterUnit(craftingInventory.getLocation());
    }

    /**
     * Handles any UI-Input to manipulate the {@link AutoCrafterUnit}, this {@link InventoryView} corresponds to
     * @param event the {@link InventoryClickEvent}
     * @return false if the Input was illegal, the Event is already cancelled and therefor nothing else should happen
     */
    public static boolean handleAutoCrafterUnitSetupUIClickEvent(InventoryClickEvent event){
        if(!(event.getClickedInventory() instanceof CraftingInventory craftingInventory))
            throw new IllegalArgumentException("Check InventoryClickEvent before passing to Manager!");
        InventoryView view = event.getView();
        AutoCrafterUnit unit = AutoCrafterUnitManager.get(craftingInventory.getLocation());
        int clickedSlot = event.getSlot();
        event.setCancelled(true);


        if(clickedSlot == view.convertSlot(ACU_TARGET_ITEM_UI_SLOT)){
            ItemStack currentItem = event.getCursor();
            if(currentItem.getType().equals(Material.AIR)
                    || Runtime.getCraftingRecipesOf(currentItem).isEmpty()
            ) return false;
            ItemStack save = currentItem.clone();
            save.setAmount(1);
            unit.setTargetItem(save);
            unit.setWaiting();
        }
        else if (event.getSlot() == view.convertSlot(ACU_ENABLE_UI_SLOT)){
            if(!unit.isOff())
                unit.setOff();
            else if(unit.getTargetItem() == null)
                unit.resetSetup();
            else
                unit.setWaiting();
        }

        AutoCrafterUnitManager.save(unit);
        return true;
    }

    /**
     * Shows the {@link Player} a {@link org.bukkit.inventory.Inventory}-UI to Configure an {@link AutoCrafterUnit}
     * @param targetPlayer the viewing {@link Player}
     * @param setupUnit the configurable {@link AutoCrafterUnit}
     */
    public static void showAutoCrafterUnitSetupUI(Player targetPlayer, AutoCrafterUnit setupUnit){
        InventoryView view;
        if(!targetPlayer.getOpenInventory().getType().equals(InventoryType.WORKBENCH))
            view = targetPlayer.openWorkbench(setupUnit.getLocation(), true);
        else view = targetPlayer.getOpenInventory();
        if(view == null) return;
        view.setTitle(AutoCrafterConstants.ACU_CONFIG_UI_TITLE);

        fillTopInventoryView(view, DISABLED);

        insertOptionEnable(view, setupUnit);

        insertTargetItem(view, setupUnit);
    }

    /**
     * Clears the WorkbenchInventory when its closed, so the player doesn't get the contents transferred to his
     * @param event the {@link InventoryCloseEvent}
     */
    public static void onAutoCrafterUnitSetupUIClose(InventoryCloseEvent event){
        InventoryView view = event.getView();
        if(view.getTopInventory().getType().equals(InventoryType.WORKBENCH)
                && view.getTopInventory() instanceof CraftingInventory craftingInventory
                && craftingInventory.getLocation() != null
                && AutoCrafterUnitManager.isAutoCrafterUnit(craftingInventory.getLocation()))
            view.getTopInventory().clear();
    }

    /**
     * Shows Option to Enable/Disable an {@link AutoCrafterUnit} in a Top-{@link org.bukkit.inventory.Inventory}
     * of {@link InventoryType}.WORKBENCH in the ACU_ENABLE_UI_SLOT
     * @param view the {@link InventoryView}
     * @param unit the {@link AutoCrafterUnit}
     */
    private static void insertOptionEnable(InventoryView view, AutoCrafterUnit unit){
        if(!unit.isOff())
            view.setItem(view.convertSlot(ACU_ENABLE_UI_SLOT), UI_BUTTON_ENABLE_ON);
        else
            view.setItem(view.convertSlot(ACU_ENABLE_UI_SLOT), UI_BUTTON_ENABLE_OFF);
    }

    /**
     * Shows the targetItem of an {@link AutoCrafterUnit} in a Top-{@link org.bukkit.inventory.Inventory}
     * of {@link InventoryType}.WORKBENCH in the TARGET_ITEM_UI_SLOT
     * @param view the {@link InventoryView}
     * @param unit the {@link AutoCrafterUnit}
     */
    private static void insertTargetItem(InventoryView view, AutoCrafterUnit unit){
        if(unit.getTargetItem() == null) {
            ItemStack dummy = new ItemStack(Material.BARRIER);
            ItemMeta meta = dummy.getItemMeta();
            meta.displayName(Component.text("No Item selected!(drop one here)", NamedTextColor.RED));
            dummy.setItemMeta(meta);
            view.setItem(view.convertSlot(ACU_TARGET_ITEM_UI_SLOT), dummy);
        }
        else view.setItem(view.convertSlot(ACU_TARGET_ITEM_UI_SLOT), unit.getTargetItem());
    }

    /**
     * @return an {@link ItemStack} as the Enabled Button for the {@link AutoCrafterUnit} SetupUI
     */
    private static ItemStack createOptionEnableOn(){
        ItemStack itemStack = new ItemStack( Material.RED_CONCRETE );
        ItemMeta iMeta = itemStack.getItemMeta();
        iMeta.displayName(Component.join(JoinConfiguration.builder().build(), List.of(
                Component.text("Enabled : ", NamedTextColor.BLUE),
                Component.text("True", NamedTextColor.RED)
        )));
        itemStack.setItemMeta(iMeta);
        return itemStack;
    }

    /**
     * @return an {@link ItemStack} as the Disable Button for the {@link AutoCrafterUnit} SetupUI
     */
    private static ItemStack createOptionEnableOff(){
        ItemStack itemStack = new ItemStack( Material.GRAY_CONCRETE );
        ItemMeta iMeta = itemStack.getItemMeta();
        iMeta.displayName(Component.join(JoinConfiguration.builder().build(), List.of(
                Component.text("Enabled: ", NamedTextColor.BLUE),
                Component.text("False", NamedTextColor.DARK_GRAY)
        )));
        itemStack.setItemMeta(iMeta);
        return itemStack;
    }


    /*
     * General use functions
     */

    /**
     * Fills, all Slots with an {@link ItemStack}
     * of a Top-{@link org.bukkit.inventory.Inventory} of an {@link InventoryView}
     * @param view the {@link InventoryView}
     * @param itemStack the {@link ItemStack}
     */
    private static void fillTopInventoryView(InventoryView view, ItemStack itemStack){
        for (int i = 0; i < view.getTopInventory().getSize(); i++)
            view.setItem(view.convertSlot(i), itemStack);
    }

    /**
     * @return an {@link ItemStack} as a Placeholder for not used Fields in Inventory UIs
     */
    private static ItemStack createDisabledFieldPlaceholder() {
        ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(Component.text(""));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

}
