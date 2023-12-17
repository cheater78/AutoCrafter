package com.c78.autocrafter.data.instance;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class AutoCrafterUnit {

    private AutoCrafterUnitState state;
    private final Location location;
    private ItemStack targetItem;

    /**
     * Creates a new {@link AutoCrafterUnit} instance
     * @param location, the {@link Location} the {@link AutoCrafterUnit} is positioned at
     */
    public AutoCrafterUnit(Location location){
        this.location = location.toBlockLocation();
        this.state = AutoCrafterUnitState.SETUP;
    }

    /**
     * Creates a new {@link AutoCrafterUnit} instance, taking all attributes for Restoring existing ones
     * @param state the {@link AutoCrafterUnitState} the {@link AutoCrafterUnit} is currently in
     * @param location the {@link Location} the {@link AutoCrafterUnit} is positioned at
     * @param itemStack the {@link ItemStack} the {@link AutoCrafterUnit} is supposed to craft
     */
    public AutoCrafterUnit(AutoCrafterUnitState state, Location location, ItemStack itemStack){
        this.state = state;
        this.location = location.toBlockLocation();
        this.targetItem = itemStack;
    }

    /**
     * @return the {@link AutoCrafterUnitState} the {@link AutoCrafterUnit} is currently in
     */
    public AutoCrafterUnitState getState() {
        return state;
    }

    /**
     * @return the {@link Location} the {@link AutoCrafterUnit} is positioned at
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @return the {@link ItemStack} the {@link AutoCrafterUnit} is supposed to craft
     */
    public ItemStack getTargetItem() {
        return targetItem;
    }

    /**
     * Sets the {@link AutoCrafterUnitState} the {@link AutoCrafterUnit} should be in
     * @param state the new {@link AutoCrafterUnitState}
     */
    public void setState(AutoCrafterUnitState state) {
        this.state = state;
    }

    /**
     * Sets the {@link ItemStack} the {@link AutoCrafterUnit} is supposed to craft
     * @param targetItem the new {@link ItemStack}
     */
    public void setTargetItem(ItemStack targetItem) {
        this.targetItem = targetItem;
    }


    /**
     * @return whether this {@link AutoCrafterUnit} is in {@link AutoCrafterUnitState} ACTIVE
     */
    public boolean isActive(){
        return state.equals(AutoCrafterUnitState.ACTIVE);
    }

    /**
     * @return whether this {@link AutoCrafterUnit} is in {@link AutoCrafterUnitState} WAITING
     */
    public boolean isWaiting(){
        return state.equals(AutoCrafterUnitState.WAITING);
    }

    /**
     * @return whether this {@link AutoCrafterUnit} is in {@link AutoCrafterUnitState} OFF
     */
    public boolean isOff(){
        return state.equals(AutoCrafterUnitState.OFF);
    }

    /**
     * @return whether this {@link AutoCrafterUnit} is in {@link AutoCrafterUnitState} SETUP
     */
    public boolean isInSetup(){
        return state.equals(AutoCrafterUnitState.SETUP);
    }

    /**
     * sets the {@link AutoCrafterUnitState} to ACTIVE
     */
    public void setActive(){
        state = AutoCrafterUnitState.ACTIVE;
    }

    /**
     * sets the {@link AutoCrafterUnitState} to WAITING
     */
    public void setWaiting(){
        state = AutoCrafterUnitState.WAITING;
    }

    /**
     * sets the {@link AutoCrafterUnitState} to OFF
     */
    public void setOff(){
        state = AutoCrafterUnitState.OFF;
    }

    /**
     * resets the {@link AutoCrafterUnit} to {@link AutoCrafterUnitState} SETUP
     */
    public void resetSetup(){
        state = AutoCrafterUnitState.SETUP;
        targetItem = null;
    }

    /**
     * Generates a unique HashCode to identify this {@link AutoCrafterUnit},
     * using the Location since its final and unique to a {@link AutoCrafterUnit}, that is in a world existent
     * @return the HashCode as an int
     */
    @Override
    public int hashCode(){
        return location.hashCode();
    }

    /**
     * Determines if another {@link AutoCrafterUnit}, describes the same {@link AutoCrafterUnit} as this
     * @param obj the other Object that is supposed to be compared
     * @return true if obj is the same as this
     */
    @Override
    public boolean equals(Object obj){
        if(obj == this)
            return true;
        if(obj instanceof AutoCrafterUnit unit)
            return unit.location.equals(this.location);
        return false;
    }

    /**
     * Provides a prettified {@link String} to represent a {@link AutoCrafterUnit}
     * @return the {@link String}
     */
    @Override
    public String toString() {
        return "AutoCrafterUnit{" +
                "state='" + state + '\'' +
                ", location=" + location +
                ", targetItem=" + targetItem +
                '}';
    }

    /**
     * Describes the State a {@link AutoCrafterUnit} can be in
     * SETUP if the {@link AutoCrafterUnit} is placed on a location, but the targetItem is still null
     * WAITING if the {@link AutoCrafterUnit} is configured, but it's not powered or doesn't have all required ingredients to craft the targetItem
     * ACTIVE if the {@link AutoCrafterUnit} is currently outputting the targetItem
     * OFF if the {@link AutoCrafterUnit} was disabled by the User
     */
    public enum AutoCrafterUnitState{
        SETUP("setup"),
        WAITING("waiting"),
        ACTIVE("active"),
        OFF("off")
        ;
        private final String state;
        AutoCrafterUnitState(String state){
            this.state = state;
        }

        @Override
        public String toString(){
            return state;
        }

        public static AutoCrafterUnitState fromString(String state){
            if(state.equals("setup"))
                return SETUP;
            else if (state.equals("active"))
                return ACTIVE;
            else if (state.equals("waiting"))
                return WAITING;
            else if (state.equals("off"))
                return OFF;
            else throw new IllegalArgumentException("Given state is not a valid AutoCrafterUnitState!");
        }
    }


    /**
     * Custom {@link PersistentDataType} to store a {@link AutoCrafterUnit} directly into
     * a {@link org.bukkit.block.TileState}s {@link org.bukkit.persistence.PersistentDataContainer},
     * allowing to have no serialization files, and loading only needed {@link AutoCrafterUnit}s
     */
    public static class UnitContainer implements PersistentDataType<byte[], AutoCrafterUnit> {

        @Override
        public @NotNull Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public @NotNull Class<AutoCrafterUnit> getComplexType() {
            return AutoCrafterUnit.class;
        }

        @Override
        public byte @NotNull [] toPrimitive(@NotNull AutoCrafterUnit autoCrafterUnit, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
                dataOutput.writeObject(autoCrafterUnit.getState().toString());
                dataOutput.writeObject(autoCrafterUnit.getLocation());
                dataOutput.writeObject(autoCrafterUnit.getTargetItem());
                dataOutput.close();
            } catch (Exception ignored) {
                throw new IllegalStateException("Serialization failed!");
            }
            return outputStream.toByteArray();
        }

        @Override
        public @NotNull AutoCrafterUnit fromPrimitive(byte @NotNull [] bytes, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            try {
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                String state = (String) dataInput.readObject();
                Location location = (Location) dataInput.readObject();
                ItemStack itemStack = (ItemStack) dataInput.readObject();

                AutoCrafterUnit unit = new AutoCrafterUnit(AutoCrafterUnitState.fromString(state), location, itemStack);
                return unit;
            } catch (Exception ignored) {
                throw new IllegalStateException("DeSerialization failed!");
            }
        }
    }
}
