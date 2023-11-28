package me.stephenminer.spleef.commands;

import me.stephenminer.spleef.Spleef;
import me.stephenminer.spleef.gui.ArenaChooser;
import me.stephenminer.spleef.region.Arena;
import me.stephenminer.spleef.region.ArenaBuilder;
import me.stephenminer.spleef.region.GameLayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SpleefCmd implements CommandExecutor, TabCompleter {
    private final Spleef plugin;

    public SpleefCmd(Spleef plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player player){
            int size = args.length;
            if (size >= 1){
                String sub = args[0].toLowerCase();
                //MinPlayers CMD
                if (sub.equals("setminplayers")){
                    if (size >= 2){
                        try {
                            int num = Integer.parseInt(args[1]);
                            plugin.settings.reloadConfig();
                            plugin.settings.getConfig().set("settings.min-to-start",num);
                            plugin.settings.saveConfig();
                            player.sendMessage(ChatColor.GREEN + "Set min players to start to " + num);
                        }catch (Exception e){
                            player.sendMessage(ChatColor.RED + "You need to input a number!");
                        }
                    }
                }
                //StartDelay CMD
                if (sub.equals("setstartdelay")){
                    if (size >= 2){
                        try {
                            int num = Integer.parseInt(args[1]);
                            plugin.settings.reloadConfig();
                            plugin.settings.getConfig().set("settings.start-delay",num);
                            plugin.settings.saveConfig();
                            player.sendMessage(ChatColor.GREEN + "Set start delay to" + num);
                            return true;
                        }catch (Exception e){
                            player.sendMessage(ChatColor.RED + "You need to input a number!");
                        }
                    }
                }
                //Join CMD
                if (sub.equals("join")){
                    if (size >= 2){
                        if (!validArenaId(args[1])){
                            player.sendMessage(ChatColor.RED + args[1] + " is not a valid arena to join!");
                            return false;
                        }else {
                            join(player,args[1]);
                            return true;
                        }
                    }else{
                        join(player);
                        return true;
                    }
                }
                //Leave CMD
                if (sub.equals("leave")){
                    Arena arena = arenaIn(player);
                    arena.removePlayer(player);
                    return true;
                }
                //SetSpawn
                if (sub.equals("setspawn")){
                    if (size >= 2){
                        if (!validArenaId(args[1])){
                            player.sendMessage(ChatColor.RED + args[1] + " is not a valid arena!");
                            return false;
                        }else {
                            setSpawn(args[1], player.getLocation() );
                            player.sendMessage(ChatColor.GREEN + "Set the spawn for this arena");
                            return true;
                        }
                    }else player.sendMessage(ChatColor.RED + "You need to specify an arena id!");
                }
                //SetLobby
                if (sub.equals("setlobby")){
                    if (size >= 2){
                        if (!validArenaId(args[1])){
                            player.sendMessage(ChatColor.RED + args[1] + " is not a valid arena!");
                            return false;
                        }else {
                            setLobby(args[1], player.getLocation());
                            player.sendMessage(ChatColor.GREEN + "Set the lobby for this arena");
                            return true;
                        }
                    }else player.sendMessage(ChatColor.RED + "You need to specify an arena id!");
                }
                //SetGracePeriod
                if (sub.equals("setgraceperiod")){
                    if (size >= 3){
                        if (!validArenaId(args[1])){
                            player.sendMessage(ChatColor.RED + args[1] + " is not a valid arena!");
                            return false;
                        }
                        try{
                            int seconds = Integer.parseInt(args[2]);
                            setGracePeriod(args[1],seconds);
                            player.sendMessage(ChatColor.GREEN + "Set grace period for arena to " + seconds + " seconds");
                            return true;
                        }catch (Exception e){
                            player.sendMessage(ChatColor.RED + args[2] + " is not a number");
                        }
                    }else player.sendMessage(ChatColor.RED + "Not enough arguments! You need to input arena Id and a number!");
                }

            }else  sender.sendMessage(ChatColor.RED + "You need more arguments! See the tab completer!");
        }else sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
        return false;
    }


    private void join(Player player){
        ArenaChooser chooser = new ArenaChooser(player);
    }
    private void join(Player player,String id){
        if (Arena.BY_IDS.containsKey(id)) Arena.BY_IDS.get(id).addPlayer(player);
        else{
            ArenaBuilder builder = new ArenaBuilder(id);
            Arena arena = builder.build();
            boolean joined = arena.addPlayer(player);
            if (!joined){
                player.sendMessage(ChatColor.RED + "There is currently a game going on for this arena, or the game is ending.");
            }
        }
    }

    private boolean validArenaId(String arenaId){
        return arenaIds(arenaId).contains(arenaId);
    }

    private Arena arenaIn(Player player){
        return Arena.arenas.stream().filter(arena -> arena.hasPlayer(player)).findFirst().orElse(null);
    }

    private void setSpawn(String id, Location loc){
        Arena active = Arena.BY_IDS.getOrDefault(id,new ArenaBuilder(id).build(false));
        if (active != null) {
            active.setSpawn(loc);
            active.cull();
            active.save();
        }
    }
    private void setLobby(String id, Location loc){
        Arena active = Arena.BY_IDS.getOrDefault(id,new ArenaBuilder(id).build(false));
        if (active != null) {
            active.setLobby(loc);
            active.cull();
            active.save();
        }
    }
    private void setGracePeriod(String id, int seconds){
        Arena active = Arena.BY_IDS.getOrDefault(id, new ArenaBuilder(id).build(false));
        if (active != null){
            active.setGracePeriod(seconds);
            active.cull();
            active.save();
        }
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return subCmds(args[0], sender);
        if (size == 2) return arenaIds(args[1]);
        return null;
    }



    public List<String> arenaIds(String match){
        Set<String> entries = plugin.arenaFile.getConfig().getConfigurationSection("arenas.").getKeys(false);
        return plugin.filter(entries,match);
    }

    public List<String> subCmds(String match, CommandSender sender){
        List<String> subs = new ArrayList<>();
        subs.add("join");
        subs.add("leave");
        if (sender.hasPermission("spleef.commands.extended")){
            subs.add("setGracePeriod");
            subs.add("setLobby");
            subs.add("setSpawn");
            subs.add("setMinPlayers");
            subs.add("setStartDelay");
        }
        return plugin.filter(subs,match);
    }

    public List<String> integer(){
        List<String> out = new ArrayList<>();
        out.add("[time-in-ticks]");
        return out;
    }





    public List<String> getLayers(String arena, String match){
        String path = "arenas." + arena + ".layers";
        Set<String> entries = plugin.arenaFile.getConfig().getConfigurationSection(path).getKeys(false);
        return plugin.filter(entries,match);
    }

    private boolean removeLayer(String arena, String layerId){
        String path = "arenas." + arena + ".layers." + layerId;
        if (plugin.arenaFile.getConfig().contains(path)){
            plugin.arenaFile.getConfig().set(path,null);
            plugin.arenaFile.saveConfig();
            return true;
        }else return false;
    }


}
