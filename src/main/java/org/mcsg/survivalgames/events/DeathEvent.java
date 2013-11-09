package org.mcsg.survivalgames.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;

public class DeathEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player)event.getEntity();
        int gameid = GameManager.getInstance().getPlayerGameId(player);
        if (gameid==-1) return;
        if (!GameManager.getInstance().isPlayerActive(player)) return;

        Game game = GameManager.getInstance().getGame(gameid);

        if (game.isProtectionOn()) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = (Player)event.getEntity();
        int gameid = GameManager.getInstance().getPlayerGameId(player);
        if (gameid == -1) return;
        if (!GameManager.getInstance().isPlayerActive(player)) return;

        //Game game = GameManager.getInstance().getGame(gameid);
        GameManager.getInstance().getGame(gameid).playerDeath(event);
    }
}