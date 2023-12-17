package com.c78.autocrafter.manager;

import com.c78.autocrafter.data.AutoCrafterConstants;
import com.c78.autocrafter.data.instance.AutoCrafterUnit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.persistence.PersistentDataContainer;

public class AutoCrafterUnitManager {
    private AutoCrafterUnitManager(){}

    /**
     * Creates an {@link AutoCrafterUnit} at a specified {@link Block}
     * @param block the {@link Block}
     */
    public static void create(Block block){
        if(block.getState() instanceof Dropper autoCrafterUnitDropper){
            PersistentDataContainer container = autoCrafterUnitDropper.getPersistentDataContainer();
            AutoCrafterUnit unit = new AutoCrafterUnit(block.getLocation());
            container.set(AutoCrafterConstants.UNIT_NAMESPACED_KEY, new AutoCrafterUnit.UnitContainer(), unit);
            autoCrafterUnitDropper.update();
            return;
        }
        throw new NullPointerException("Unit could not be created!");
    }

    /**
     * Creates an {@link AutoCrafterUnit} at a specified {@link Location}
     * @param location the {@link Location}
     */
    public static void create(Location location){
        location = location.toBlockLocation();
        Block block = location.getBlock();
        create(block);
    }

    /**
     * Saves an existing {@link AutoCrafterUnit} instance in its {@link org.bukkit.block.BlockState}
     * @param unit the {@link AutoCrafterUnit}, that should be saved
     */
    public static void save(AutoCrafterUnit unit){
        Location location = unit.getLocation();
        Block block = location.getBlock();
        if(block.getState() instanceof Dropper acb){
            PersistentDataContainer container = acb.getPersistentDataContainer();
            container.set(AutoCrafterConstants.UNIT_NAMESPACED_KEY, new AutoCrafterUnit.UnitContainer(), unit);
            acb.update();
            return;
        }
        throw new IllegalArgumentException("Unit could not be saved!");
    }

    /**
     * Determines, whether a {@link Block} contains an {@link AutoCrafterUnit}
     * @param block the {@link Block} in question
     * @return true if the {@link org.bukkit.block.BlockState} of the {@link Block} contains the {@link AutoCrafterUnit}
     */
    public static boolean isAutoCrafterUnit(Block block){
        if(block.getState() instanceof Dropper acb){
            PersistentDataContainer container = acb.getPersistentDataContainer();
            return container.has(AutoCrafterConstants.UNIT_NAMESPACED_KEY, new AutoCrafterUnit.UnitContainer());
        }
        return false;
    }

    /**
     * Determines, whether a {@link Block} at a {@link Location} contains an {@link AutoCrafterUnit}
     * @param location the {@link Location} of the {@link Block} in question
     * @return true if the {@link org.bukkit.block.BlockState} of the {@link Block}
     *         at the {@link Location} contains the {@link AutoCrafterUnit}
     */
    public static boolean isAutoCrafterUnit(Location location){
        return isAutoCrafterUnit(location.getBlock());
    }


    /**
     * Retrieves the {@link AutoCrafterUnit} from a specified {@link Block}
     * @param block the {@link Block} containing the {@link AutoCrafterUnit} in its {@link org.bukkit.block.BlockState}
     * @return the {@link AutoCrafterUnit}
     */
    public static AutoCrafterUnit get(Block block){
        if(!isAutoCrafterUnit(block))
            throw new IllegalArgumentException("no Unit found at that Location!");
        if(block.getState() instanceof Dropper acb){
            PersistentDataContainer container = acb.getPersistentDataContainer();
            return container.get(AutoCrafterConstants.UNIT_NAMESPACED_KEY, new AutoCrafterUnit.UnitContainer());
        }
        throw new IllegalArgumentException("Requested Unit Block does not implement interface Dropper!");
    }

    /**
     * Retrieves the {@link AutoCrafterUnit} from a specified {@link Location} of a {@link Block}
     * @param location the {@link Location} of the {@link Block},
     *                 containing the {@link AutoCrafterUnit} in its {@link org.bukkit.block.BlockState}
     * @return the {@link AutoCrafterUnit}
     */
    public static AutoCrafterUnit get(Location location){
        location = location.toBlockLocation();
        Block block = location.getBlock();
        return get(block);
    }

    /**
     * Removes the {@link AutoCrafterUnit} from a specified {@link Block}
     * @param block the {@link Block} containing the {@link AutoCrafterUnit} in its {@link org.bukkit.block.BlockState}
     */
    public static void remove(Block block){
        if(!isAutoCrafterUnit(block))
            throw new IllegalArgumentException("no Unit found at that Location!");
        if(block.getState() instanceof Dropper acb){
            PersistentDataContainer container = acb.getPersistentDataContainer();
            container.remove(AutoCrafterConstants.UNIT_NAMESPACED_KEY);
            block.getState().update();
            return;
        }
        throw new IllegalArgumentException("Unit could not be removed!");
    }

    /**
     * Removes the {@link AutoCrafterUnit} from a specified {@link Location} of a {@link Block}
     * @param location the {@link Location} of the {@link Block},
     *                 containing the {@link AutoCrafterUnit} in its {@link org.bukkit.block.BlockState}
     */
    public static void remove(Location location){
        location = location.toBlockLocation();
        Block block = location.getBlock();
        remove(block);
    }
}
