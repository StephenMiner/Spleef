package me.stephenminer.spleef.region;

import me.stephenminer.spleef.Spleef;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class ArenaBuilder {
    private final Spleef plugin;
    private final String id;
    private Location loc1,loc2,lobby,spawn;
    private int gracePeriod;

    public ArenaBuilder(String id){
        this.plugin = JavaPlugin.getPlugin(Spleef.class);
        this.id = id;
    }




    private void loadData(){
        String root = "arenas." + id;

        loc1 = plugin.fromString(plugin.arenaFile.getConfig().getString(root + ".loc1"));
        loc2 = plugin.fromString(plugin.arenaFile.getConfig().getString(root + ".loc2"));
        if (plugin.arenaFile.getConfig().contains(root + ".lobby"))
            lobby = plugin.fromString(plugin.arenaFile.getConfig().getString("arenas." + id + ".lobby" ));
        if (plugin.arenaFile.getConfig().contains(root + ".spawn"))
            spawn = plugin.fromString(plugin.arenaFile.getConfig().getString("arenas." + id + ".spawn"));
        gracePeriod = plugin.arenaFile.getConfig().getInt("arenas." + id + ".grace-period");

    }


    private void addLayers(Arena arena){
        if (plugin.arenaFile.getConfig().contains("arenas." + id + ".layers")) {
            Set<String> layerIds = plugin.arenaFile.getConfig().getConfigurationSection("arenas." + id + ".layers").getKeys(false);
            for (String layerId : layerIds) {
                String str = plugin.arenaFile.getConfig().getString("arenas." + id + ".layers." + layerId);
                GameLayer layer = GameLayer.fromString(str);
                arena.addLayer(layer);
            }
        }
    }
    private void addKillZone(Arena arena){
        if (!plugin.arenaFile.getConfig().contains("arenas." + id + ".kill-zone")) return;
        String str = plugin.arenaFile.getConfig().getString("arenas." + id + ".kill-zone");
        KillZone killZone = KillZone.fromString(str);
        arena.setKillZone(killZone);
    }
    private void setIcon(Arena arena){
        Material mat = Material.matchMaterial(plugin.arenaFile.getConfig().getString("arenas." + id + ".icon"));
        arena.setIcon(mat);
    }

    public Arena build(){
        return build(true);
    }

    /**
     *
     * @param add defines whether arena should be added to static data structures or not
     * @return Arena object based on data from id
     */
    public Arena build(boolean add){
        loadData();
        Arena arena = new Arena(id,loc1,loc2);
        arena.setLobby(lobby);
        arena.setSpawn(spawn);
        arena.setGracePeriod(gracePeriod);
        addLayers(arena);
        addKillZone(arena);
        setIcon(arena);
        if (add) {
            Arena.arenas.add(arena);
            Arena.BY_IDS.put(id, arena);
        }
        return arena;
    }

}
