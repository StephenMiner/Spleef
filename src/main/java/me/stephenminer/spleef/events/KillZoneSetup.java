package me.stephenminer.spleef.events;

import me.stephenminer.spleef.Spleef;
import me.stephenminer.spleef.region.Arena;
import me.stephenminer.spleef.region.ArenaBuilder;
import me.stephenminer.spleef.region.GameLayer;
import me.stephenminer.spleef.region.KillZone;
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
import java.util.List;
import java.util.UUID;

public class KillZoneSetup implements Listener {
    private final Spleef plugin;
    private final HashMap<UUID, Location> loc1s, loc2s;
    private final HashMap<UUID,String> canName;

    public KillZoneSetup(){
        this.plugin = JavaPlugin.getPlugin(Spleef.class);
        this.loc1s = new HashMap<>();
        this.loc2s = new HashMap<>();
        canName = new HashMap<>();
    }


    @EventHandler
    public void setPositions(PlayerInteractEvent event){
        if (!event.hasItem()) return;
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        if (plugin.checkLore(item,"kill-zone-wand")){
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
                player.sendMessage(ChatColor.GOLD + "Please type 'confirm' to confirm killzone creation");
                canName.put(uuid, parseArenaId(item));
            }
        }
    }

    private String parseArenaId(ItemStack item){
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return null;
        List<String> lore = item.getItemMeta().getLore().stream()
                .filter(str->str.contains("arena-id:")).toList();
        for (String str : lore){
            str = str.replace("arena-id:","");
            return str;
        }
        return null;
    }

    @EventHandler
    public void nameLayer(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        if (!canName.containsKey(player.getUniqueId())) return;
        UUID uuid = player.getUniqueId();
        String id = event.getMessage();
        if (id.equalsIgnoreCase("confirm")) {
            String arenaId = ChatColor.stripColor(canName.get(player.getUniqueId()));
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                Location loc1 = loc1s.get(uuid);
                Location loc2 = loc2s.get(uuid);
                KillZone killZone = new KillZone(loc1, loc2);
                killZone.construir();
                Arena arena = new ArenaBuilder(arenaId).build(false);
                arena.setKillZone(killZone);
                arena.save();
                loc1s.remove(uuid);
                loc2s.remove(uuid);
                canName.remove(uuid);
            }, 1);
        }else{
            loc1s.remove(uuid);
            loc2s.remove(uuid);
            canName.remove(uuid);
            player.sendMessage(ChatColor.RED + "Cancelled kill-zone creation");
        }
    }
}
