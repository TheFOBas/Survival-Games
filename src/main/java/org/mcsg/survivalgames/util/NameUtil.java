package org.mcsg.survivalgames.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class NameUtil {
	public static String stylize(String name, boolean s, boolean r){
        /*if((Bukkit.getPlayer(name)).hasPermission("sg.vip")){
            name = ChatColor.GOLD+name;
        } else if ((Bukkit.getPlayer(name)).hasPermission("sg.arena.start")){
			name = ChatColor.DARK_BLUE+name;
		}*/
		return name;
	}
}
