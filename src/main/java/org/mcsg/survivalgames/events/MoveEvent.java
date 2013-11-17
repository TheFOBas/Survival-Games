package org.mcsg.survivalgames.events;


import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.Game.GameMode;
import org.mcsg.survivalgames.SettingsManager;


public class MoveEvent implements Listener{

    @EventHandler(priority = EventPriority.HIGHEST)
    public void outOfBoundsHandler(PlayerMoveEvent e){
        /*  Optimization for single game world. No longer works since support for multiple worlds was added
         * if(SettingsManager.getGameWorld() == null)
            return;
        if(e.getPlayer().getWorld()!=SettingsManager.getGameWorld())
            return;*//*
        if(!GameManager.getInstance().isPlayerActive(e.getPlayer()))
            return;
        int id = GameManager.getInstance().getPlayerGameId(e.getPlayer());
        if(GameManager.getInstance().getGameMode(id) == Game.GameMode.WAITING)
            return;
        if(GameManager.getInstance().getBlockGameId(e.getPlayer().getLocation()) == id)
            return;
        else{

            Location l = e.getPlayer().getLocation();
            Location max = GameManager.getInstance().getGame(id).getArena().getMax();
            Location min = GameManager.getInstance().getGame(id).getArena().getMin();
            if(max.getBlockX() - 1 <= l.getBlockX())
                l.add(-5, 0, 0);
            else if(min.getBlockX() + 1>= l.getBlockX())
                l.add(5,0,0);

            if(max.getBlockZ() - 1<= l.getBlockZ())
                l.add(0,0,-5);
            else if(min.getBlockX() + 1>= l.getBlockZ())
                l.add(0,0,5);
            
            l.setY(l.getBlockY());
            //l.setYaw(e.getPlayer().getLocation().getYaw());
            //l.setPitch(e.getPlayer().getLocation().getPfditch());
            e.getPlayer().teleport(l);
        }*/
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void frozenSpawnHandler(PlayerMoveEvent e) {
        int gameId = GameManager.getInstance().getPlayerGameId(e.getPlayer());
        if(gameId == -1 ){
            return;
        }
        if(GameManager.getInstance().getGame(gameId).getMode() == Game.GameMode.INGAME || GameManager.getInstance().getGame(gameId).getMode() == Game.GameMode.DEATHMACH)
            return;
        GameMode mo3 = GameManager.getInstance().getGameMode(gameId);
        if(GameManager.getInstance().isPlayerActive(e.getPlayer()) && mo3 != Game.GameMode.INGAME && mo3 != Game.GameMode.DEATHMACH){
            Location spawn = SettingsManager.getInstance().getSpawnPoint(gameId, GameManager.getInstance().getGame(gameId).getPlayerSpawn(e.getPlayer()));
            Location l = e.getPlayer().getLocation();
            Vector v = spawn.toVector();
            if(l.getBlockX() != v.getBlockX()  || l.getBlockZ() != v.getBlockZ()){
                l.setX(v.getBlockX() + .5);
                l.setZ(v.getBlockZ() + .5);
                l.setYaw(e.getPlayer().getLocation().getYaw());
                l.setPitch(e.getPlayer().getLocation().getPitch());
                e.getPlayer().teleport(l);
            }
        }
    }
}
