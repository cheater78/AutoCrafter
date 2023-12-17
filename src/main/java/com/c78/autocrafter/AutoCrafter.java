package com.c78.autocrafter;

import com.c78.autocrafter.data.item.AutoCrafterItem;
import com.c78.autocrafter.events.*;
import com.c78.autocrafter.runtime.Runtime;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public class AutoCrafter extends JavaPlugin {

    /**
     * Called by <? implements Bukkit> at Plugin start up
     */
    @Override
    public void onEnable() {
        Runtime.setup(Bukkit.getServer(), this);
        Runtime.addRecipe(AutoCrafterItem.getRecipe());
        Runtime.addEventListener(new AutoCrafterUnitCreateHandler());
        Runtime.addEventListener(new AutoCrafterUnitRemoveHandler());
        Runtime.addEventListener(new AutoCrafterUnitRuntimeHandler());
        Runtime.addEventListener(new AutoCrafterUnitSetupHandler());
    }

    /**
     * Called by <? implements Bukkit> at Plugin shutdown
     */
    @Override
    public void onDisable() {
        /* All Data gets Stored at Runtime */
    }
}
