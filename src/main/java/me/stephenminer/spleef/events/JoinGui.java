package me.stephenminer.spleef.events;

import me.stephenminer.spleef.gui.ArenaChooser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class JoinGui implements Listener {



    @EventHandler
    public void onInvClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        if (ArenaChooser.choosers.containsKey(player.getUniqueId())){
            ArenaChooser chooser = ArenaChooser.choosers.get(player.getUniqueId());
            chooser.handleInteract(event);
        }
    }

    @EventHandler
    public void invClose(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        ArenaChooser.choosers.remove(player.getUniqueId());
    }

}
