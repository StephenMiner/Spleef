package me.stephenminer.spleef;

import me.stephenminer.spleef.commands.*;
import me.stephenminer.spleef.events.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Spleef extends JavaPlugin {
    /**
     * Contains all the information about individual arenas (maps)
     */
    public ConfigFile arenaFile;
    public ConfigFile settings;

    public Location reroute;

    @Override
    public void onEnable() {
        this.arenaFile = new ConfigFile(this,"arenas");
        this.settings = new ConfigFile(this,"settings");
        if (this.settings.getConfig().contains("settings.reroute"))
            this.reroute = fromString(this.settings.getConfig().getString("settings.reroute"));
        registerCommand();
        registerEvents();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerCommand(){
        getCommand("spleefwand").setExecutor(new GiveRegionWand());
        getCommand("spleefReroute").setExecutor(new SpleefReroute(this));
        getCommand("forceSpleef").setExecutor(new ForceSpleef());

        SpleefCmd spleefCmd = new SpleefCmd(this);
        getCommand("spleef").setExecutor(spleefCmd);
        getCommand("spleef").setTabCompleter(spleefCmd);

        GiveLayerWand layerWand = new GiveLayerWand();
        getCommand("layerWand").setExecutor(layerWand);
        getCommand("layerWand").setTabCompleter(layerWand);

        GiveKillWand killWand = new GiveKillWand(this);
        getCommand("killWand").setExecutor(killWand);
        getCommand("killWand").setTabCompleter(killWand);
    }

    private void registerEvents(){
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ArenaSetup(),this);
        pm.registerEvents(new JoinGui(),this);
        pm.registerEvents(new SnowLayerSetup(),this);
        pm.registerEvents(new KillZoneSetup(),this);
        pm.registerEvents(new ArenaEvents(),this);
        pm.registerEvents(new SnowballEvents(),this);
    }


    /**
     *
     * @param item item whose lore you want to checl
     * @param check what line the function should check for
     * @return whether the item's lore contains a line 'check' regardless of chatcolor or case
     */
    public boolean checkLore(ItemStack item, String check){
        check = check.toLowerCase();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        List<String> lore = item.getItemMeta().getLore();
        for (String entry : lore){
            String temp = ChatColor.stripColor(entry.toLowerCase());
            if (temp.equalsIgnoreCase(check)) return true;
        }
        return false;
    }

    /**
     *
     * @param str String formatted as "world,x,y,z,yaw,pitch" or "world,x,y,z"
     * @return location from str formatted as "world,x,y,z,yaw,pitch" or "world,x,y,z"
     */
    public Location fromString(String str){
        String[] split = str.split(",");
        String wName = split[0];
        try{
            World world = Bukkit.getWorld(wName);
            double x = Double.parseDouble(split[1]);
            double y = Double.parseDouble(split[2]);
            double z = Double.parseDouble(split[3]);
            float yaw = 0;
            float pitch = 0;
            if (split.length > 5){
                yaw = Float.parseFloat(split[4]);
                pitch = Float.parseFloat(split[5]);
            }
            return new Location( world,x,y,z,yaw,pitch);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param loc the location to get information from
     * @return string formatted as "world,x,y,z" with the block location data of loc (All numbers are integers)
     */
    public String fromBLoc(Location loc){
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    /**
     *
     * @param loc the location to get information from
     * @return string formatted as "world,x,y,z,yaw,pitch" with the exact location data of loc, including rotations.
     */
    public String fromLoc(Location loc){
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    /**
     *
     * @param base Strings to filter
     * @param match the String that all elements from base will be compared against
     * @return List containing only strings from base that contain match.
     */
    public List<String> filter(Collection<String> base, String match){
        match = match.toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String entry : base){
            String temp = ChatColor.stripColor(entry).toLowerCase();
            if (temp.contains(match)) filtered.add(entry);
        }
        return filtered;
    }

    /**
     * @return Minimum # of players needed to start a round
     */
    public int minPlayers(){
        return this.settings.getConfig().getInt("settings.min-to-start");
    }

    /**
     * @return Delay in seconds before a round starts after the minimum player requirement is met
     */
    public int startDelay(){
        return this.settings.getConfig().getInt("settings.start-delay")*20;
    }

}
