package me.stephenminer.spleef.commands;

import me.stephenminer.spleef.Spleef;
import me.stephenminer.spleef.region.Arena;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpleefReroute implements CommandExecutor {
    private final Spleef plugin;
    public SpleefReroute(Spleef plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player player){
            if (!player.hasPermission("spleef.commands.reroute")){
                player.sendMessage(ChatColor.RED + "No permission!");
                return false;
            }
            setLoc(player.getLocation());
            plugin.reroute = player.getLocation();
            player.sendMessage(ChatColor.GREEN + "Set reroute");
            for (Arena arena : Arena.arenas){
                arena.checkStart();
            }
            return true;
        }
        return false;
    }

    private void setLoc(Location loc){
        plugin.settings.getConfig().set("settings.reroute",plugin.fromLoc(loc));
        plugin.settings.saveConfig();
    }
}
