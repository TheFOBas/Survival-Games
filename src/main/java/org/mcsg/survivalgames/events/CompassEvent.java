package org.mcsg.survivalgames.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.SurvivalGames;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: Nerijus
 * Date: 13.12.26
 * Time: 14.05
 * To change this template use File | Settings | File Templates.
 */
public class CompassEvent implements Listener {

    HashMap<Player, Player> users = new HashMap<Player,Player>();
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCompassRightClick(PlayerInteractEvent event){
        Player player = event.getPlayer();
        int gameId = GameManager.getInstance().getPlayerGameId(player);
        if (gameId == -1)
            return;
        if (GameManager.getInstance().getGame(gameId).getMode() != Game.GameMode.INGAME && GameManager.getInstance().getGame(gameId).getMode() != Game.GameMode.DEATHMACH) {
            return;
        }
        if(player.getInventory().getItemInHand().getType() == Material.COMPASS){
            List<Player> players = GameManager.getInstance().getGame(gameId).getAllPlayers();
            Player nearest = players.get(0);
            if (player.getDisplayName().equalsIgnoreCase(nearest.getDisplayName())){
                nearest = players.get(1);
            }
            double nearest_distance =  player.getLocation().distanceSquared(nearest.getLocation());
            for (Player p:players){
                if (player.getDisplayName().equalsIgnoreCase(p.getDisplayName())){
                    continue;
                }
                if (GameManager.getInstance().getGame(gameId).isPlayerActive(p)){
                    double distance = player.getLocation().distanceSquared(p.getLocation());
                    if (nearest_distance > distance){
                        nearest = p;
                        nearest_distance = distance;
                    }
                }
            }
            player.setCompassTarget(nearest.getLocation());

            if (users.containsKey(player)){
                Player value = users.get(player);
                if (value != nearest){
                    users.remove(player);
                    player.sendMessage("Radau zaideja: "+nearest.getDisplayName());
                    nearest.sendMessage("Tave seka zaidejas: " + player.getDisplayName());
                    users.put(player, nearest);
                }
            } else {
                player.sendMessage("Radau zaideja: "+nearest.getDisplayName());
                nearest.sendMessage("Tave seka zaidejas: " + player.getDisplayName());
                users.put(player, nearest);
            }
        }
    }

}
