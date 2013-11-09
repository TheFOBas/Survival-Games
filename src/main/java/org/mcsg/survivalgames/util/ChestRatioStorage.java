package org.mcsg.survivalgames.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.mcsg.survivalgames.SettingsManager;
import org.mcsg.survivalgames.SurvivalGames;

import static org.mcsg.survivalgames.SurvivalGames.$;


public class ChestRatioStorage {

	HashMap<Integer,  ArrayList<ItemStack>>lvlstore = new HashMap<Integer, ArrayList<ItemStack>>();
	public static ChestRatioStorage instance = new ChestRatioStorage();
	private int ratio = 2;
	private int maxlevel = 0;

	private ChestRatioStorage(){ }

	public static ChestRatioStorage getInstance(){
        return instance;
    }

	public void setup(){
		FileConfiguration conf = SettingsManager.getInstance().getChest();

		for(int clevel = 1; clevel <= 16; clevel++){
			ArrayList<ItemStack> lvl = new ArrayList<ItemStack>();
			List<String>list = conf.getStringList("chest.lvl" + clevel);

			if(list != null){
				for(int b = 0; b<list.size();b++){
                    //$("Item: " + list.get(b));
					ItemStack i = ItemReader.read(list.get(b));
					lvl.add(i);
                    maxlevel = clevel;
                }
				lvlstore.put(clevel, lvl);
			}
		}
        //$("MaxLevel: " + maxlevel);
		ratio = conf.getInt("chest.ratio", ratio);

	}


	public ArrayList<ItemStack> getItems(int Startlevel){
        //SurvivalGames.$("Start level: " + Startlevel);
        Random r = new Random();
        ArrayList<ItemStack>items = new ArrayList<ItemStack>();
        for(int a = 0; a< r.nextInt(7)+10; a++){
            int level = Startlevel;
            if(r.nextBoolean() == true){
                while(level<maxlevel && r.nextInt(ratio) == 0){
                    level++;
                    //SurvivalGames.$("Level++");
                }
                //SurvivalGames.$("Level: " + level);
                ArrayList<ItemStack>lvl = lvlstore.get(level);
                ItemStack item = lvl.get(r.nextInt(lvl.size()));

                items.add(item);
            }
        }
        return items;
	}

}