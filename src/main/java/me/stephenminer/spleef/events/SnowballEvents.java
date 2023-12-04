package me.stephenminer.spleef.events;

import me.stephenminer.spleef.Spleef;
import me.stephenminer.spleef.region.Arena;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SnowballEvents implements Listener {
    private final Spleef plugin;
    public SnowballEvents(){
        this.plugin = JavaPlugin.getPlugin(Spleef.class);
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event){
        if (event.getEntity() instanceof Egg egg){
            String name = effectFromId(egg.getItem());
            if (name != null) {
                egg.setCustomName(effectFromId(egg.getItem()));
                egg.setCustomNameVisible(false);
            }
        }
    }

    @EventHandler
    public void throwPowerball(PlayerInteractEvent event){
        if (!event.hasItem()) return;
        ItemStack item = event.getItem();
        if (plugin.checkLore(item,"powerball")){
            Player player = event.getPlayer();
            if (arenaIn(player) == null) return;
            Snowball snowball = player.launchProjectile(Snowball.class);
            snowball.setItem(new ItemStack(Material.SLIME_BALL));
            item.setAmount(item.getAmount()-1);
        }
    }

    @EventHandler
    public void onLand(ProjectileHitEvent event){
        if (event.getHitEntity() != null && event.getHitEntity() instanceof Player player && (event.getEntity() instanceof Snowball snowball || event.getEntity() instanceof Egg egg)){
            if (arenaIn(player) != null){
                player.damage(1, event.getEntity());
                Location pLoc  = player.getLocation();
                Location eLoc = event.getEntity().getLocation();
                Location dif = pLoc.subtract(eLoc);
                player.setVelocity(dif.toVector().multiply(0.75));
                //player.getVelocity().add(snowball.getLocation().getDirection().normalize().multiply(1.2));
                player.setHealth(Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(),player.getHealth()+0.1));
            }
            if (event.getEntity().getCustomName() != null) {
                PotionEffect effect = parseEffect(event.getEntity().getCustomName());
                if (effect != null) {
                    player.playSound(player, Sound.ENTITY_PLAYER_HURT_FREEZE, 1, 1);
                    player.addPotionEffect(effect);
                }
                return;
            }
            if (event.getEntity() instanceof  Snowball snowball && snowball.getItem().getType() == Material.SLIME_BALL){
                Location pLoc  = player.getLocation();
                Location eLoc = snowball.getLocation();
                Location dif = pLoc.subtract(eLoc);
                double mag = 2.5;
                Vector vDif = dif.toVector();
                player.setVelocity(vDif.setX(vDif.getX()*mag).setZ(vDif.getZ()*mag).setY(vDif.getY()*(mag/4d)));
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
        int roll = random.nextInt(250);
        if (roll < 5) return powerball();
        if (roll < 10) return tippedEgg(new PotionEffect(PotionEffectType.WITHER,40,4));
        if (roll < 20) return tippedEgg(randEffect());
        return normalSnowball();
    }

    private PotionEffect randEffect(){
        PotionEffect[] effects = new PotionEffect[]{
                new PotionEffect(PotionEffectType.SLOW,120,1),
                new PotionEffect(PotionEffectType.DARKNESS,100,60),
                new PotionEffect(PotionEffectType.SLOW_FALLING,80,1)
        };
        return effects[ThreadLocalRandom.current().nextInt(effects.length)];
    }

    private ItemStack tippedEgg(PotionEffect effect){
        ItemStack item = new ItemStack(Material.EGG);
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

    private ItemStack powerball(){
        ItemStack item = new ItemStack(Material.SLIME_BALL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Powerball");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.ITALIC + "Winning the lottery?");
        lore.add(ChatColor.YELLOW + "Right-Click to throw");
        lore.add(ChatColor.BLACK + "powerball");
        meta.setLore(lore);
        meta.addEnchant(Enchantment.LURE,1,true);
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
