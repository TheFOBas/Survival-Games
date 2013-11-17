package org.mcsg.survivalgames.events;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.SettingsManager;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Nerijus
 * Date: 13.11.17
 * Time: 15.54
 * To change this template use File | Settings | File Templates.
 */
public class ArenaMenu implements Listener {

    @EventHandler
    public void itemClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player p = (Player)e.getWhoClicked();
            if(GameManager.getInstance().isInArenaMenu(p)){
                ItemStack item = e.getCurrentItem();
                ItemMeta itemMeta = item.getItemMeta();
                try {
                    String game = itemMeta.getDisplayName().replace("Arena ", "");
                    int gameno  = Integer.parseInt(game);
                    GameManager.getInstance().addPlayer((Player) e.getWhoClicked(), gameno);
                    GameManager.getInstance().leaveArenaMenu(p);
                    p.closeInventory();
                } catch (Exception ex){
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void InvClose(InventoryCloseEvent e){
        GameManager.getInstance().leaveKitMenu((Player) e.getPlayer());
    }


}
