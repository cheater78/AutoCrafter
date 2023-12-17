package com.c78.autocrafter.data.block;

import com.c78.autocrafter.data.AutoCrafterConstants;
import com.c78.autocrafter.data.item.AutoCrafterItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dropper;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.ItemStack;

public class AutoCrafterBlock {
    private AutoCrafterBlock() { }

    /** NEVER CHANGE
     *  must be {@link Material}.DROPPER, {@link Dropper} interface is used
     */
    private static final Material autoCrafterBlockMaterial = Material.DROPPER;

    /**
     * Places an InGame {@link Block} representation of an {@link com.c78.autocrafter.data.instance.AutoCrafterUnit},
     * at the specified {@link Location}
     * @param location the specified {@link Location}
     * @param direction the {@link BlockFace} Direction the {@link Block} should face
     */
    public static void createAutoCrafterBlock(Location location, BlockFace direction){
        Block block = location.getBlock();
        block.setType(autoCrafterBlockMaterial);

        BlockData blockData = Bukkit.getServer().createBlockData(autoCrafterBlockMaterial);
        ((Directional) blockData).setFacing(direction);
        block.setBlockData(blockData);

        Dropper blockState = (Dropper) block.getState();
        blockState.customName(AutoCrafterConstants.ACU_DISPLAYNAME);

        blockState.update();
    }

    /**
     * Breaks the {@link AutoCrafterBlock} at the specified {@link Location}
     * and drops it as {@link org.bukkit.entity.Item} at its position
     * spawns some {@link Particle}s to indicate its removal
     * @param location the {@link Location} of the {@link AutoCrafterBlock}
     */
    public static void breakAutoCrafterBlock(Location location){
        breakAndDrop(location, AutoCrafterItem.get());
        spawnUnitDestroyParticles(location);
    }

    /**
     * Removes a {@link Block} at a given {@link Location} and Drops an {@link ItemStack}
     * @param location the {@link Location} of the {@link Block}
     * @param itemStack the dropped {@link ItemStack}
     */
    private static void breakAndDrop(Location location, ItemStack itemStack){
        World world = location.getWorld();
        Block block = location.getBlock();
        location = location.toCenterLocation();

        block.setType(Material.AIR);
        world.dropItemNaturally(location, itemStack);
    }

    /**
     * Spawns 100 purple 'ish Particles at a {@link Location}
     * @param location the {@link Location}
     */
    private static void spawnUnitDestroyParticles(Location location){
        World currentWorld = location.getWorld();
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(200, 0, 255), 1.0F);
        currentWorld.spawnParticle(Particle.REDSTONE, location.toCenterLocation(), 100, dustOptions);
    }
}
