package com.c78.autocrafter.runtime;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class Runtime {

    private static final DebugLevel debug = DebugLevel.ALL;
    private static final Logger log = Logger.getLogger("AutoCrafter");

    public static Server server;
    public static JavaPlugin plugin;

    public static void setup(Server serverInstance, JavaPlugin pluginInstance){
        server = serverInstance;
        plugin = pluginInstance;
    }

    public static BukkitScheduler getScheduler(){
        return server.getScheduler();
    }

    public static void addEventListener(Listener listener){
        server.getPluginManager().registerEvents(listener, plugin);
    }

    public static void addCommandHandler(String command, TabExecutor handler){
        Objects.requireNonNull(plugin.getCommand(command)).setExecutor(handler);
    }

    public static void addRecipe(Recipe recipe){
        server.addRecipe(recipe);
    }


    public static List<CraftingRecipe> getCraftingRecipesOf(ItemStack itemStack){
        List<Recipe> recipes = Bukkit.getRecipesFor(itemStack);
        List<CraftingRecipe> craftingRecipes = new ArrayList<>();
        for(Recipe recipe : recipes)
            if(recipe instanceof CraftingRecipe craftingRecipe)
                craftingRecipes.add(craftingRecipe);
        return craftingRecipes;
    }


    public static void info(String msg){
        if(debug.info())
            log.info(msg);
    }

    public static void warn(String msg){
        if(debug.warn())
            log.warning(msg);
    }
    public static void error(String msg){
        if(debug.error())
            log.severe(msg);
    }
    public static void fatal(String msg){
        if(debug.fatal()) {
            log.severe(msg);
            assert false;
        }
    }

    private enum DebugLevel{
        ALL(4), WARN(3), ERROR(2), FATAL(1), FORCE_OFF(0);
        private final int level;
        DebugLevel(int level){
            this.level = level;
        }
        public boolean info(){ return level > 3; }
        public boolean warn(){ return level > 2; }
        public boolean error(){ return level > 1; }
        public boolean fatal(){ return level > 0; }
    }
}
