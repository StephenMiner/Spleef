package me.stephenminer.spleef.commands;

import me.stephenminer.spleef.Items;
import me.stephenminer.spleef.Spleef;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.List;
import java.util.Set;

public class GiveKillWand implements CommandExecutor, TabCompleter {
    private final Spleef plugin;

    public GiveKillWand(Spleef plugin){
        this.plugin = plugin;
    }



    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size < 1){
            sender.sendMessage(ChatColor.RED + "You need to specify an arena id!");
            return false;
        }
        if (sender instanceof Player player){
            if (!player.hasPermission("spleef.commands.wand")){
                player.sendMessage( ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }

            String id = args[0];
            if (!realId(id)){
                player.sendMessage(ChatColor.RED + id + " id does not exist!");
                return false;
            }
            Items items = new Items();
            player.getInventory().addItem(items.killZoneWand(id));
            return true;
        }
        return false;
    }

    private boolean realId(String id){
        return plugin.arenaFile.getConfig().contains("arenas." + id);
    }



    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return arenaIds(args[0]);
        return null;
    }


    private List<String> arenaIds(String match){
        Set<String> section = plugin.arenaFile.getConfig().getConfigurationSection("arenas").getKeys(false);
        return plugin.filter(section, match);
    }
}
