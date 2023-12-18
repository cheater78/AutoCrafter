package com.c78.autocrafter.manager;

import com.c78.autocrafter.data.block.AutoCrafterBlock;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockPlaceEvent;

public class AutoCrafterUnitCreateManager {
    private AutoCrafterUnitCreateManager() {}

    /**
     * Creates a {@link com.c78.autocrafter.data.instance.AutoCrafterUnit} based on a {@link BlockPlaceEvent}
     * @param event the {@link BlockPlaceEvent}
     */
    public static void handleAutoCrafterUnitPlace(BlockPlaceEvent event){
        Location unitLocation = event.getBlock().getLocation();
        AutoCrafterBlock.createAutoCrafterBlock(unitLocation, locationToBlockFace(event.getPlayer().getEyeLocation()).getOppositeFace() );
        AutoCrafterUnitManager.create(unitLocation);
    }

    /**
     * Converts a {@link Location}s Direction to a {@link BlockFace}
     * @param location the {@link Location}
     * @return the determined {@link BlockFace}
     */
    public static BlockFace locationToBlockFace(Location location){
        float yaw = (location.getYaw() + 180 + 45) % 360;
        float pitch = location.getPitch();

        if(pitch < -45.f) return BlockFace.UP;
        if(pitch > +45.f) return BlockFace.DOWN;
        if(yaw < 90.f) return BlockFace.NORTH;
        if(yaw < 180.f) return BlockFace.EAST;
        if(yaw < 270.f) return BlockFace.SOUTH;
        return BlockFace.WEST;
    }
}
