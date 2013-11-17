package org.mcsg.survivalgames;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.mcsg.survivalgames.MessageManager.PrefixType;
import org.mcsg.survivalgames.api.PlayerJoinArenaEvent;
import org.mcsg.survivalgames.api.PlayerKilledEvent;
import org.mcsg.survivalgames.hooks.HookManager;
import org.mcsg.survivalgames.logging.QueueManager;
import org.mcsg.survivalgames.stats.StatsManager;
import org.mcsg.survivalgames.util.ItemReader;
import org.mcsg.survivalgames.util.Kit;

public class Game {

	public static enum GameMode {
		DISABLED, LOADING, INACTIVE, WAITING,
		STARTING, INGAME, FINISHING, RESETING, ERROR, DEATHMACH, STARTING_DEATHMACH
	}

	private GameMode mode = GameMode.DISABLED;
	private ArrayList < Player > activePlayers = new ArrayList < Player > ();
	private ArrayList < Player > inactivePlayers = new ArrayList < Player > ();
	private ArrayList < String > spectators = new ArrayList < String > ();
	private ArrayList < Player > queue = new ArrayList < Player > ();
	private HashMap < String, Object > flags = new HashMap < String, Object > ();
	HashMap < Player, Integer > nextspec = new HashMap < Player, Integer > ();
	private ArrayList<Integer>tasks = new ArrayList<Integer>();

	private Arena arena;
	private int gameID;
	private int gcount = 0;
	private FileConfiguration config;
	private FileConfiguration system;
	private HashMap < Integer, Player > spawns = new HashMap < Integer, Player > ();
	private HashMap < Player, ItemStack[][] > inv_store = new HashMap < Player, ItemStack[][] > ();
	private int spawnCount = 0;
	private int vote = 0;
	private boolean disabled = false;
	private int endgameTaskID = 0;
	private boolean endgameRunning = false;
    private int dmTaskID = 0;
    private int endGameDM;
	private String rbstatus = "";
    private double rbpercent = 0;
	private long startTime = 0;
	private boolean countdownRunning;
	private StatsManager sm = StatsManager.getInstance();
	private HashMap < String, String > hookvars = new HashMap < String, String > ();
	private MessageManager msgmgr = MessageManager.getInstance();
    private GameScoreboard scoreBoard = null;

    private Location center;

	public Game(int gameid) {
		gameID = gameid;
		reloadConfig();
		setup();
	}

	public void reloadConfig(){
		config = SettingsManager.getInstance().getConfig();
		system = SettingsManager.getInstance().getSystemConfig();
	}

	public void $(String msg){
		SurvivalGames.$(msg);
	}

	public void debug(String msg){
		SurvivalGames.debug(msg);
	}

	public void setup() {
		mode = GameMode.LOADING;
		int x = system.getInt("sg-system.arenas." + gameID + ".x1");
		int y = system.getInt("sg-system.arenas." + gameID + ".y1");
		int z = system.getInt("sg-system.arenas." + gameID + ".z1");
		//$(x + " " + y + " " + z);
		int x1 = system.getInt("sg-system.arenas." + gameID + ".x2");
		int y1 = system.getInt("sg-system.arenas." + gameID + ".y2");
		int z1 = system.getInt("sg-system.arenas." + gameID + ".z2");
		//$(x1 + " " + y1 + " " + z1);
		Location max = new Location(SettingsManager.getGameWorld(gameID), Math.max(x, x1), Math.max(y, y1), Math.max(z, z1));
		//$(max.toString());
		Location min = new Location(SettingsManager.getGameWorld(gameID), Math.min(x, x1), Math.min(y, y1), Math.min(z, z1));
		//$(min.toString());

		arena = new Arena(min, max);

		loadspawns();

		hookvars.put("arena", gameID + "");
		hookvars.put("maxplayers", spawnCount + "");
		hookvars.put("activeplayers", "0");

		mode = GameMode.WAITING;

        //Bukkit.broadcastMessage("Game " + gameID);
        scoreBoard = new GameScoreboard(gameID);
	}

    private Location getCenter(){
        double minX=0;
        double minZ=0;
        double maxX=0;
        double maxZ=0;
        double y=0;
        {
            Location l = SettingsManager.getInstance().getSpawnPoint(gameID, 1);
            y = l.getY();
            maxX = l.getX();
            maxZ = l.getZ();
            minX = l.getX();
            minZ = l.getZ();
        }

        for(int i=2; i<=spawnCount; i++){
            Location l = SettingsManager.getInstance().getSpawnPoint(gameID, i);
            if(l.getX() > maxX){
                maxX = l.getX();
            }
            if(l.getZ() > maxZ){
                maxZ = l.getZ();
            }
            if(l.getX() < minX){
                minX = l.getX();
            }
            if(l.getZ() < minZ){
                minZ = l.getZ();
            }
        }


        SurvivalGames.$("Center location of arena "+gameID+" is, max x: " + maxX +" minX: "+ minX +" maxZ: "+ maxZ +" minZ: " + minZ);
        SurvivalGames.$("Center location of arena "+gameID+" is, x: " + ((minX+maxX)/2) +" y: "+ y +" z: "+ ((minZ+maxZ)/2));
        return new Location(SettingsManager.getGameWorld(gameID),(minX+maxX)/2 ,y, (minZ+maxZ)/2);
    }

	public void reloadFlags() {
		flags = SettingsManager.getInstance().getGameFlags(gameID);

        scoreBoard.reset();
	}

	public void saveFlags() {
		SettingsManager.getInstance().saveGameFlags(flags, gameID);
	}

	public void loadspawns() {
		for (int a = 1; a <= SettingsManager.getInstance().getSpawnCount(gameID); a++) {
			spawns.put(a, null);
			spawnCount = a;
		}
        center = getCenter();
	}

	public void addSpawn() {
		spawnCount++;
		spawns.put(spawnCount, null);
	}

	public void setMode(GameMode m) {
		mode = m;
	}

	public GameMode getGameMode() {
		return mode;
	}

	public Arena getArena() {
		return arena;
	}


	/*
	 * 
	 * ################################################
	 * 
	 * 				ENABLE
	 * 
	 * ################################################
	 * 
	 * 
	 */


	public void enable() {
		mode = GameMode.WAITING;
		if(disabled){
			MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gameenabled", "arena-"+gameID);
		}
		disabled = false;
		int b = (SettingsManager.getInstance().getSpawnCount(gameID) > queue.size()) ? queue.size() : SettingsManager.getInstance().getSpawnCount(gameID);
		for (int a = 0; a < b; a++) {
			addPlayer(queue.remove(0));
		}
		int c = 1;
		for (Player p : queue) {
			msgmgr.sendMessage(PrefixType.INFO, "You are now #" + c + " in line for arena " + gameID, p);
			c++;
		}

		LobbyManager.getInstance().updateWall(gameID);

		MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamewaiting", "arena-"+gameID);

        scoreBoard.reset();
	}


	/*
	 * 
	 * ################################################
	 * 
	 * 				ADD PLAYER
	 * 
	 * ################################################
	 * 
	 * 
	 */


	public boolean addPlayer(Player p) {
		if(SettingsManager.getInstance().getLobbySpawn() == null){
			msgmgr.sendFMessage(PrefixType.WARNING, "error.nolobbyspawn", p);
			return false;
		}
		if(!p.hasPermission("sg.arena.join."+gameID)){
			debug("permission needed to join arena: " + "sg.arena.join."+gameID);
			msgmgr.sendFMessage(PrefixType.WARNING, "game.nopermission", p, "arena-"+gameID);
			return false;
		}
		HookManager.getInstance().runHook("GAME_PRE_ADDPLAYER", "arena-"+gameID, "player-"+p.getName(), "maxplayers-"+spawns.size(), "players-"+activePlayers.size());

		GameManager.getInstance().removeFromOtherQueues(p, gameID);

		if (GameManager.getInstance().getPlayerGameId(p) != -1) {
			if (GameManager.getInstance().isPlayerActive(p)) {
				msgmgr.sendMessage(PrefixType.ERROR, "Cannot join multiple games!", p);
				return false;
			}
		}
		if(p.isInsideVehicle()){
			p.leaveVehicle();
		}
		if (spectators.contains(p)) removeSpectator(p);
		if (mode == GameMode.WAITING || mode == GameMode.STARTING) {
			if (activePlayers.size() < SettingsManager.getInstance().getSpawnCount(gameID)) {
				msgmgr.sendMessage(PrefixType.INFO, "Joining Arena " + gameID, p);
				PlayerJoinArenaEvent joinarena = new PlayerJoinArenaEvent(p, GameManager.getInstance().getGame(gameID));
				Bukkit.getServer().getPluginManager().callEvent(joinarena);
				if(joinarena.isCancelled()) return false;
				boolean placed = false;
				int spawnCount = SettingsManager.getInstance().getSpawnCount(gameID);

				for (int a = 1; a <= spawnCount; a++) {
					if (spawns.get(a) == null) {
						placed = true;
						spawns.put(a, p);
                        SurvivalGames.$("Spawn: "+a);
						p.setGameMode(org.bukkit.GameMode.SURVIVAL);

						p.teleport(SettingsManager.getInstance().getLobbySpawn());           //why??

						saveInv(p);
                        clearInv(p);

                        Location l = SettingsManager.getInstance().getSpawnPoint(gameID, a);

                        SurvivalGames.$("Center location of arena "+gameID+" is, x: " + center.getX() +" z: "+ center.getZ());

                        double dX =  center.getX() - l.getX();
                        double dZ =  center.getZ() - l.getZ();
                        double yaw = Math.atan2(dZ, dX) * (180/Math.PI) - 90;

                        SurvivalGames.$("Delta location of arena "+gameID+" is, x: " + dX +" z: "+ dZ);

                        SurvivalGames.$("Yaw: " + yaw);

                        l.setYaw((float) yaw);                                    //face players to center of the arena

						p.teleport(l);

                        p.setBedSpawnLocation(SettingsManager.getInstance().getLobbySpawn());

						p.setHealth(p.getMaxHealth());p.setFoodLevel(20);clearInv(p);p.setLevel(0);p.setExp(0);

						activePlayers.add(p);sm.addPlayer(p, gameID);
                        scoreBoard.addPlayer(p);
						hookvars.put("activeplayers", activePlayers.size()+"");
						LobbyManager.getInstance().updateWall(gameID);
						showMenu(p);
						HookManager.getInstance().runHook("GAME_POST_ADDPLAYER", "activePlayers-"+activePlayers.size());

						if(spawnCount == activePlayers.size()){
							countdown(5);
						}
						break;
					}
				}
				if (!placed) {
					msgmgr.sendFMessage(PrefixType.ERROR,"error.gamefull", p,"arena-"+gameID);
					return false;
				}

			} else if (SettingsManager.getInstance().getSpawnCount(gameID) == 0) {
				msgmgr.sendMessage(PrefixType.WARNING, "No spawns set for Arena " + gameID + "!", p);
				return false;
			} else {
				msgmgr.sendFMessage(PrefixType.WARNING, "error.gamefull", p, "arena-"+gameID);
				return false;
			}
			msgFall(PrefixType.INFO, "game.playerjoingame", "player-"+p.getName(), "activeplayers-"+ getActivePlayers(), "maxplayers-"+ SettingsManager.getInstance().getSpawnCount(gameID));
			if (activePlayers.size() >= config.getInt("auto-start-players") && !countdownRunning) countdown(config.getInt("auto-start-time"));
			return true;
		} else {
			if (config.getBoolean("enable-player-queue")) {
				if (!queue.contains(p)) {
					queue.add(p);
					msgmgr.sendFMessage(PrefixType.INFO, "game.playerjoinqueue", p, "queuesize-"+queue.size());
				}
				int a = 1;
				for (Player qp: queue) {
					if (qp == p) {
						msgmgr.sendFMessage(PrefixType.INFO, "game.playercheckqueue", p,"queuepos-"+a);
						break;
					}
					a++;
				}
			}
		}
		if (mode == GameMode.INGAME) msgmgr.sendFMessage(PrefixType.WARNING, "error.alreadyingame", p);
		else if (mode == GameMode.DISABLED) msgmgr.sendFMessage(PrefixType.WARNING, "error.gamedisabled", p, "arena-"+gameID);
		else if (mode == GameMode.RESETING) msgmgr.sendFMessage(PrefixType.WARNING, "error.gamereseting", p);
		else msgmgr.sendMessage(PrefixType.INFO, "Cannot join game!", p);
		LobbyManager.getInstance().updateWall(gameID);
		return false;
	}



	public void showMenu(Player p){
		GameManager.getInstance().openKitMenu(p);
		Inventory i = Bukkit.getServer().createInventory(p, 9, ChatColor.RED+""+ChatColor.BOLD+"Kit Selection");

		int b = 0;
		ArrayList<Kit>kits = GameManager.getInstance().getKits(p);
		//SurvivalGames.debug(kits+"");
		if(kits == null || kits.size() == 0 || !SettingsManager.getInstance().getKits().getBoolean("enabled")){
			GameManager.getInstance().leaveKitMenu(p);
			return;
		}

		for(Kit k: kits){
			ItemStack i1 = k.getIcon();
			ItemMeta im = i1.getItemMeta();
			im.setDisplayName(ChatColor.GOLD+""+ChatColor.BOLD+k.getName());
			i1.setItemMeta(im);
			i.setItem((0) + b, i1);
			b++;
		}
		p.openInventory(i);
		debug("Showing menu");
	}




	public void removeFromQueue(Player p) {
		queue.remove(p);
	}

	/*
	 * 
	 * ################################################
	 * 
	 * 				VOTE
	 * 
	 * ################################################
	 * 
	 * 
	 */


	ArrayList < Player > voted = new ArrayList < Player > ();

	public void vote(Player pl) {
        if (GameMode.STARTING == mode) {
            msgmgr.sendMessage(PrefixType.WARNING, "Game already starting!", pl);
            return;
        }
        if (GameMode.WAITING != mode) {
            msgmgr.sendMessage(PrefixType.WARNING, "Game already started!", pl);
            return;
        }
        if (voted.contains(pl)) {
            msgmgr.sendMessage(PrefixType.WARNING, "You already voted!", pl);
            return;
        }
        vote++;
        voted.add(pl);
        msgmgr.sendFMessage(PrefixType.INFO, "game.playervote", pl, "player-"+pl.getName());
        HookManager.getInstance().runHook("PLAYER_VOTE", "player-"+pl.getName());
        scoreBoard.playerLiving(pl);
		/*for(Player p: activePlayers){
            p.sendMessage(ChatColor.AQUA+pl.getName()+" Voted to start the game! "+ Math.round((vote +0.0) / ((getActivePlayers() +0.0)*100)) +"/"+((c.getInt("auto-start-vote")+0.0))+"%");
        }*/
        // Bukkit.getServer().broadcastPrefixType((vote +0.0) / (getActivePlayers() +0.0) +"% voted, needs "+(c.getInt("auto-start-vote")+0.0)/100);
        if ((((vote + 0.0) / (getActivePlayers() +0.0))>=(config.getInt("auto-start-vote")+0.0)/100) && getActivePlayers() > 1) {
            countdown(config.getInt("auto-start-time"));
            for (Player p: activePlayers) {
                //p.sendMessage(ChatColor.LIGHT_PURPLE + "Game Starting in " + c.getInt("auto-start-time"));
                msgmgr.sendMessage(PrefixType.INFO, "Game starting in " + config.getInt("auto-start-time") + "!", p);
                scoreBoard.playerLiving(pl);
            }
        }
        //StatsManager.getInstance().updateScoreboard(gameID);
    }

	/*
	 * 
	 * ################################################
	 * 
	 * 				START GAME
	 * 
	 * ################################################
	 * 
	 * 
	 */
    private int nextStrike = 1200;     //tik'ai

	public void startGame() {
		if (mode == GameMode.INGAME) {
			return;
		}

		if (activePlayers.size() <= 0) {
			for (Player pl: activePlayers) {
				msgmgr.sendMessage(PrefixType.WARNING, "Not enough players!", pl);
				mode = GameMode.WAITING;
				LobbyManager.getInstance().updateWall(gameID);

			}
			return;
		} else if (mode == GameMode.STARTING_DEATHMACH) {
            nextStrike = 1200; //tikks
            mode = GameMode.DEATHMACH;
            tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), new LightningStrike(), nextStrike));
            MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.deathmachstarted", "arena-"+gameID);
        } else {
			startTime = new Date().getTime();
			for (Player pl: activePlayers) {
				pl.setHealth(pl.getMaxHealth());
				//clearInv(pl);
				msgmgr.sendFMessage(PrefixType.INFO, "game.goodluck", pl);
			}
			if (config.getBoolean("restock-chest")) {
				SettingsManager.getGameWorld(gameID).setTime(0);
				gcount++;
				tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(),
						new NightChecker(),
						14400));
			}
			if (config.getInt("grace-period") != 0) {
				for (Player play: activePlayers) {
					msgmgr.sendMessage(PrefixType.INFO, "You have a " + config.getInt("grace-period") + " second grace period!", play);
				}
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), new Runnable() {
					public void run() {
						for (Player play: activePlayers) {
							msgmgr.sendMessage(PrefixType.INFO, "Grace period has ended!", play);
						}
					}
				}, config.getInt("grace-period") * 20);
			}
            if (activePlayers.size() > config.getInt("endgame.players")){
                endGameDM = config.getInt("endgame.players");
            } else {
                endGameDM = 2;
            }
            if(config.getBoolean("deathmatch.enabled")) {
                SurvivalGames.$("Launching deathmatch timer...");
                dmTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameManager.getInstance().getPlugin(), new DeathMatchTimer(), 40L, 20L);
                tasks.add(dmTaskID);
            }
            mode = GameMode.INGAME;
            MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamestarted", "arena-"+gameID);
		}
		LobbyManager.getInstance().updateWall(gameID);
	}
	/*
	 * 
	 * ################################################
	 * 
	 * 				COUNTDOWN
	 * 
	 * ################################################
	 * 
	 * 
	 */
	public int getCountdownTime() {
		return count;
	}

	int count = 20;
	int tid = 0;
	public void countdown(int time) {
		//Bukkit.broadcastMessage(""+time);
        MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamestarting", "arena-"+gameID, "t-"+time);
		countdownRunning = true;
		count = time;
		Bukkit.getScheduler().cancelTask(tid);

		if (mode == GameMode.WAITING || mode == GameMode.STARTING || mode == GameMode.STARTING_DEATHMACH) {
            if (mode != GameMode.STARTING_DEATHMACH){
                mode  = GameMode.STARTING;
                tid = Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin) GameManager.getInstance().getPlugin(), new Runnable() {
                    public void run() {
                        if (count > 0) {
                            if (count % 10 == 0) {
                                msgFall(PrefixType.INFO, "game.countdown","t-"+count);
                            }
                            if (count < 6) {
                                msgFall(PrefixType.INFO, "game.countdown","t-"+count);
                            }
                            count--;
                            LobbyManager.getInstance().updateWall(gameID);
                        } else {
                            startGame();
                            Bukkit.getScheduler().cancelTask(tid);
                            countdownRunning = false;
                        }
                    }
                }, 0, 20);
            } else {
                tid = Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin) GameManager.getInstance().getPlugin(), new Runnable() {
                    public void run() {
                        if (count > 0) {
                            if (count % 10 == 0) {
                                msgFall(PrefixType.INFO, "game.deathmachcountdown","t-"+count);
                            }
                            if (count < 6) {
                                msgFall(PrefixType.INFO, "game.deathmachcountdown","t-"+count);

                            }
                            count--;
                            LobbyManager.getInstance().updateWall(gameID);
                        } else {
                            startGame();
                            Bukkit.getScheduler().cancelTask(tid);
                            countdownRunning = false;
                        }
                    }
                }, 0, 20);
            }


		}
	}

	/*
	 * 
	 * ################################################
	 * 
	 * 				REMOVE PLAYER
	 * 
	 * ################################################
	 * 
	 * 
	 */

    public void playerLeave(final Player p, boolean teleport) {
        msgFall(PrefixType.INFO, "game.playerleavegame", "player-" + p.getName());
        if (teleport) {
            p.teleport(SettingsManager.getInstance().getLobbySpawn());
        }

        sm.removePlayer(p, gameID);
        scoreBoard.removePlayer(p);
        activePlayers.remove(p);
        inactivePlayers.remove(p);
        voted.remove(p);
        restoreInv(p);
        for (Object in : spawns.keySet().toArray()) {
            if (spawns.get(in) == p) spawns.remove(in);
        }

        HookManager.getInstance().runHook("PLAYER_REMOVED", "player-"+p.getName());
        LobbyManager.getInstance().updateWall(gameID);
        SurvivalGames.$("DEBUG: Leave. arena: " + gameID + "  active players "+ activePlayers.size());
        if (activePlayers.size() < 2 && mode != GameMode.WAITING) {
            tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), new Runnable() {
                public void run(){
                    playerWin(p);
                    endGame();
                }
            }, 1L));
        } else if (getActivePlayers() <= endGameDM && (mode != GameMode.WAITING || mode != GameMode.DEATHMACH || mode != GameMode.STARTING_DEATHMACH)){
            deathMach();
        }

        LobbyManager.getInstance().updateWall(gameID);

        //MoveEvent.removePlayer();
    }

    /*
     *
     * ################################################
     *
     * 			   HANDLE PLAYER DEATH
     *
     *  PLAYERS DIE A REAL DEATH WHICH IS HANDLED HERE
     *
     * ################################################
     *
     *
     */
    public void playerDeath(PlayerDeathEvent e) {

            final Player p = e.getEntity();
            if (!activePlayers.contains(p)) return;

            sm.playerDied(p, activePlayers.size(), gameID, new Date().getTime() - startTime);
            scoreBoard.playerDead(p);
            activePlayers.remove(p);
            inactivePlayers.add(p);
            restoreInv(p);

            for (Object in : spawns.keySet().toArray()) {
                if (spawns.get(in) == p) spawns.remove(in);
            }

            PlayerKilledEvent pk = null;
            if ((mode == GameMode.INGAME || mode == GameMode.INGAME ) && p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null){
                EntityDamageEvent.DamageCause cause = p.getLastDamageCause().getCause();
                switch (cause) {
                    case ENTITY_ATTACK:
                        if(p.getLastDamageCause().getEntityType() == EntityType.PLAYER){
                            EntityType enttype = p.getLastDamageCause().getEntityType();
                            Player killer = p.getKiller();
                            String killername = "Unknown";

                            if (killer != null) {
                                killername = killer.getName();
                            }

                            String itemname = "Unknown Item";
                            if (killer != null) {
                                itemname = ItemReader.getFriendlyItemName(killer.getItemInHand().getType());
                            }

                            msgFall(PrefixType.INFO, "death."+enttype, "player-"+p.getName(), "killer-"+killername, "item-"+itemname);

                            if (killer != null && p != null) {
                                sm.addKill(killer, p, gameID);
                                scoreBoard.incScore(killer);
                            }
                            pk = new PlayerKilledEvent(p, this, killer, cause);
                        }
                        else {
                            msgFall(PrefixType.INFO, "death." + p.getLastDamageCause().getEntityType(),
                                    "player-" + p.getName(),
                                    "killer-" + p.getLastDamageCause().getEntityType());
                            pk = new PlayerKilledEvent(p, this, null, cause);
                        }
                        break;
                    default:
                        msgFall(PrefixType.INFO, "death." + cause.name(),
                                "player-" + p.getName(),
                                "killer-" + cause);
                        pk = new PlayerKilledEvent(p, this, null, cause);

                        break;
                }
                Bukkit.getServer().getPluginManager().callEvent(pk);
            }

            if (getActivePlayers() > 1) {
                for (Player pl: getAllPlayers()) {
                    msgmgr.sendMessage(PrefixType.INFO, ChatColor.DARK_AQUA + "There are " + ChatColor.YELLOW + ""
                            + getActivePlayers() + ChatColor.DARK_AQUA + " players remaining!", pl);
                }
            }

            for (Player pe: activePlayers) {
                Location l = pe.getLocation();
                l.setY(l.getWorld().getMaxHeight());
                l.getWorld().strikeLightningEffect(l);
            }


            SurvivalGames.$("DEBUG: Death. arena: " + gameID + "  active players "+ activePlayers.size());
            if (activePlayers.size() < 2 && mode != GameMode.WAITING) {
                tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), new Runnable(){
                    public void run(){
                        playerWin(p);
                        endGame();
                    }
                }, 5L));
            } else if (activePlayers.size() <= endGameDM && (mode != GameMode.DEATHMACH || mode != GameMode.STARTING_DEATHMACH)){
                deathMach();
            }
            /** EndgameManager is replaced by Deathmach*/
            /* if (getActivePlayers() <= config.getInt("endgame.players") && config.getBoolean("endgame.fire-lighting.enabled") && !endgameRunning) {
                tasks.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(GameManager.getInstance().getPlugin(),
                    new EndgameManager(),
                    0,
                    config.getInt("endgame.fire-lighting.interval") * 20));
            }
            */


            LobbyManager.getInstance().updateWall(gameID);
    }


	/*
	 * 
	 * ################################################
	 * 
	 * 				PLAYER WIN
	 * 
	 * ################################################
	 * 
	 * 
	 */

	public void playerWin(Player p) {
		if (GameMode.DISABLED == mode) return;
		Player win = activePlayers.get(0);
		// clearInv(p);
		win.teleport(SettingsManager.getInstance().getLobbySpawn());
        scoreBoard.removePlayer(p);
		restoreInv(win);
		msgmgr.broadcastFMessage(PrefixType.INFO, "game.playerwin","arena-"+gameID, "victim-"+p.getName(), "player-"+win.getName());
		LobbyManager.getInstance().display(new String[] {
				win.getName(), "", "Won the ", "Survival Games!"
		}, gameID);

		mode = GameMode.FINISHING;

		clearSpecs();
		win.setHealth(p.getMaxHealth());
		win.setFoodLevel(20);
		win.setFireTicks(0);
		win.setFallDistance(0);

		sm.playerWin(win, gameID, new Date().getTime() - startTime);
		sm.saveGame(gameID, win, getActivePlayers() + getInactivePlayers(), new Date().getTime() - startTime);

		activePlayers.clear();
		inactivePlayers.clear();
		spawns.clear();

		loadspawns();
		LobbyManager.getInstance().updateWall(gameID);
		MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gameend", "arena-"+gameID);

	}

	public void endGame() {
		mode = GameMode.WAITING;
		resetArena();
		LobbyManager.getInstance().clearSigns(gameID);
		LobbyManager.getInstance().updateWall(gameID);

	}
	/*
	 * 
	 * ################################################
	 * 
	 * 				DISABLE
	 * 
	 * ################################################
	 * 
	 * 
	 */
	public void disable() {
		disabled = true;
		spawns.clear();
        scoreBoard.reset();

		for (int a = 0; a < activePlayers.size(); a = 0) {
			try {

				Player p = activePlayers.get(a);
				msgmgr.sendMessage(PrefixType.WARNING, "Game disabled!", p);
                playerLeave(p, true);
			} catch (Exception e) {}

		}

		for (int a = 0; a < inactivePlayers.size(); a = 0) {
			try {

				Player p = inactivePlayers.remove(a);
				msgmgr.sendMessage(PrefixType.WARNING, "Game disabled!", p);
			} catch (Exception e) {}

		}

		clearSpecs();
		queue.clear();

		endGame();
		LobbyManager.getInstance().updateWall(gameID);
		MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamedisabled", "arena-"+gameID);

	}
	/*
	 * 
	 * ################################################
	 * 
	 * 				RESET
	 * 
	 * ################################################
	 * 
	 * 
	 */
	public void resetArena() {

		for(Integer i: tasks){
			Bukkit.getScheduler().cancelTask(i);
		}

		tasks.clear();
		vote = 0;
		voted.clear();

		mode = GameMode.RESETING;
		endgameRunning = false;

		Bukkit.getScheduler().cancelTask(endgameTaskID);
		GameManager.getInstance().gameEndCallBack(gameID);
		QueueManager.getInstance().rollback(gameID, false);
		LobbyManager.getInstance().updateWall(gameID);

        scoreBoard.reset();
	}

	public void resetCallback() {
		if (!disabled){
			enable();
		}
		else mode = GameMode.DISABLED;
		LobbyManager.getInstance().updateWall(gameID);
	}

	public void saveInv(Player p) {
		ItemStack[][] store = new ItemStack[2][1];

		store[0] = p.getInventory().getContents();
		store[1] = p.getInventory().getArmorContents();

		inv_store.put(p, store);

	}

	public void restoreInvOffline(String p) {
        restoreInv(Bukkit.getPlayer(p));
	}


	/*
	 * 
	 * ################################################
	 * 
	 * 				SPECTATOR
	 * 
	 * ################################################
	 * 
	 * 
	 */

	public void addSpectator(Player p) {
		saveInv(p);
		clearInv(p);
		p.teleport(SettingsManager.getInstance().getSpawnPoint(gameID, 1).add(0, 10, 0));

		HookManager.getInstance().runHook("PLAYER_SPECTATE", "player-"+p.getName());

		for (Player pl: Bukkit.getOnlinePlayers()) {
			pl.hidePlayer(p);
		}

		p.setAllowFlight(true);
		p.setFlying(true);
		spectators.add(p.getName());
		msgmgr.sendMessage(PrefixType.INFO, "You are now spectating! Use /sg spectate again to return to the lobby.", p);
		msgmgr.sendMessage(PrefixType.INFO, "Right click while holding shift to teleport to the next ingame player, left click to go back.", p);
		nextspec.put(p, 0);
	}

	public void removeSpectator(Player p) {
		ArrayList < Player > players = new ArrayList < Player > ();
		players.addAll(activePlayers);
		players.addAll(inactivePlayers);

		if(p.isOnline()){
			for (Player pl: Bukkit.getOnlinePlayers()) {
				pl.showPlayer(p);
			}
		}
		restoreInv(p);
		p.setAllowFlight(false);
		p.setFlying(false);
		p.setFallDistance(0);
		p.setHealth(p.getMaxHealth());
		p.setFoodLevel(20);
		p.setSaturation(20);
		p.teleport(SettingsManager.getInstance().getLobbySpawn());
		// Bukkit.getServer().broadcastPrefixType("Removing Spec "+p.getName()+" "+spectators.size()+" left");
		spectators.remove(p.getName());
		// Bukkit.getServer().broadcastPrefixType("Removed");

		nextspec.remove(p);
	}

	public void clearSpecs() {

		for (int a = 0; a < spectators.size(); a = 0) {
			removeSpectator(Bukkit.getPlayerExact(spectators.get(0)));
		}
		spectators.clear();
		nextspec.clear();
	}


	public HashMap < Player, Integer > getNextSpec() {
		return nextspec;
	}

	@SuppressWarnings("deprecation")
	public void restoreInv(Player p) {
		try {
			clearInv(p);
			p.getInventory().setContents(inv_store.get(p)[0]);
			p.getInventory().setArmorContents(inv_store.get(p)[1]);
			inv_store.remove(p);
			p.updateInventory();
		} catch (Exception e) {
            p.sendMessage(ChatColor.RED+"Inentory failed to restore or nothing was in it.");
		}
	}

	@SuppressWarnings("deprecation")
	public void clearInv(Player p) {

		ItemStack[] inv = p.getInventory().getContents();
		for (int i = 0; i < inv.length; i++) {
			inv[i] = null;
		}
		p.getInventory().setContents(inv);
		inv = p.getInventory().getArmorContents();
		for (int i = 0; i < inv.length; i++) {
			inv[i] = null;
		}
		p.getInventory().setArmorContents(inv);
		p.updateInventory();

	}



	class NightChecker implements Runnable {
		boolean reset = false;
		int tgc = gcount;
		public void run() {
			if (SettingsManager.getGameWorld(gameID).getTime() > 14000) {
				for (Player pl: activePlayers) {
					msgmgr.sendMessage(PrefixType.INFO, "Chests restocked!", pl);
				}
				GameManager.openedChest.get(gameID).clear();
				reset = true;
			}

		}
	}

	class EndgameManager implements Runnable {
		@Override
		public void run() {
			for (Player player: activePlayers.toArray(new Player[0])) {
				Location l = player.getLocation();
				l.add(0, 5, 0);
				player.getWorld().strikeLightningEffect(l);
			}
		}
	}

    class LightningStrike implements Runnable {
        public void run() {
            if (nextStrike>200){
                nextStrike -=200;
            } else if (nextStrike > 20){
                nextStrike -=20;
            }
            for(Player p: activePlayers) {
                SurvivalGames.$("Zaibas " + p.getName());
                p.getLocation().getWorld().strikeLightningEffect(p.getLocation());
                p.damage(3);
            }
            tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), new LightningStrike(), nextStrike));
        }
    }

    class DeathMatchTimer implements Runnable {
        public void run() {
            if (activePlayers.size() < 1){
                endGame();
            }
            int now = (int) (new Date().getTime() / 1000);
            long length = config.getInt("deathmatch.time") * 60;//
            long remaining = (length - (now - (startTime / 1000)));
            //SurvivalGames.$("Remaining: " + remaining + " (" + now + " / " + length + " / " + (startTime / 1000) + ")");

            // Every 3 minutes or every minute in the last 3 minutes
            if (((remaining % 180) == 0) || (((remaining % 60) == 0) && (remaining <= 180))) {
                if (remaining > 0) {
                    msgFall(PrefixType.INFO, "game.deathmatchwarning", "t-" + (remaining / 60));
                }
            }

            // Death match time!!
            if (remaining >= 1) return;
            deathMach();
        }
    }

    void deathMach(){
        Bukkit.getScheduler().cancelTask(dmTaskID);
        if (!tasks.remove((Integer) dmTaskID)) {
            SurvivalGames.$("WARNING: DeathMatch task NOT removed!");
        }
        if (mode == GameMode.INGAME) {
            mode = GameMode.STARTING_DEATHMACH;
            countdown(10);
        }
        for(Player p: activePlayers){
            for(int a = 0; a < spawns.size(); a++){
                if(spawns.get(a) == p){
                    p.teleport(SettingsManager.getInstance().getSpawnPoint(gameID, a));
                    p.sendMessage(ChatColor.RED + "DeathMach!! Get READY!!");
                    break;
                }
            }
        }
    }

	public boolean isBlockInArena(Location v) {
		return arena.containsBlock(v);
	}

	public boolean isProtectionOn() {
        //SurvivalGames.$("DAMAGE: "+(mode != GameMode.DEATHMACH) + " " +  (mode != GameMode.INGAME));
		long t = startTime / 1000;
		long l = config.getLong("grace-period");
		long d = new Date().getTime() / 1000;
		if ((d - t) < l || !(mode != GameMode.DEATHMACH ^ mode != GameMode.INGAME)) {
            return true;
        }
        return false;
	}


	public int getID() {
		return gameID;
	}

	public int getActivePlayers() {
		return activePlayers.size();
	}

	public int getInactivePlayers() {
		return inactivePlayers.size();
	}

	public Player[][] getPlayers() {
		return new Player[][] {
		    activePlayers.toArray(new Player[0]), inactivePlayers.toArray(new Player[0])
		};
	}

	public ArrayList < Player > getAllPlayers() {
		ArrayList < Player > all = new ArrayList < Player > ();
		all.addAll(activePlayers);
		all.addAll(inactivePlayers);
		return all;
	}

    public GameScoreboard getScoreboard() {
        return scoreBoard;
    }

	public boolean isSpectator(Player p) {
		return spectators.contains(p.getName());
	}

	public boolean isInQueue(Player p) {
		return queue.contains(p);
	}

	public boolean isPlayerActive(Player player) {
		return activePlayers.contains(player);
	}
	public boolean isPlayerinactive(Player player) {
		return inactivePlayers.contains(player);
	}
	public boolean hasPlayer(Player p) {
		return activePlayers.contains(p) || inactivePlayers.contains(p);
	}
	public GameMode getMode() {
		return mode;
	}

	public synchronized void setRBPercent(double d) {
		rbpercent = d;
	}

	public double getRBPercent() {
		return rbpercent;
	}

	public void setRBStatus(String s) {
		rbstatus = s;
	}

    public int getPlayerSpawn(Player p){
        for(int a = 0; a < spawns.size(); a++){
            if(spawns.get(a) == p){
                return  a;
            }
        }
        return 1;
    }

	public String getRBStatus() {
		return rbstatus;
	}

	public String getName() {
		return "Arena "+gameID;
	}
    public boolean isVoted(Player p){
        return voted.contains(p);
    }
	public void msgFall(PrefixType type, String msg, String...vars){
		for(Player p: getAllPlayers()){
			msgmgr.sendFMessage(type, msg, p, vars);
		}
	}


    public static ChatColor GetColorPrefix(GameMode gameMode) {

        if (gameMode == GameMode.DISABLED)
            return ChatColor.RED;
        if (gameMode == GameMode.ERROR)
            return ChatColor.DARK_RED;
        if (gameMode == GameMode.FINISHING)
            return ChatColor.DARK_PURPLE;
        if (gameMode == GameMode.WAITING)
            return ChatColor.GOLD;
        if (gameMode == GameMode.INGAME)
            return ChatColor.DARK_GREEN;
        if (gameMode == GameMode.STARTING)
            return ChatColor.GREEN;
        if (gameMode == GameMode.RESETING)
            return ChatColor.DARK_AQUA;
        if (gameMode == GameMode.LOADING)
            return ChatColor.BLUE;
        if (gameMode == GameMode.INACTIVE)
            return ChatColor.DARK_GRAY;

        return ChatColor.WHITE;
    }
}