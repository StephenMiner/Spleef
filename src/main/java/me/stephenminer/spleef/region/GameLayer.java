package me.stephenminer.spleef.region;

import me.stephenminer.spleef.Spleef;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class GameLayer {

    private final Spleef plugin;
    private Set<BlockState> blocks;

    private String id;
    private Location loc1,loc2,center;
    private int radius;
    private boolean circle;

    /**
     * This constructor is for making a circular layer
     * @param center
     * @param radius
     */

    public GameLayer(String id, Location center, int radius){
        this.plugin = JavaPlugin.getPlugin(Spleef.class);
        circle = true;
        this.id = id;
        this.center = center.getBlock().getLocation().clone();
        this.radius = radius;
        blocks = new HashSet<>();
    }

    /**
     * This is for making a square layer
     * @param loc1 one corner
     * @param loc2 other corner
     */
    public GameLayer(String id, Location loc1, Location loc2){
        this.plugin = JavaPlugin.getPlugin(Spleef.class);
        this.loc1 = loc1.getBlock().getLocation().clone();
        this.loc2 = loc2.getBlock().getLocation().clone();
        this.id = id;
        blocks = new HashSet<>();
    }


    /**
     * Builds the layer
     */
    public void construir(){
        if (circle){
            drawCircle();
        }else{
            drawSquare();
        }
    }

    /**
     * Destroys the layer
     */
    public void destruir(){
        for (BlockState state : blocks){
            state.update(true);
        }
    }

    /**
     * Gets all the blocks that should make up the circle layer and makes them snow material. Also saves blocks to collection.
     */
    private void drawCircle(){
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int radiusSqr = radius*radius;
        World world = center.getWorld();
        for (int x = cx - radius; x <= cx + radius; x++){
            for (int z = cz - radius; z <= cz + radius; z++){
                double dist = (x - cx) * (x - cx) + (z - cz) * (z - cz);

                if (dist <= radiusSqr) {
                    Block block = world.getBlockAt(x,cy,z);
                    if (block.getType().isAir()) {
                        blocks.add(block.getState());
                        BlockState state = block.getState();
                        state.setType(Material.SNOW_BLOCK);
                        state.update(true);
                    }
                }
            }
        }
    }

    /**
     * Gets all the blocks that should make up square layer and makes them snow material. Also saves blocks to a collection.
     */
    private void drawSquare(){

        int minX = Math.min(loc1.getBlockX(),loc2.getBlockX());
        int minZ = Math.min(loc1.getBlockZ(),loc2.getBlockZ());
        int y = loc1.getBlockY();
        int maxX = Math.max(loc1.getBlockX(),loc2.getBlockX());
        int maxZ = Math.max(loc1.getBlockZ(),loc2.getBlockZ());
        World world = loc1.getWorld();
        for (int x = minX; x <= maxX; x++){
            for (int z = minZ; z <= maxZ; z++){
                Block block = world.getBlockAt(x,y,z);
                if (block.getType().isAir()){
                    blocks.add(block.getState());
                    BlockState state = block.getState();
                    state.setType(Material.SNOW_BLOCK);
                    state.update(true);
                }
            }
        }
    }


    public String getId(){ return id; }
    public boolean isCircle(){ return circle; }
    public Location getCenter(){ return center; }
    public Location getLoc1(){ return loc1; }
    public Location getLoc2(){ return loc2; }


    @Override
    public String toString(){
        String base =  id + "/" + circle + "/";
        if (circle){
            base += plugin.fromBLoc(center) + "/" + radius;
        }else base += plugin.fromBLoc(loc1) + "/" + plugin.fromBLoc(loc2);
        return base;
    }

    public static GameLayer fromString(String str){
        Spleef plugin = JavaPlugin.getPlugin(Spleef.class);
        String[] split = str.split("/");
        String id = split[0];
        boolean circle = Boolean.parseBoolean(split[1]);
        if (circle){
            Location center = plugin.fromString(split[2]);
            int radius = Integer.parseInt(split[3]);
            return new GameLayer(id, center, radius);
        }else{
            Location loc1 = plugin.fromString(split[2]);
            Location loc2 = plugin.fromString(split[3]);
            return new GameLayer(id, loc1,loc2);
        }
    }
}
