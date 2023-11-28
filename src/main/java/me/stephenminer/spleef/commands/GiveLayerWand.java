package me.stephenminer.spleef.commands;

import me.stephenminer.spleef.Items;
import me.stephenminer.spleef.Spleef;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Set;

public class GiveLayerWand implements CommandExecutor, TabCompleter {
    private final Spleef plugin;
    public GiveLayerWand(){
        this.plugin = JavaPlugin.getPlugin(Spleef.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player player){
            if (!player.hasPermission("spleef.commands.wand")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
            if (args.length < 1){
                player.sendMessage(ChatColor.RED + "You need to specify an arena id for this wand");
                return false;
            }
            String id = args[0];
            if (!validId(id)){
                player.sendMessage(ChatColor.RED + id + " is not a real arena id!");
                return false;
            }
            Items items = new Items();
            player.getInventory().addItem(items.layerWand(id));
            player.sendMessage(ChatColor.GREEN + "You have been given your wand");
            return true;
        }
        return false;
    }




    private boolean validId(String id){
        id = ChatColor.stripColor(id);
        return plugin.arenaFile.getConfig().contains("arenas." + id);
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        if (args.length == 1) return arenaIds(args[0]);
        return null;
    }


    private List<String> arenaIds(String match){
        if (plugin.arenaFile.getConfig().contains("arenas")) {
            Set<String> ids = plugin.arenaFile.getConfig().getConfigurationSection("arenas").getKeys(false);
            return plugin.filter(ids, match);
        }else return null;
    }
}
