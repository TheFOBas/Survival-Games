package org.mcsg.survivalgames;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.mcsg.survivalgames.Game.GameMode;
import org.mcsg.survivalgames.MessageManager.PrefixType;
import org.mcsg.survivalgames.stats.StatsManager;
import org.mcsg.survivalgames.util.ArenaInventoryHolder;
import org.mcsg.survivalgames.util.Kit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class GameManager {
	static GameManager instance = new GameManager();
	private ArrayList < Game > games = new ArrayList < Game > ();
	private SurvivalGames p;
	public static HashMap < Integer, HashSet < Block >> openedChest = new HashMap < Integer, HashSet < Block >> ();
	private ArrayList<Kit>kits = new ArrayList<Kit>();
	private HashSet<Player>kitsel = new HashSet<Player>();
	private HashSet<Player>arenaMenu = new HashSet<Player>();
	MessageManager msgmgr = MessageManager.getInstance();

    Inventory arenasInventory;

	private GameManager() {

	}

	public static GameManager getInstance() {
		return instance;
	}

	public void setup(SurvivalGames plugin) {
		p = plugin;
		LoadGames();
		LoadKits();
		for (Game g: getGames()) {
			openedChest.put(g.getID(), new HashSet < Block > ());
		}
        arenasInventory = Bukkit.getServer().createInventory(new ArenaInventoryHolder(), 27);

        updateArenaInventory();

	}



    public void updateArenaInventory(){
        int b = 0;
        for (Game game : games) {
            ItemStack item;
            switch (game.getGameMode()){
                case INGAME:
                    item = new ItemStack(Material.WOOD_SWORD);
                    item.setDurability((short)(item.getData().getItemType().getMaxDurability()-item.getData().getItemType().getMaxDurability()*(game.getActivePlayers()/(game.getInactivePlayers()+game.getActivePlayers()))));
                    break;
                case STARTING:
                    item = new ItemStack(Material.DIAMOND_SWORD);
                    break;
                case DEATHMACH:
                    item = new ItemStack(Material.STONE_SWORD);
                    break;
                case WAITING:
                    item = new ItemStack(Material.IRON_SWORD);
                    if (SettingsManager.getInstance().getSpawnCount(game.getID()) > 0){
                        //SurvivalGames.$(""+((float)item.getData().getItemType().getMaxDurability()*((float)game.getActivePlayers()/(float)SettingsManager.getInstance().getSpawnCount(game.getID())))+" "+ game.getActivePlayers() + " "+ SettingsManager.getInstance().getSpawnCount(game.getID())+ " " +(game.getActivePlayers()/SettingsManager.getInstance().getSpawnCount(game.getID())));
                        item.setDurability((short)(item.getData().getItemType().getMaxDurability()-(float)item.getData().getItemType().getMaxDurability()*((float)game.getActivePlayers()/(float)SettingsManager.getInstance().getSpawnCount(game.getID()))));
                    }
                    break;
                default:
                    item = new ItemStack(Material.IRON_SWORD);
            }
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName("Arena "+game.getID());
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(Game.GetColorPrefix(game.getGameMode()) + "" + game.getGameMode() + " - Players (" + game.getActivePlayers() + "/" + SettingsManager.getInstance().getSpawnCount(game.getID()) + ")");


            String []players = getMenuStringList(game.getID()).split("\n");

            for (String p: players){
                lore.add(p);
            }


            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);

            arenasInventory.setItem((0) + b, item);
            b++;
        }
        ItemStack ingameItem = new ItemStack(Material.WOOD_SWORD);
        ItemMeta im = ingameItem.getItemMeta();
        im.setDisplayName("Arena uzimta, zaidimas vyksta.");
        ingameItem.setItemMeta(im);

        ItemStack randomItem = new ItemStack(Material.BEACON);
        im.setDisplayName("Atsitiktinė arena.");
        randomItem.setItemMeta(im);

        ItemStack deathMachItem = new ItemStack(Material.STONE_SWORD);
        im.setDisplayName("Arenoje vyksta deathmach.");
        deathMachItem.setItemMeta(im);

        ItemStack defaultItem = new ItemStack(Material.IRON_SWORD);
        im.setDisplayName("Arena laisva galite jungtis.");
        defaultItem.setItemMeta(im);

        ItemStack startingItem = new ItemStack(Material.DIAMOND_SWORD);
        im.setDisplayName("Arenoje žaidimas toujau prasides.");
        startingItem.setItemMeta(im);
        arenasInventory.setItem(18, randomItem);
        arenasInventory.setItem(23, ingameItem);
        arenasInventory.setItem(24, deathMachItem);
        arenasInventory.setItem(25, defaultItem);
        arenasInventory.setItem(26, startingItem);

    }

	public Plugin getPlugin() {
		return p;
	}

	public void reloadGames() {
		LoadGames();
	}


	public void LoadKits(){
		Set<String> kits1 = SettingsManager.getInstance().getKits().getConfigurationSection("kits").getKeys(false);
		for(String s:kits1){
			kits.add(new Kit(s));
		}
	}

	public void LoadGames() {
		FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
		games.clear();
		int no = c.getInt("sg-system.arenano", 0);
		int loaded = 0;
		int a = 1;

		while (loaded < no) {
			if (c.isSet("sg-system.arenas." + a + ".x1")) {
				//c.set("sg-system.arenas."+a+".enabled",c.getBoolean("sg-system.arena."+a+".enabled", true));
				if (c.getBoolean("sg-system.arenas." + a + ".enabled")) {
					//SurvivalGames.$(c.getString("sg-system.arenas."+a+".enabled"));
					//c.set("sg-system.arenas."+a+".vip",c.getBoolean("sg-system.arenas."+a+".vip", false));
					//SurvivalGames.$("Loading Arena: " + a);
					loaded++;
					games.add(new Game(a));
					StatsManager.getInstance().addArena(a);
				}
			}
			a++;
			
		}
		LobbyManager.getInstance().clearAllSigns();
	}

	public int getBlockGameId(Location v) {
		for (Game g: games) {
			if (g.isBlockInArena(v)) {
				return g.getID();
			}
		}
		return -1;
	}

	public int getPlayerGameId(Player p) {
		for (Game g: games) {
			if (g.isPlayerActive(p)) {
				return g.getID();
			}
		}
		return -1;
	}

	public int getPlayerSpectateId(Player p) {
		for (Game g: games) {
			if (g.isSpectator(p)) {
				return g.getID();
			}
		}
		return -1;
	}

	public boolean isPlayerActive(Player player) {
		for (Game g: games) {
			if (g.isPlayerActive(player)) {
				return true;
			}
		}
		return false;
	}

	public boolean isPlayerInactive(Player player) {
		for (Game g: games) {
			if (g.isPlayerActive(player)) {
				return true;
			}
		}
		return false;
	}

	public boolean isSpectator(Player player) {
		for (Game g: games) {
			if (g.isSpectator(player)) {
				return true;
			}
		}
		return false;
	}

	public void removeFromOtherQueues(Player p, int id) {
		for (Game g: getGames()) {
			if (g.isInQueue(p) && g.getID() != id) {
				g.removeFromQueue(p);
				msgmgr.sendMessage(PrefixType.INFO, "Removed from the queue in arena " + g.getID(), p);
			}
		}
	}

	public boolean isInKitMenu(Player p){
		return kitsel.contains(p);
	}

	public void leaveKitMenu(Player p){
		kitsel.remove(p);
	}

	public void openKitMenu(Player p){
		kitsel.add(p);
	}

    public boolean isInArenaMenu(Player p){
        return arenaMenu.contains(p);
    }

    public void leaveArenaMenu(Player p){
        arenaMenu.remove(p);
    }

    public void openArenaMenu(Player p){
        arenaMenu.add(p);
    }

	@SuppressWarnings("deprecation")
	public void selectKit(Player p, int i) {
		p.getInventory().clear();
		ArrayList<Kit>kits = getKits(p);
		if(i <= kits.size()){
			Kit k = getKits(p).get(i);
			if(k!=null){
				p.getInventory().setContents(k.getContents().toArray(new ItemStack[0]));
			}
		}
		p.updateInventory();

	}

	public int getGameCount() {
		return games.size();
	}

	public Game getGame(int a) {
		//int t = gamemap.get(a);
		for (Game g: games) {
			if (g.getID() == a) {
				return g;
			}
		}
		return null;
	}

	public void removePlayer(Player p, boolean b) {
		getGame(getPlayerGameId(p)).playerLeave(p, b);
	}

	public void removeSpectator(Player p) {
		getGame(getPlayerSpectateId(p)).removeSpectator(p);
	}

	public void disableGame(int id) {
		getGame(id).disable();
	}

	public void enableGame(int id) {
		getGame(id).enable();
	}

	public ArrayList < Game > getGames() {
		return games;
	}

	public GameMode getGameMode(int a) {
		for (Game g: games) {
			if (g.getID() == a) {
				return g.getMode();
			}
		}
		return null;
	}

	public ArrayList<Kit> getKits(Player p){
		ArrayList<Kit>k = new ArrayList<Kit>();
		for(Kit kit: kits){
			if(kit.canUse(p)){
				k.add(kit);
			}
		}
		return k;
	}

	//TODO: Actually make this countdown correctly
	public void startGame(int a) {
		getGame(a).countdown(10);
	}

	public void addPlayer(Player p, int g) {
		Game game = getGame(g);
		if (game == null) {
			MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.input",p, "message-No game by this ID exist!");
			return;
		}
		getGame(g).addPlayer(p);
	}

	public void autoAddPlayer(Player pl) {
		ArrayList < Game > qg = new ArrayList < Game > (5);
		for (Game g: games) {
			if (g.getMode() == Game.GameMode.WAITING) qg.add(g);
		}
		//TODO: fancy auto balance algorithm
		if (qg.size() == 0) {
			pl.sendMessage(ChatColor.RED + "No games to join");
			msgmgr.sendMessage(PrefixType.WARNING, "No games to join!", pl);
			return;
		}
		qg.get(0).addPlayer(pl);
	}

	public WorldEditPlugin getWorldEdit() {
		return p.getWorldEdit();
	}

	public void createArenaFromSelection(Player pl) {
		FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
		//SettingsManager s = SettingsManager.getInstance();

		WorldEditPlugin we = p.getWorldEdit();
		Selection sel = we.getSelection(pl);
		if (sel == null) {
			msgmgr.sendMessage(PrefixType.WARNING, "You must make a WorldEdit Selection first!", pl);
			return;
		}
		Location max = sel.getMaximumPoint();
		Location min = sel.getMinimumPoint();

		/* if(max.getWorld()!=SettingsManager.getGameWorld() || min.getWorld()!=SettingsManager.getGameWorld()){
            pl.sendMessage(ChatColor.RED+"Wrong World!");
            return;
        }*/

		int no = c.getInt("sg-system.arenano") + 1;
		c.set("sg-system.arenano", no);
		if (games.size() == 0) {
			no = 1;
		} else no = games.get(games.size() - 1).getID() + 1;
		SettingsManager.getInstance().getSpawns().set(("spawns." + no), null);
		c.set("sg-system.arenas." + no + ".world", max.getWorld().getName());
		c.set("sg-system.arenas." + no + ".x1", max.getBlockX());
		c.set("sg-system.arenas." + no + ".y1", max.getBlockY());
		c.set("sg-system.arenas." + no + ".z1", max.getBlockZ());
		c.set("sg-system.arenas." + no + ".x2", min.getBlockX());
		c.set("sg-system.arenas." + no + ".y2", min.getBlockY());
		c.set("sg-system.arenas." + no + ".z2", min.getBlockZ());
		c.set("sg-system.arenas." + no + ".enabled", true);

		SettingsManager.getInstance().saveSystemConfig();
		hotAddArena(no);
		pl.sendMessage(ChatColor.GREEN + "Arena ID " + no + " Succesfully added");

	}

	private void hotAddArena(int no) {
		Game game = new Game(no);
		games.add(game);
		StatsManager.getInstance().addArena(no);
		//SurvivalGames.$("game added "+ games.size()+" "+SettingsManager.getInstance().getSystemConfig().getInt("gs-system.arenano"));
	}

	public void hotRemoveArena(int no) {
		for (Game g: games.toArray(new Game[0])) {
			if (g.getID() == no) {
				games.remove(getGame(no));
			}
		}
	}

	public void gameEndCallBack(int id) {
		getGame(id).setRBStatus("clearing chest");
		openedChest.put(id, new HashSet < Block > ());
	}

	public String getStringList(int gid){
		Game g = getGame(gid);
		StringBuilder sb = new StringBuilder();
		Player[][]players = g.getPlayers();

		sb.append(ChatColor.GREEN+"Alive:"+ChatColor.GREEN+" ");
		for(Player p: players[0]){
			sb.append(p.getName()+",");
		}
		sb.append("\n\n");
		sb.append(ChatColor.RED+  "Dead:"+ChatColor.GREEN+" ");
		for(Player p: players[1]){
			sb.append(p.getName()+",");
		}
		sb.append("\n\n");

		return sb.toString();
	}

    public String getMenuStringList(int gid){
        Game g = getGame(gid);
        StringBuilder sb = new StringBuilder();
        Player[][]players = g.getPlayers();

        sb.append(ChatColor.GREEN+"Alive:"+ChatColor.GREEN+" ");
        int i = 0;
        for(Player p: players[0]){
            if (i % 4 == 0){
                sb.append("\n");
            }
            sb.append(p.getName()+", ");
            i++;
        }
        sb.append("\n\n");
        sb.append(ChatColor.RED+  "Dead:"+ChatColor.GREEN+" ");
        i = 0;
        for(Player p: players[1]){
            if (i % 4 == 0){
                sb.append("\n");
            }
            sb.append(p.getName()+", ");
            i++;
        }
        sb.append("\n\n");

        return sb.toString();
    }

    public boolean showArenaMenu(Player player){
        openArenaMenu(player);
        // list all arenas
        ArrayList<Game> games = getGames();
        if (games.isEmpty()) {
            player.sendMessage(SettingsManager.getInstance().getMessageConfig().getString("messages.words.noarenas", "No arenas exist"));
            return false;
        }
        player.openInventory(arenasInventory);
        return true;
    }
}