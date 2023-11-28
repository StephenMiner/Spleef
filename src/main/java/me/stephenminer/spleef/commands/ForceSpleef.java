package me.stephenminer.spleef.commands;

import me.stephenminer.spleef.region.Arena;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceSpleef implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player player){
            if (!player.hasPermission("spleef.commands.forcestart")){
                player.sendMessage(ChatColor.RED + "Lacking permission");
                return false;
            }
            Arena arenaIn = arenaIn(player);
            if (arenaIn == null){
                player.sendMessage(ChatColor.RED + "You need to be in an arena to use this command!");
                return false;
            }
            arenaIn.start();
            return true;
        }sender.sendMessage(ChatColor.RED + "Need to be a player to use this!");
        return false;
    }

    private Arena arenaIn(Player player){
        return Arena.arenas.stream().filter(arena->arena.hasPlayer(player)).findFirst().orElse(null);
    }



}
