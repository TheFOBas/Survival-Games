package org.mcsg.survivalgames.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class NameUtil {
	public static String stylize(String name, boolean s, boolean r){
        Player p = Bukkit.getPlayer(name);
        if(p.hasPermission("sg.vip")){
            name = ChatColor.GOLD+name;
        } else if(p.hasPermission("sg.arena.start")){
			name = ChatColor.DARK_BLUE+name;
		}
		return name;
	}
}
