package me.stephenminer.spleef.region;

import me.stephenminer.spleef.Spleef;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ArenaSaver {
    private final Spleef plugin;
    private final List<BlockState> savedStates;
    private boolean loading;
    private final Arena arena;
    public ArenaSaver(Arena arena){
        this.plugin = JavaPlugin.getPlugin(Spleef.class);
        this.arena = arena;
        this.savedStates = new ArrayList<>();
    }


    public void saveRegion(){
        World world = arena.getLoc1().getWorld();
        for (int x = minX(); x <= maxX(); x++){
            for (int y = minY(); y <= maxY(); y++){
                for (int z = minZ(); z <= minZ(); z++){
                    Block block = world.getBlockAt(x,y,z);
                    savedStates.add(block.getState());
                }
            }
        }
    }

    public void loadRegion(int rate){
        loading = true;
        if (rate <= 1000){
            for (int i = 0; i < Math.min(rate, savedStates.size()); i++){
                BlockState state = savedStates.get(i);
                state.update(true,false);
            }
            loading = false;
        }
        new BukkitRunnable(){
            int index = 0;
            int countTo = Math.min(index + rate,savedStates.size());
            @Override
            public void run(){
                if (index >= savedStates.size()){
                    this.cancel();
                    loading = false;
                    return;
                }
                for (int i = index; i < countTo; i++){
                    BlockState state = savedStates.get(i);
                    state.update(true,false);
                }
                index = countTo;
                countTo = Math.min(savedStates.size(), index + rate);
            }

        }.runTaskTimer(plugin,0,1);
    }




    private int minX(){
        return Math.min(arena.getLoc1().getBlockX(),arena.getLoc2().getBlockX());
    }
    private int minY(){
        return Math.min(arena.getLoc1().getBlockY(),arena.getLoc2().getBlockY());
    }
    private int minZ(){
        return Math.min(arena.getLoc1().getBlockZ(),arena.getLoc2().getBlockZ());
    }
    private int maxX(){
        return Math.max(arena.getLoc1().getBlockX(),arena.getLoc2().getBlockX());
    }
    private int maxY(){
        return Math.max(arena.getLoc1().getBlockY(),arena.getLoc2().getBlockY());
    }
    private int maxZ(){
        return Math.max(arena.getLoc1().getBlockZ(),arena.getLoc2().getBlockZ());
    }

    public boolean isLoading(){ return loading; }
}
