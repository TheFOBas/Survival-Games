package org.mcsg.survivalgames.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.mcsg.survivalgames.SettingsManager;

/**
 * Created with IntelliJ IDEA.
 * User: Nerijus
 * Date: 13.11.17
 * Time: 13.49
 * To change this template use File | Settings | File Templates.
 */
public class RespawnEvent implements Listener {
     @EventHandler(priority = EventPriority.HIGHEST)
     public void respawn(PlayerRespawnEvent ev){
           ev.setRespawnLocation(SettingsManager.getInstance().getLobbySpawn());
     }
}
