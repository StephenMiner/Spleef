package me.stephenminer.spleef.region;

import me.stephenminer.spleef.Spleef;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.checkerframework.checker.units.qual.A;

import java.util.*;

public class Arena {
    /**
     * The Arena must be manually added to arenas and BY_IDS data structures!
     */
    public static Set<Arena> arenas = new HashSet<>();
    public static HashMap<String,Arena> BY_IDS = new HashMap<>();
    private final String id;
    private final Spleef plugin;
    private final Location loc1,loc2;
    private ArenaSaver saver;
    private Location spawn,lobby;
    private int gracePeriod;
    private List<Player> players;
    private HashMap<UUID,OfflineEntry> offlinePlayers;

    private List<GameLayer> layers;
    private KillZone killZone;
    private boolean started,starting, grace, ending;

    private Material icon;

    public Arena(String id, Location loc1, Location loc2){
        this.id = id;
        this.plugin = JavaPlugin.getPlugin(Spleef.class);
        /*
        0.5 is added in order to center the locations.
        Otherwise, a bounding box may not include some edges of the region.
         */
        this.loc1 = loc1.clone().add(0.5,0.5,0.5);
        this.loc2 = loc2.clone().add(0.5,0.5,0.5);

        players = new ArrayList<>();
        offlinePlayers = new HashMap<>();
        layers = new ArrayList<>();
        this.icon = Material.OAK_SIGN;
        gracePeriod = 2*20;
        saver = new ArenaSaver(this);
        saver.saveRegion();

    }


    public boolean addPlayer(Player player){
        if (started) return false;
        player.teleport(lobby);
        players.add(player);
        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        broadcastMsg(ChatColor.GOLD + player.getName() + " has joined!" +  " (" + players.size()  + "/" + plugin.minPlayers() + " needed to start)");
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(1);
        removePotEffect(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION,999999,9));
        checkStart();
        return true;
    }

    private void removePotEffect(Player player){
        List<PotionEffectType> types = player.getActivePotionEffects().stream()
                .map(PotionEffect::getType).toList();
        for (PotionEffectType type : types) {
            player.removePotionEffect(type);
        }
    }
    public void removePlayer(OfflinePlayer offline){
        if (offline.isOnline()){
            Player player = offline.getPlayer();
            player.getInventory().clear();
            players.remove(player);
            player.teleport(plugin.reroute);
            player.setGameMode(GameMode.SURVIVAL);
            removePotEffect(player);
        }
        String msg = ChatColor.GOLD + offline.getName() + " has left the game";
        if (!started) msg += " (" + players.size() + "/" + plugin.minPlayers() + " needed to start)";
        broadcastMsg(msg);
    }
    public void checkStart(){
        if (starting || started) return;
        if (players.size() < plugin.minPlayers()) return;
        starting = true;
        broadcastMsg(ChatColor.GOLD + "Game starting in " + plugin.startDelay() + " seconds");
        new BukkitRunnable(){
            private int count;
            @Override
            public void run(){
                if (players.size() < plugin.minPlayers()){
                    this.cancel();
                    broadcastMsg(ChatColor.RED + "Start cancelled due to lack of players");
                    broadcastSound(Sound.ENTITY_CAT_PURREOW,2,1);
                    starting = false;
                    return;
                }
                if (saver.isLoading()) return;
                if ((plugin.startDelay() - count) / 20 <= 5 && (plugin.startDelay()-count) % 20 == 0){
                    broadcastTitle(ChatColor.AQUA + "" + (plugin.startDelay()-count)/20,"",5,20,5);
                    broadcastSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);
                }
                if (count >= plugin.startDelay()){
                    start();

                    this.cancel();
                    return;
                }
                count++;
            }
        }.runTaskTimer(plugin,1,1);
    }

    private void runGracePeriod(){
        new BukkitRunnable(){
            int count = 0;
            @Override
            public void run(){
                if (count >= gracePeriod){
                    grace = false;
                    this.cancel();
                }
                count++;
            }
        }.runTaskTimer(plugin,1,1);
    }




    public void start(){
        started = true;
        broadcastTitle("Game Has Started","",5,30,5);
        layers.forEach(GameLayer::construir);
        players.forEach(p->{
            p.teleport(spawn);
            p.getInventory().addItem(shovel());
        });
        killZone.construir();
        killZone.activate();
        grace = true;
        checkEnd();
        runGracePeriod();
    }

    private ItemStack shovel(){
        ItemStack shovel = new ItemStack(Material.NETHERITE_SHOVEL);
        ItemMeta meta = shovel.getItemMeta();
        meta.addEnchant(Enchantment.DIG_SPEED,4,true);
        shovel.setItemMeta(meta);
        return shovel;
    }

    public void checkEnd(){
        new BukkitRunnable(){
            @Override
            public void run(){
                long alive = getAlive();
                if (alive <= 1){
                    declareWinner();
                    ending = true;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                        end();
                    },3*20);
                    this.cancel();
                    return;
                }
            }
        }.runTaskTimer(plugin,1,1);
    }

    public void end(){
        killZone.cancel();
        layers.forEach(GameLayer::construir);
        for (int i = players.size()-1; i >= 0; i--){
            Player player = players.remove(i);
            removePlayer(player);
        }
        saver.loadRegion(5000);
        cull();
    }



    public boolean canInteract(Location loc, Player player){
        if (!loc1.getWorld().equals(player.getWorld())) return true;
        return !isInArena(loc) || (loc.getBlock().getType() == Material.SNOW_BLOCK && !grace && started && !ending);
    }

    public boolean isInArena(Location loc){
        BoundingBox bounds = BoundingBox.of(loc1,loc2);
        return bounds.overlaps(loc.getBlock().getBoundingBox());
    }




    public void save(){
        String path = "arenas." + id;
        plugin.arenaFile.getConfig().set(path + ".loc1",plugin.fromBLoc(loc1));
        plugin.arenaFile.getConfig().set(path + ".loc2",plugin.fromBLoc(loc2));
        if (spawn != null)
            plugin.arenaFile.getConfig().set(path + ".spawn",plugin.fromLoc(spawn));
        if (lobby != null)
            plugin.arenaFile.getConfig().set(path + ".lobby",plugin.fromLoc(lobby));
        plugin.arenaFile.getConfig().set(path + ".grace-period",gracePeriod/20);
        if (killZone != null)
            plugin.arenaFile.getConfig().set(path + ".kill-zone",killZone.toString());
        plugin.arenaFile.getConfig().set(path + ".icon",icon.toString());
        plugin.arenaFile.saveConfig();
        saveLayers();
    }

    private void saveLayers(){
        String path = "arenas." + id + ".layers";
        plugin.arenaFile.getConfig().set(path,null);
        plugin.arenaFile.saveConfig();
        for (GameLayer layer : layers){
            plugin.arenaFile.getConfig().set(path + "." + layer.getId(),layer.toString());
        }
        plugin.arenaFile.saveConfig();
    }





    public void broadcastMsg(String msg){
        for (Player player : players){
            player.sendMessage(msg);
        }
    }
    public void broadcastTitle(String title, String sub, int fadeIn, int stay, int fadeOut){
        for (Player player : players){
            player.sendTitle(title,sub,fadeIn,stay,fadeOut);
        }
    }
    public void broadcastSound(Sound sound, float vol, float pitch){
        for (Player player : players) {
            player.playSound(player, sound, vol, pitch);
        }
    }

    public long getAlive(){
        return players.stream().filter(p->p.getGameMode()!=GameMode.SPECTATOR).count();
    }
    public void declareWinner(){
        System.out.println(players.size());
        Player player = players.stream().filter(p->p.getGameMode()!=GameMode.SPECTATOR).findFirst().orElse(null);
        broadcastTitle(ChatColor.AQUA + player.getDisplayName() + " Has Won","They get more snow or something...", 20,60,20);
        broadcastSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST,2,1);
    }

    public void cull(){
        for (int i = players.size()-1; i >= 0; i--){
            removePlayer(players.remove(i));
        }

        Arena.arenas.remove(this);
        Arena.BY_IDS.remove(id);
    }


    public void setSpawn(Location spawn){ this.spawn = spawn; }
    public void setLobby(Location lobby){ this.lobby = lobby; }
    public void setGracePeriod(int seconds){ this.gracePeriod = seconds * 20; }
    public void setKillZone(KillZone killZone){ this.killZone = killZone; }

    public void addLayer(GameLayer layer){
        layers.add(layer);
        layer.construir();
    }

    public void setLayers(List<GameLayer> layers){ this.layers = layers; }

    public List<Player> getPlayers(){ return players; }
    public boolean hasPlayer(Player player){ return players.contains(player); }
    public HashMap<UUID, OfflineEntry> getOfflinePlayers(){ return offlinePlayers; }
    public int getGracePeriod(){ return gracePeriod;}
    public String getId(){ return id; }
    public boolean isStarted(){ return started; }

    public Location getLoc1(){ return loc1; }
    public Location getLoc2(){ return loc2; }
    public Location getSpawn(){ return spawn; }
    public Location getLobby(){ return lobby; }

    public Material getIcon(){ return icon; }
    public KillZone getKillZone(){ return killZone;}
    public List<GameLayer> getLayers(){ return layers; }
    public void setIcon(Material icon){ this.icon = icon; }




}
