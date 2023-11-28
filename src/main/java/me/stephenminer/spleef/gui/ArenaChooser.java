package me.stephenminer.spleef.gui;

import me.stephenminer.spleef.Spleef;
import me.stephenminer.spleef.region.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ArenaChooser {

    public static HashMap<UUID, ArenaChooser> choosers = new HashMap<>();
    private final Spleef plugin;
    private Inventory inv;


    public ArenaChooser(Player player){
        this.plugin = JavaPlugin.getPlugin(Spleef.class);
        inv = Bukkit.createInventory(null, 54, ChatColor.AQUA + "Arena Selector");
        init();
        player.openInventory(inv);
        update();
        ArenaChooser.choosers.put(player.getUniqueId(),this);
    }

    private void init(){
        ItemStack filler = filler();
        for (int i = 45; i < 54; i++){
            inv.setItem(i, filler);
        }
    }




    private void update(){
        new BukkitRunnable(){
            @Override
            public void run(){
                if (inv.getViewers().isEmpty()) {
                    this.cancel();
                    return;
                }else{
                    populate();
                }
            }
        }.runTaskTimer(plugin,0,100);
    }

    private void populate(){
        Set<String> section = plugin.arenaFile.getConfig().getConfigurationSection("arenas").getKeys(false);
        for (String id : section){
            Arena arena = Arena.BY_IDS.getOrDefault(id,null);
            inv.addItem(arenaIcon(id,arena));
        }
    }


    private ItemStack arenaIcon(String arenaId, Arena arena){
        String playerCount;
        if (arena == null) playerCount = ChatColor.GOLD +  "0 players";
        else if (arena.isStarted()) playerCount = ChatColor.RED + "Game Has Started";
        else playerCount = ChatColor.GOLD + "" + arena.getPlayers().size() + " players";

        ItemStack item = new ItemStack(arena != null ? arena.getIcon() : Material.OAK_SIGN);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + arenaId);
        List<String> lore = new ArrayList<>();
        lore.add(playerCount);
        lore.add(ChatColor.YELLOW + "Click me to join!");
        if (arena != null){
            if (arena.getLobby() == null) lore.add(ChatColor.RED + "Warning Lobby spawn missing!");
            if (arena.getSpawn() == null) lore.add(ChatColor.RED + "Warning Spawn location missing!");
            if (arena.getKillZone() == null) lore.add(ChatColor.RED + "Warning KillZone is missing!");
            if (arena.getLayers().isEmpty()) lore.add(ChatColor.RED + "Warning Arena has no snow layer!");
        }
        lore.add(ChatColor.BLACK + "arena:"+arenaId);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack filler(){
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack info(){
        ItemStack item = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Click on an arena icon to join");
        item.setItemMeta(meta);
        return item;
    }



    public void handleInteract(InventoryClickEvent event){
        if(inv.equals(event.getView().getTopInventory()) && event.getView().getTitle().equalsIgnoreCase(ChatColor.AQUA + "Arena Selector")){
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return;
            String arenaId = item.getItemMeta().getLore().stream().filter(l->l.contains("arena:")).findAny().orElse(null);
            if (arenaId == null) return;
            arenaId = arenaId.replace(ChatColor.BLACK + "arena:","");
            Bukkit.dispatchCommand(event.getWhoClicked(),"spleef join " + arenaId);
        }
    }


    public Inventory getInv(){ return inv; }
}
