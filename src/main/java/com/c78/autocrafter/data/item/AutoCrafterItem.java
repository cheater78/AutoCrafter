package com.c78.autocrafter.data.item;

import com.c78.autocrafter.data.AutoCrafterConstants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;

public class AutoCrafterItem {
    private AutoCrafterItem(){}

    public static ItemStack get(){
        ItemStack is = new ItemStack(Material.CRAFTING_TABLE);
        is.setAmount(1);
        is.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
        is.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        ItemMeta iMeta = is.getItemMeta();
        iMeta.displayName(Component.text("AutoCrafter"));
        iMeta.lore(
            List.of(
                Component.text( NamedTextColor.DARK_GRAY + "automatically crafts a preset recipe"),
                Component.text( NamedTextColor.DARK_PURPLE + "by c78")
            )
        );
        PersistentDataContainer container = iMeta.getPersistentDataContainer();
        container.set(AutoCrafterConstants.ITEM_NAMESPACED_KEY, PersistentDataType.BOOLEAN, true);
        is.setItemMeta(iMeta);

        return is;
    }

    public static boolean isACItem(ItemStack itemStack){
        return itemStack.getItemMeta().getPersistentDataContainer().has(AutoCrafterConstants.ITEM_NAMESPACED_KEY);
    }

    public static ShapedRecipe getRecipe(){
        ShapedRecipe recipe = new ShapedRecipe(
                Objects.requireNonNull(NamespacedKey.fromString(NamespacedKey.MINECRAFT)),
                AutoCrafterItem.get()
        );
        recipe.shape("GRG","RCR","GHG");
        recipe.setIngredient('C', Material.CRAFTING_TABLE);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        recipe.setIngredient('H', Material.HOPPER);
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        return recipe;
    }

}
