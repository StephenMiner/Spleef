package me.stephenminer.spleef.events;

import me.stephenminer.spleef.region.Arena;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SnowballEvents implements Listener {


    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event){
        if (event.getEntity() instanceof Snowball snowball){
            String name = effectFromId(snowball.getItem());
            if (name != null) {
                snowball.setCustomName(effectFromId(snowball.getItem()));
                snowball.setCustomNameVisible(false);
            }
        }
    }

    @EventHandler
    public void onLand(ProjectileHitEvent event){
        if (event.getHitEntity() != null && event.getHitEntity() instanceof Player player && event.getEntity() instanceof Snowball snowball){
            if (arenaIn(player) != null){
                player.damage(0.1,((Player)snowball.getShooter()));
                //player.getVelocity().add(snowball.getLocation().getDirection().normalize().multiply(1.2));
                player.setHealth(Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(),player.getHealth()+0.1));
            }
            if (snowball.getCustomName() == null) return;
            PotionEffect effect = parseEffect(snowball.getCustomName());
            if (effect != null) {
                player.playSound(player, Sound.ENTITY_PLAYER_HURT_FREEZE, 1, 1);
                player.addPotionEffect(effect);
            }

        }
        if (event.getHitBlock() != null && event.getEntity().getShooter() instanceof Player player){
            Block block = event.getHitBlock();
            Arena.arenas.forEach(arena->{
                if (arena.isInArena(block.getLocation()) && arena.canInteract(block.getLocation(),player) && block.getType() == Material.SNOW_BLOCK){
                    block.breakNaturally(null);
                    event.getEntity().remove();
                }
            });
        }
    }

    @EventHandler
    public void breakSnow(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Arena.arenas.forEach(arena->{
            if (arena.isInArena(block.getLocation())){

                if (!arena.canInteract(block.getLocation(),player)) event.setCancelled(true);
                else if (block.getType() == Material.SNOW_BLOCK) {
                    onBreak(player);
                    event.setDropItems(false);
                }
            }
        });
    }


    public void onBreak(Player player){
        player.getInventory().addItem(rollItem());
    }


    public ItemStack rollItem(){
        Random random = new Random();
        int roll = random.nextInt(100);
        if (roll < 5) return tippedSnowball(new PotionEffect(PotionEffectType.WITHER,40,4));
        if (roll < 20) return tippedSnowball(randEffect());
        return normalSnowball();
    }

    private PotionEffect randEffect(){
        PotionEffect[] effects = new PotionEffect[]{
                new PotionEffect(PotionEffectType.SLOW,100,1),
                new PotionEffect(PotionEffectType.DARKNESS,100,1),
                new PotionEffect(PotionEffectType.SLOW_FALLING,60,1)
        };
        return effects[ThreadLocalRandom.current().nextInt(effects.length)];
    }

    private ItemStack tippedSnowball(PotionEffect effect){
        ItemStack item = new ItemStack(Material.SNOWBALL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "" + effect.getType().getName().toLowerCase().replace('_',' '));
        List<String> lore = new ArrayList<>();
        String effectStr = effect.getType().getName() + "," + effect.getDuration() + "," + effect.getAmplifier();
        lore.add(ChatColor.ITALIC + "How luring!");
        lore.add(ChatColor.BLACK + "effect:" + effectStr);
        meta.setLore(lore);
        meta.addEnchant(Enchantment.LURE,999,true);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack normalSnowball(){
        ItemStack item = new ItemStack(Material.SNOWBALL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Perfectly Normal Snowball!");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.ITALIC + "Seriously!");
        lore.add(ChatColor.YELLOW + "Grass Fed, Cage Free, and Non-GMO!!");
        lore.add(ChatColor.YELLOW + "What a steal!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }


    private String effectFromId(ItemStack item){
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return null;
        List<String> lore = item.getItemMeta().getLore();
        for (String entry : lore){
            String temp = ChatColor.stripColor(entry);
            if (temp.contains("effect:")) return temp.replace("effect:","");
        }
        return null;
    }

    private PotionEffect parseEffect(String str){
        try {
            String[] split = str.split(",");
            PotionEffectType type = PotionEffectType.getByName(split[0]);
            int duration = Integer.parseInt(split[1]);
            int amp = Integer.parseInt(split[2]);
            return type.createEffect(duration, amp);
        }catch (Exception e){
            System.out.println("Uh oh Mark");
        }
        return null;
    }
    private Arena arenaIn(Player player){
        return Arena.arenas.stream().filter(arena->arena.hasPlayer(player)).findFirst().orElse(null);
    }

}
