package me.stephenminer.spleef.commands;

import me.stephenminer.spleef.Items;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveRegionWand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player player){
            Items items = new Items();
            player.getInventory().addItem(items.wand());
            player.sendMessage(ChatColor.GREEN + "Sent your message");
            return true;
        }else sender.sendMessage(ChatColor.RED + "Need to be a player");
        return false;
    }
}
