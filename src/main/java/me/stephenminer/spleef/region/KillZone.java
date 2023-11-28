package me.stephenminer.spleef.region;

import me.stephenminer.spleef.Spleef;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class KillZone {
    private final Spleef plugin;
    private Location loc1,loc2;
    private Material mat;
    private Set<BlockState> blocks;

    private boolean active;
    public KillZone(Location loc1, Location loc2){
        this(loc1,loc2,Material.COBWEB);
    }
    public KillZone(Location loc1, Location loc2,Material mat){
        this.mat = mat;
        this.plugin = JavaPlugin.getPlugin(Spleef.class);
        this.loc1 = loc1.clone().add(0.5,0.5,0.5);
        this.loc2 = loc2.clone().add(0.5,0.5,0.5);
        active = false;
        blocks = new HashSet<>();
    }



    public void activate(){
        active = true;
        construir();
        BoundingBox box = BoundingBox.of(loc1,loc2);
        World world = loc1.getWorld();
        new BukkitRunnable(){
            @Override
            public void run(){
                if (!active){
                    this.cancel();
                    return;
                }
                world.getNearbyEntities(box).forEach(e->{
                    if (e instanceof Player player && !player.isDead() && player.getGameMode() == GameMode.SURVIVAL) player.damage(9999);
                });

            }
        }.runTaskTimer(plugin,1,1);
    }

    public void construir(){
        int minX = Math.min(loc1.getBlockX(),loc2.getBlockX());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        int y = loc1.getBlockY();
        World world = loc1.getWorld();
        for (int x = minX; x <= maxX; x++){
            for (int z = minZ; z <= maxZ; z++){
                Block block = world.getBlockAt(x,y,z);
                if (block.getType().isAir()) {
                    blocks.add(block.getState());
                    BlockState state = block.getState();
                    state.setType(mat);
                    state.update(true,false);
                }
            }
        }
    }

    public void destruir(){
        for (BlockState state : blocks){
            state.update(true);
        }
    }

    public void cancel(){
        active = false;
        destruir();
    }


    /**
     *
     * @return String formatted as "Location/Location"
     */
    @Override
    public String toString(){
        return plugin.fromBLoc(loc1) + "/" + plugin.fromBLoc(loc2);
    }

    /**
     *
     * @param str String formatted as "Location/Location"
     * @return KillZone, generating locations from str.
     */
    public static KillZone fromString(String str){
        Spleef plugin = JavaPlugin.getPlugin(Spleef.class);
        String[] split = str.split("/");
        Location loc1 = plugin.fromString(split[0]);
        Location loc2 = plugin.fromString(split[1]);
        return new KillZone(loc1,loc2);
    }
}
