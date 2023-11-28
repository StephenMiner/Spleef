package me.stephenminer.spleef.events;

import me.stephenminer.spleef.Spleef;
import me.stephenminer.spleef.region.Arena;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public class ArenaEvents implements Listener {
    private final Spleef plugin;

    public ArenaEvents(){
        this.plugin = JavaPlugin.getPlugin(Spleef.class);
    }


    @EventHandler
    public void onBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        for (Arena arena : Arena.arenas){
            if (!arena.canInteract(loc,player)){
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        Location loc = event.getBlockPlaced().getLocation();
        for (Arena arena : Arena.arenas){
            if (!arena.canInteract(loc,player)){
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event){
        if (event.getRecipe().getResult().getType() == Material.SNOW_BLOCK){
            if (event.getWhoClicked() instanceof Player player){
                Arena arena = arenaIn(player);
                if (arena != null) {
                    event.setCancelled(true);
                }

            }

        }
    }


    @EventHandler
    public void disablePvp(EntityDamageByEntityEvent event){
        if (event.getEntity() instanceof Player player && event.getDamager() instanceof Player damager){
            Arena arenaIn = arenaIn(player);
            if (arenaIn == null) return;
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        for (Arena arena : Arena.arenas){
            if (arena.hasPlayer(player)) {
                arena.removePlayer(player);
                return;
            }
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event){
        Player player = event.getPlayer();
        for (Arena arena : Arena.arenas){
            if (arena.hasPlayer(player)) {
                arena.removePlayer(player);
                return;
            }
        }
    }

    @EventHandler
    public void handleDeath(EntityDamageEvent event){
        double dmg = event.getFinalDamage();
        if (event.getEntity() instanceof Player player){
            if (player.getHealth() - dmg > 0) return;
            Arena arena = arenaIn(player);
            if (arena != null){
                if (!arena.isStarted()){
                    event.setDamage(0);
                    return;
                }
                event.setCancelled(true);
                arena.broadcastSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);
                arena.broadcastMsg(ChatColor.GOLD + player.getName() + selectDeathMsg());
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(arena.getSpawn());
                player.playSound(player,Sound.ENTITY_CAT_DEATH,1,1);
            }
        }
    }

    private Arena arenaIn(Player player){
        return Arena.arenas.stream().filter(arena->arena.hasPlayer(player)).findFirst().orElse(null);
    }


    private String selectDeathMsg(){
        String[] msgs = new String[]{" fell to their doom", " found gravity", " became Joever"};
        return msgs[ThreadLocalRandom.current().nextInt(msgs.length)];
    }
}
