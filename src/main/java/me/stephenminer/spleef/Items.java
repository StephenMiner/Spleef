package me.stephenminer.spleef;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Items {

    /**
     *
     * @return wand item used to create the regions for spleef
     */
    public ItemStack wand(){
        ItemStack item = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Spleef Wand");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Left-Click: Pos1");
        lore.add(ChatColor.YELLOW + "Right-Click: Pos2");
        lore.add(ChatColor.BLACK + "spleefwand");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    public ItemStack layerWand(String arenaId){
        ItemStack item = new ItemStack(Material.DIAMOND_AXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Snow-Layer-Wand");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Left-Click: Pos1");
        lore.add(ChatColor.YELLOW + "Right-Click: Pos2");
        lore.add(ChatColor.ITALIC + "arena-id:" + arenaId);
        lore.add(ChatColor.BLACK + "snow-layer-wand");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack killZoneWand(String arenaId){
        ItemStack item = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Kill-Zone Wand");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Used to define kill-zone area where players should die");
        lore.add(ChatColor.YELLOW + "Left-Clic: Pos1");
        lore.add(ChatColor.YELLOW + "Right-Click: Pos2");
        lore.add(ChatColor.ITALIC + "arena-id:" + arenaId);
        lore.add(ChatColor.BLACK + "kill-zone-wand");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
