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

    private final AutoCrafterUnitState state;
    private final Location location;
    private ItemStack targetItem;

    /**
     * Creates a new {@link AutoCrafterUnit} instance
     * @param location, the {@link Location} the {@link AutoCrafterUnit} is positioned at
     */
    public AutoCrafterUnit(Location location){
        this.location = location.toBlockLocation();
        this.state = new AutoCrafterUnitState(); // SETUP as default
    }

    /**
     * Creates a new {@link AutoCrafterUnit} instance, taking all attributes for Restoring existing ones
     * @param state the {@link String} of the {@link AutoCrafterUnitState} the {@link AutoCrafterUnit} is currently in
     * @param location the {@link Location} the {@link AutoCrafterUnit} is positioned at
     * @param itemStack the {@link ItemStack} the {@link AutoCrafterUnit} is supposed to craft
     */
    public AutoCrafterUnit(String state, Location location, ItemStack itemStack){
        this.state = AutoCrafterUnitState.fromString(state);
        this.location = location.toBlockLocation();
        this.targetItem = itemStack;
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
        return state.isActive();
    }

    /**
     * @return whether this {@link AutoCrafterUnit} is in {@link AutoCrafterUnitState} WAITING
     */
    public boolean isWaiting(){
        return state.isWaiting();
    }

    /**
     * @return whether this {@link AutoCrafterUnit} is in {@link AutoCrafterUnitState} OFF
     */
    public boolean isOff(){
        return state.isOff();
    }

    /**
     * @return whether this {@link AutoCrafterUnit} is in {@link AutoCrafterUnitState} SETUP
     */
    public boolean isInSetup(){
        return state.isInSetup();
    }

    /**
     * sets the {@link AutoCrafterUnitState} to ACTIVE
     * @param business the required Actions left before returning to WAITING
     */
    public void setActive(int business){
        state.setActive(business);
    }

    /**
     * @return the required Actions left before returning to WAITING
     */
    public int getActive(){
        return state.getActive();
    }

    /**
     * lowers required Actions left before returning to WAITING by 1
     */
    public void decreaseState(){
        if(!state.isActive()) throw new IllegalStateException("State cannot be decreased if not Active!");
        if(state.getActive()-1 > 0){
            state.setActive(state.getActive()-1);
        } else if(state.getActive()-1 == 0){
            state.setWaiting();
        }
    }

    /**
     * sets the {@link AutoCrafterUnitState} to WAITING
     */
    public void setWaiting(){
        state.setWaiting();
    }

    /**
     * sets the {@link AutoCrafterUnitState} to OFF
     */
    public void setOff(){
        state.setOff();
    }

    /**
     * resets the {@link AutoCrafterUnit} to {@link AutoCrafterUnitState} SETUP
     */
    public void resetSetup(){
        state.resetSetup();
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

    public String stateToString(){
        return state.toString();
    }

    /**
     * Describes the State a {@link AutoCrafterUnit} can be in
     * SETUP (-2) if the {@link AutoCrafterUnit} is placed on a location, but the targetItem is still null
     * WAITING (0) if the {@link AutoCrafterUnit} is configured, but it's not powered or doesn't have all required ingredients to craft the targetItem
     * ACTIVE (>0) if the {@link AutoCrafterUnit} is currently outputting the targetItem
     * OFF (-1) if the {@link AutoCrafterUnit} was disabled by the User
     */
    private static class AutoCrafterUnitState {

        private int state = -2;

        public AutoCrafterUnitState() { /* defaults alr set */ }

        public boolean isInSetup(){ return state == -2; }
        public boolean isOff(){ return state == -1; }
        public boolean isWaiting(){ return state == 0; }
        public boolean isActive(){ return state > 0; }
        public void resetSetup(){ this.state = -2; }
        public void setOff(){ this.state = -1; }
        public void setWaiting(){ this.state = 0; }

        public void setActive(int amount){
            if(amount < 1) throw new IllegalArgumentException("StateSemaphore is not active for < 1!");
            this.state = amount;
        }

        public int getActive() {
            if(state < 1) throw new IllegalArgumentException("StateSemaphore is not active for < 1! (currently is " + state + ")");
            return state;
        }

        @Override
        public String toString(){
            return String.valueOf(state);
        }

        public static AutoCrafterUnitState fromString(String state){
            AutoCrafterUnitState instance = new AutoCrafterUnitState();
            Integer value = null;
            try {
                value = Integer.parseInt(state);
            } catch (NumberFormatException e) { /* handled later */ }

            if(value == null || value < -2)
                throw new IllegalArgumentException("String representing the State must be Integer and >=-2");

            instance.state = value;
            return instance;
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
                dataOutput.writeObject(autoCrafterUnit.stateToString());
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

                return new AutoCrafterUnit(state, location, itemStack);
            } catch (Exception ignored) {
                throw new IllegalStateException("DeSerialization failed!");
            }
        }
    }
}
