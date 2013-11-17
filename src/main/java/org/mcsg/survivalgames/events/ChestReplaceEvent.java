package org.mcsg.survivalgames.events;

import java.util.HashSet;
import java.util.Random;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.Game.GameMode;
import org.mcsg.survivalgames.SurvivalGames;
import org.mcsg.survivalgames.util.ChestRatioStorage;



public class ChestReplaceEvent implements Listener{

	private Random rand = new Random();
	
    @EventHandler(priority = EventPriority.HIGHEST)
    public void ChestListener(PlayerInteractEvent e){
        //SurvivalGames.$("chest");

    	if(e.getAction() != Action.RIGHT_CLICK_BLOCK){
            //SurvivalGames.$("return 1");
            return;
        }
        BlockState clicked = e.getClickedBlock().getState();
        if(!(clicked instanceof Chest ^ clicked instanceof DoubleChest)){
            //SurvivalGames.$("return 2");
            return;
        }
        int gameid = GameManager.getInstance().getPlayerGameId(e.getPlayer());
        if(gameid == -1){
            //SurvivalGames.$("return 3");
            return;
        }
        Game game = GameManager.getInstance().getGame(gameid);
        if(game.getMode() != GameMode.INGAME){
            //SurvivalGames.$("return 4");
            e.setCancelled(true);
            return;
        }

        int level = 1;
        if (clicked.getBlock().getType().getId() == 146) {
            level += 4;
        }

        HashSet<Block>openedChest = GameManager.openedChest.get(gameid);
        openedChest = (openedChest == null)? new HashSet<Block>() : openedChest;
        if(!openedChest.contains(e.getClickedBlock())){
            Inventory[] invs = ((clicked instanceof Chest))? new Inventory[] {((Chest) clicked).getBlockInventory()}
                    : new Inventory[] {((DoubleChest)clicked).getLeftSide().getInventory(), ((DoubleChest)clicked).getRightSide().getInventory()};
            if (rand.nextInt(5) == 0){
                level ++;
            }

            for(Inventory inv : invs){
                inv.setContents(new ItemStack[inv.getContents().length]);
                for(ItemStack i: ChestRatioStorage.getInstance().getItems(level)){
                    int l = rand.nextInt(26);
                    while(inv.getItem(l) != null)
                        l = rand.nextInt(26);
                    inv.setItem(l, i);
                }
            }
        }
        openedChest.add(e.getClickedBlock());
        GameManager.openedChest.put(gameid, openedChest);
    }
}
