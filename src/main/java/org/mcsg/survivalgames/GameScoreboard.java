package org.mcsg.survivalgames;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class GameScoreboard {

    private final int gameID;
    private final Scoreboard scoreboard;
    private Objective sidebarObjective = null;
    private Team waitingTeam = null;
    private Team livingTeam = null;
    private Team deadTeam = null;

    private HashMap<String, Scoreboard> originalScoreboard = new HashMap<String, Scoreboard>();
    private ArrayList<String> activePlayers = new ArrayList<String>();

    /**
     * Class constructor
     *
     * @param gameID	The game id this scoreboard is used within
     */
    public GameScoreboard(int gameID) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();

        this.gameID = gameID;
        this.scoreboard = manager.getNewScoreboard();

        reset();
    }

    /**
     * Reset the scoreboard back to its original empty state
     */
    public void reset() {

        // Remove any players still on the scoreboard
        if (!this.activePlayers.isEmpty()) {
            ArrayList<String> players = new ArrayList<String>();
            for (String playerName : this.activePlayers) {
                players.add(playerName);
            }
            for (String playerName : players) {
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    removePlayer(player);
                }
            }
        }

        // Unregister the objective
        if (this.sidebarObjective != null) {
            this.sidebarObjective.unregister();
            this.sidebarObjective = null;
        }

        // Reset the waiting team
        if (this.waitingTeam != null) {
            this.waitingTeam.unregister();
            this.waitingTeam = null;
        }

        // Reset the living team
        if (this.livingTeam != null) {
            this.livingTeam.unregister();
            this.livingTeam = null;
        }

        // Reset the dead team
        if (this.deadTeam != null) {
            this.deadTeam.unregister();
            this.deadTeam = null;
        }

        // Create the objective
        this.sidebarObjective = this.scoreboard.registerNewObjective("survivalGames-" + this.gameID, "dummy");
        this.sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Create the living team
        this.waitingTeam = this.scoreboard.registerNewTeam("Waiting");
        this.waitingTeam.setAllowFriendlyFire(true);
        this.waitingTeam.setCanSeeFriendlyInvisibles(false);
        this.waitingTeam.setPrefix(ChatColor.WHITE.toString());

        // Create the living team
        this.livingTeam = this.scoreboard.registerNewTeam("Living");
        this.livingTeam.setAllowFriendlyFire(true);
        this.livingTeam.setCanSeeFriendlyInvisibles(false);
        this.livingTeam.setPrefix(ChatColor.GREEN.toString());

        // Create the dead team
        this.deadTeam = this.scoreboard.registerNewTeam("Dead");
        this.deadTeam.setAllowFriendlyFire(true);
        this.deadTeam.setCanSeeFriendlyInvisibles(false);
        this.deadTeam.setPrefix(ChatColor.RED.toString() + ChatColor.STRIKETHROUGH.toString());
    }

    /**
     * Add a player to the scoreboard
     *
     * @param player	The player to add to the scoreboard
     */
    public void addPlayer(final Player player) {

        // Store the current scoreboard for the player
        Scoreboard original = player.getScoreboard();
        if (original != null) {
            this.originalScoreboard.put(player.getName(), original);
        }

        this.activePlayers.add(player.getName());

        // Set the players scoreboard and and them too the team
        player.setScoreboard(this.scoreboard);
        this.waitingTeam.addPlayer(player);

        // Set the players score to zero, then increase it
        Score score = this.sidebarObjective.getScore(player);
        score.setScore(1);

        final Objective sidebarObjective = this.sidebarObjective;
        Bukkit.getScheduler().runTaskLater(GameManager.getInstance().getPlugin(), new Runnable() {
            public void run() {
                sidebarObjective.getScore(player).setScore(0);
            }
        }, 1L);

        updateSidebarTitle();
    }

    /**
     * Remove a player from the scoreboard
     *
     * @param player	The player to remove from the scoreboard
     */
    public void removePlayer(Player player) {

        // remove the player from the team
        this.waitingTeam.removePlayer(player);
        this.livingTeam.removePlayer(player);
        this.deadTeam.removePlayer(player);
        this.scoreboard.resetScores(player);

        // Restore the players scoreboard
        Scoreboard original = this.originalScoreboard.get(player.getName());
        if (original != null) {
            player.setScoreboard(original);
            this.originalScoreboard.remove(player.getName());
        }

        this.activePlayers.remove(player.getName());

        updateSidebarTitle();
    }

    /**
     * Update the title of the sidebar objective
     */
    private void updateSidebarTitle() {
        final int noofPlayers = this.activePlayers.size();
        final int maxPlayers = SettingsManager.getInstance().getSpawnCount(gameID);
        final String gameName = GameManager.getInstance().getGame(gameID).getName();

        this.sidebarObjective.setDisplayName(ChatColor.GOLD + gameName + " (" + noofPlayers + "/" + maxPlayers + ")");
    }

    /**
     * Increase a player's score on the scoreboard
     *
     * @param player	The player to increase the score of
     */
    public void incScore(final Player player) {
        // Set the players score to zero, then increase it
        Score score = this.sidebarObjective.getScore(player);
        if (score != null) {
            score.setScore(score.getScore() + 1);
        }
    }

    public void playerLiving(Player player) {
        this.waitingTeam.removePlayer(player);
        this.deadTeam.removePlayer(player);
        this.livingTeam.addPlayer(player);
    }

    public void playerDead(Player player) {
        this.waitingTeam.removePlayer(player);
        this.livingTeam.removePlayer(player);
        this.deadTeam.addPlayer(player);

        // Restore the players scoreboard
        Scoreboard original = this.originalScoreboard.get(player.getName());
        if (original != null) {
            player.setScoreboard(original);
            this.originalScoreboard.remove(player.getName());
        }
        this.activePlayers.remove(player.getName());
        updateSidebarTitle();
    }
}