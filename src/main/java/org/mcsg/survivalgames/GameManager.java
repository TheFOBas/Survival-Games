package org.mcsg.survivalgames;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.*;
import org.bukkit.util.Vector;
import org.mcsg.survivalgames.Game.GameMode;
import org.mcsg.survivalgames.MessageManager.PrefixType;
import org.mcsg.survivalgames.api.PlayerLeaveArenaEvent;
import org.mcsg.survivalgames.stats.StatsManager;
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
        arenasInventory = Bukkit.getServer().createInventory(new HumanEntity() {
            @Override
            public String getName() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public PlayerInventory getInventory() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Inventory getEnderChest() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean setWindowProperty(InventoryView.Property property, int i) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public InventoryView getOpenInventory() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public InventoryView openInventory(Inventory itemStacks) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public InventoryView openWorkbench(Location location, boolean b) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public InventoryView openEnchanting(Location location, boolean b) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void openInventory(InventoryView inventoryView) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void closeInventory() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ItemStack getItemInHand() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setItemInHand(ItemStack itemStack) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ItemStack getItemOnCursor() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setItemOnCursor(ItemStack itemStack) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isSleeping() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getSleepTicks() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public org.bukkit.GameMode getGameMode() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setGameMode(org.bukkit.GameMode gameMode) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isBlocking() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getExpToLevel() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public double getEyeHeight() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public double getEyeHeight(boolean b) {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Location getEyeLocation() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public List<Block> getLineOfSight(HashSet<Byte> bytes, int i) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Block getTargetBlock(HashSet<Byte> bytes, int i) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public List<Block> getLastTwoTargetBlocks(HashSet<Byte> bytes, int i) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Egg throwEgg() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Snowball throwSnowball() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Arrow shootArrow() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public <T extends Projectile> T launchProjectile(Class<? extends T> aClass) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getRemainingAir() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setRemainingAir(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getMaximumAir() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setMaximumAir(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getMaximumNoDamageTicks() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setMaximumNoDamageTicks(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getLastDamage() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setLastDamage(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getNoDamageTicks() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setNoDamageTicks(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Player getKiller() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean addPotionEffect(PotionEffect potionEffect) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean addPotionEffect(PotionEffect potionEffect, boolean b) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean addPotionEffects(Collection<PotionEffect> potionEffects) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean hasPotionEffect(PotionEffectType potionEffectType) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void removePotionEffect(PotionEffectType potionEffectType) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Collection<PotionEffect> getActivePotionEffects() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean hasLineOfSight(Entity entity) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean getRemoveWhenFarAway() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setRemoveWhenFarAway(boolean b) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public EntityEquipment getEquipment() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setCanPickupItems(boolean b) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean getCanPickupItems() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setCustomName(String s) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public String getCustomName() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setCustomNameVisible(boolean b) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isCustomNameVisible() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void damage(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void damage(int i, Entity entity) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getHealth() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setHealth(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getMaxHealth() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setMaxHealth(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void resetMaxHealth() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Location getLocation() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Location getLocation(Location location) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setVelocity(Vector vector) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Vector getVelocity() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isOnGround() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public World getWorld() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean teleport(Location location) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause teleportCause) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean teleport(Entity entity) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean teleport(Entity entity, PlayerTeleportEvent.TeleportCause teleportCause) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public List<Entity> getNearbyEntities(double v, double v2, double v3) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getEntityId() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getFireTicks() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getMaxFireTicks() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setFireTicks(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void remove() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isDead() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isValid() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Server getServer() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Entity getPassenger() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean setPassenger(Entity entity) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isEmpty() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean eject() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public float getFallDistance() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setFallDistance(float v) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setLastDamageCause(EntityDamageEvent entityDamageEvent) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public EntityDamageEvent getLastDamageCause() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public UUID getUniqueId() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getTicksLived() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setTicksLived(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void playEffect(EntityEffect entityEffect) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public EntityType getType() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isInsideVehicle() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean leaveVehicle() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Entity getVehicle() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setMetadata(String s, MetadataValue metadataValue) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public List<MetadataValue> getMetadata(String s) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean hasMetadata(String s) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void removeMetadata(String s, Plugin plugin) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isPermissionSet(String s) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isPermissionSet(Permission permission) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean hasPermission(String s) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean hasPermission(Permission permission) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, int i) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void removeAttachment(PermissionAttachment permissionAttachment) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void recalculatePermissions() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Set<PermissionAttachmentInfo> getEffectivePermissions() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isOp() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void setOp(boolean b) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        }, 27);
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin) getPlugin(), new Runnable() {
            public void run() {
                int b = 0;
                for (Game game : games) {
                    ItemStack item = new ItemStack(267, 1);
                    ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.setDisplayName("Arena "+game.getID());
                    ArrayList<String> lore = new ArrayList<String>();
                    lore.add(Game.GetColorPrefix(game.getGameMode()) + "" + game.getGameMode() + " - Players (" + game.getActivePlayers() + "/" + SettingsManager.getInstance().getSpawnCount(game.getID()) + ")");
                    String[] players = getStringList(game.getID()).split("\n");
                    for(String p:players){
                        lore.add(p);
                    }
                    itemMeta.setLore(lore);
                    item.setItemMeta(itemMeta);
                    arenasInventory.setItem((0) + b, item);
                    b++;
                }
            }
        }, 0, 20);

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