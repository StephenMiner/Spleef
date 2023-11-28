package me.stephenminer.spleef.events;

import me.stephenminer.spleef.Spleef;
import me.stephenminer.spleef.region.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ArenaSetup implements Listener {
    private HashMap<UUID, Location> loc1s,loc2s;
    private Set<UUID> canName;
    private final Spleef plugin;
    public ArenaSetup(){
        this.plugin = JavaPlugin.getPlugin(Spleef.class);
        loc1s = new HashMap<>();
        loc2s = new HashMap<>();
        canName = new HashSet<>();
    }



    @EventHandler
    public void setPositions(PlayerInteractEvent event){
        if (!event.hasItem()) return;
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        if (plugin.checkLore(item,"spleefwand")){
            UUID uuid = player.getUniqueId();
            switch (event.getAction()){
                case LEFT_CLICK_AIR -> {
                    player.sendMessage(ChatColor.GOLD + "Set positon 1");
                    loc1s.put(uuid, player.getLocation());
                }
                case LEFT_CLICK_BLOCK -> {
                    player.sendMessage(ChatColor.GOLD + "Set position 1");
                    loc1s.put(uuid, event.getClickedBlock().getLocation());
                }
                case RIGHT_CLICK_AIR -> {
                    player.sendMessage(ChatColor.GOLD + "Set position 2");
                    loc2s.put(uuid,player.getLocation());
                }
                case RIGHT_CLICK_BLOCK -> {
                    player.sendMessage(ChatColor.GOLD + "Set position 2");
                    loc2s.put(uuid, event.getClickedBlock().getLocation());
                }
            }
            event.setCancelled(true);
            if (loc1s.containsKey(uuid) && loc2s.containsKey(uuid)){
                player.sendMessage(ChatColor.GOLD + "Please type out the name of your arena in chat (No SPACES Please!)");
                canName.add(uuid);
            }
        }
    }

    @EventHandler
    public void nameRegion(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        if (!canName.contains(player.getUniqueId())) return;
        UUID uuid = player.getUniqueId();
        String id = event.getMessage().replace(' ','_');
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
            Location loc1 = loc1s.get(uuid);
            Location loc2 = loc2s.get(uuid);
            Arena arena = new Arena(id,loc1,loc2);
            arena.save();
            loc1s.remove(uuid);
            loc2s.remove(uuid);
            canName.remove(uuid);
        },1);
    }
}
