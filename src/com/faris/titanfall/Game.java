package com.faris.titanfall;

import com.faris.titanfall.equipment.Grenade;
import com.faris.titanfall.equipment.PlayerClass;
import com.faris.titanfall.helper.ArenaMap;
import com.faris.titanfall.helper.Team;
import com.faris.titanfall.helper.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

/**
 * @author KingFaris10
 */
@SuppressWarnings("ALL")
public class Game {
    private static boolean gameStarted = false;
    private static ArenaMap gameMap = null;
    private static int gameTask = -1;
    private static int gameTime = 0;

    private static int countdownTask = -1;
    private static int countdownTime = 61;

    private static final Map<String, Team> playerTeams = new HashMap<String, Team>();

    public static void addPlayer(Player player, Team team) {
        if (player != null && team != null) {
            if (team == Team.NONE) playerTeams.remove(player.getName());
            else playerTeams.put(player.getName(), team);
        }
    }

    public static void broadcastMessage(String message) {
        Bukkit.getServer().broadcastMessage(Main.getInstance().getSettings().getPrefix() + Utils.replaceChatColour(message));
    }

    public static void checkScores() {
        if (Team.IMC.getScore() >= Main.getInstance().getSettings().getMaximumPoints()) {
            endGame(Team.IMC);
        } else if (Team.MILITIA.getScore() >= Main.getInstance().getSettings().getMaximumPoints()) {
            endGame(Team.MILITIA);
        }
    }

    public static void endGame(Team winningTeam) {
        boolean hadStarted = gameStarted;
        gameStarted = false;
        if (countdownTask != -1) {
            if (Bukkit.getServer().getScheduler().isQueued(countdownTask) || Bukkit.getServer().getScheduler().isCurrentlyRunning(countdownTask))
                Bukkit.getServer().getScheduler().cancelTask(countdownTask);
            countdownTask = -1;
            countdownTime = Main.getInstance().getSettings().getCountdownTime() + 1;
        }
        if (gameTask != -1) {
            if (Bukkit.getServer().getScheduler().isQueued(gameTask) || Bukkit.getServer().getScheduler().isCurrentlyRunning(gameTask))
                Bukkit.getServer().getScheduler().cancelTask(gameTask);
            gameTask = -1;
            gameTime = -1;
        }
        if (winningTeam == Team.NONE) {
            broadcastMessage("&6It's a draw, stand down!");
        } else if (winningTeam == Team.IMC) {
            broadcastMessage(Lang.GAME_WON.format(Team.IMC.getName(), Team.IMC.getScore()));
        } else if (winningTeam == Team.MILITIA) {
            broadcastMessage(Lang.GAME_WON.format(Team.MILITIA.getName(), Team.MILITIA.getScore()));
        }
        Team.IMC.resetScore();
        Team.MILITIA.resetScore();
        for (Integer taskID : Main.getInstance().getPlayerListener().jumpDelay.values()) {
            if (Bukkit.getServer().getScheduler().isQueued(taskID) || Bukkit.getServer().getScheduler().isCurrentlyRunning(taskID))
                Bukkit.getServer().getScheduler().cancelTask(taskID);
        }
        Main.getInstance().getPlayerListener().jumpDelay.clear();
        int rewardCoins = Main.getInstance().getSettings().getRewardCoins(), contributionCoins = Main.getInstance().getSettings().getContributionCoins();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (Main.getInstance().hasTitan(onlinePlayer)) {
                IronGolem onlinePlayerTitan = Main.getInstance().getTitanEntity(onlinePlayer);
                if (onlinePlayerTitan != null) {
                    if (Utils.inTitan(onlinePlayerTitan, onlinePlayer)) {
                        onlinePlayer.eject();
                        if (Main.getInstance().hasInventory(onlinePlayer)) {
                            Main.getInstance().setInventory(onlinePlayer, null);
                            onlinePlayer.getInventory().setHeldItemSlot(0);
                        }
                    }
                    onlinePlayerTitan.setPassenger(null);
                    onlinePlayerTitan.remove();
                }
            }
            onlinePlayer.getInventory().setHeldItemSlot(0);
            for (PotionEffect potionEffect : onlinePlayer.getActivePotionEffects())
                onlinePlayer.removePotionEffect(potionEffect.getType());
            onlinePlayer.getInventory().clear();
            onlinePlayer.getInventory().setArmorContents(null);
            Utils.resetPlayer(onlinePlayer);
            onlinePlayer.setScoreboard(getScoreboard(onlinePlayer));
            onlinePlayer.setGameMode(GameMode.SURVIVAL);
            onlinePlayer.setAllowFlight(false);
            onlinePlayer.getInventory().setItem(8, Utils.ItemUtils.setName(new ItemStack(Material.COMPASS), "&6Change Class"));
            onlinePlayer.updateInventory();
            onlinePlayer.teleport(Main.getInstance().getSettings().getLobbyLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);

            if (Main.economyEnabled) {
                if (hadStarted) {
                    if (winningTeam != Team.NONE) {
                        Team playerTeam = playerTeams.get(onlinePlayer.getName());
                        if (playerTeam != null) {
                            if (winningTeam.getID() == playerTeam.getID()) {
                                try {
                                    // TODO: Increment coins with rewardCoins
                                    Lang.sendMessage(onlinePlayer, Lang.GAME_WON_REWARD, String.valueOf(rewardCoins));
                                } catch (Exception ex) {
                                    Main.economyEnabled = false;
                                }
                            } else {
                                try {
                                    // TODO: Increment coins with contributionCoins
                                    Lang.sendMessage(onlinePlayer, Lang.GAME_LOST_REWARD, String.valueOf(contributionCoins));
                                } catch (Exception ex) {
                                    Main.economyEnabled = false;
                                }
                            }
                        }
                    } else {
                        try {
                            // TODO: Increment coins with contributionCoins
                            Lang.sendMessage(onlinePlayer, Lang.GAME_LOST_REWARD, String.valueOf(contributionCoins));
                        } catch (Exception ex) {
                            Main.economyEnabled = false;
                        }
                    }
                }
            }
        }
        playerTeams.clear();
        Main.getInstance().clearClasses();
        Main.getInstance().clearInventories();
        Main.getInstance().clearTitanClasses();
        Main.getInstance().clearTitanPlayers();

        if (gameMap != null) {
            World gameWorld = gameMap.getWorld();
            for (LivingEntity livingEntity : gameWorld.getLivingEntities()) {
                if (livingEntity.getType() != EntityType.PLAYER) livingEntity.remove();
            }
            for (Projectile projectile : gameWorld.getEntitiesByClass(Projectile.class)) {
                projectile.remove();
            }
            for (Item item : gameWorld.getEntitiesByClass(Item.class)) {
                item.remove();
            }
        }
    }

    public static ArenaMap getMap() {
        return gameMap;
    }

    public static List<Player> getPlayers() {
        List<Player> playerList = new ArrayList<Player>();
        Set<String> usernameList = playerTeams.keySet();
        for (String username : usernameList) {
            Player player = Bukkit.getPlayerExact(username);
            if (player != null && player.isOnline()) playerList.add(player);
        }
        return playerList;
    }

    public static List<Player> getPlayers(Team team) {
        List<Player> playerList = new ArrayList<Player>();
        List<String> usernameList = getUsernames(team);
        for (String username : usernameList) {
            Player player = Bukkit.getPlayerExact(username);
            if (player != null && player.isOnline()) playerList.add(player);
        }
        return playerList;
    }

    public static int getSize() {
        return playerTeams.size();
    }

    public static Scoreboard getScoreboard(Player player) {
        Scoreboard newScoreboard = player.getServer().getScoreboardManager().getNewScoreboard();
        Objective objective = newScoreboard.registerNewObjective("titanfall", "dummy");
        String displayName = null;
        if (hasStarted()) {
            displayName = ChatColor.AQUA.toString() + formatIntoHHMMSS(Main.getInstance().getSettings().getMaximumTime() - gameTime);
        } else if (isStarting()) {
            displayName = ChatColor.YELLOW.toString() + formatIntoHHMMSS(countdownTime);
        } else {
            displayName = ChatColor.GOLD.toString() + Main.getInstance().getSettings().getPluginName();
        }
        objective.setDisplayName(displayName);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        int scorePosition = 6;
        objective.getScore("============").setScore(scorePosition);
        scorePosition--;
        objective.getScore(ChatColor.GREEN + Team.IMC.getName() + ": " + ChatColor.DARK_RED + Team.IMC.getScore()).setScore(scorePosition);
        scorePosition--;
        objective.getScore(ChatColor.GREEN + Team.MILITIA.getName() + ": " + ChatColor.DARK_RED + Team.MILITIA.getScore()).setScore(scorePosition);
        scorePosition--;
        objective.getScore(ChatColor.WHITE.toString() + "============").setScore(scorePosition);
        scorePosition--;
        objective.getScore(ChatColor.GREEN + "Players: " + ChatColor.DARK_RED + player.getServer().getOnlinePlayers().length).setScore(scorePosition);
        scorePosition--;
        objective.getScore(ChatColor.RESET.toString() + "============").setScore(scorePosition);
        return newScoreboard;
    }

    public static Team getTeam(Player player) {
        Validate.notNull(player);
        return playerTeams.get(player.getName());
    }

    public static Map<String, Team> getTeams() {
        return Collections.unmodifiableMap(playerTeams);
    }

    public static List<Team> getTeamsLeft() {
        List<Team> teamList = new ArrayList<Team>();
        if (getUsernames(Team.IMC).isEmpty()) teamList.add(Team.IMC);
        if (getUsernames(Team.MILITIA).isEmpty()) teamList.add(Team.MILITIA);
        return teamList;
    }

    public static int getTime() {
        return gameTime;
    }

    public static List<String> getUsernames(Team team) {
        List<String> usernameList = new ArrayList<String>();
        if (team != null) {
            for (Map.Entry<String, Team> playerEntry : playerTeams.entrySet()) {
                if (playerEntry.getValue().getID() == team.getID()) usernameList.add(playerEntry.getKey());
            }
        }
        return usernameList;
    }

    public static Team getWinnerTeam() {
        if (Team.IMC.getScore() > Team.MILITIA.getScore()) return Team.IMC;
        else if (Team.MILITIA.getScore() > Team.IMC.getScore()) return Team.MILITIA;
        else return Team.NONE;
    }

    public static boolean hasStarted() {
        return gameStarted;
    }

    public static boolean isStarting() {
        return countdownTask != -1;
    }

    public static void removePlayer(Player player) {
        if (player != null) {
            playerTeams.remove(player.getName());
        }
    }

    public static void startGame() {
        playerTeams.clear();
        gameMap = ArenaMap.getNextMap(gameMap);
        countdownTime = Main.getInstance().getSettings().getCountdownTime() + 1;
        countdownTask = Bukkit.getServer().getScheduler().runTaskTimer(Main.getInstance(), new Runnable() {
            public void run() {
                countdownTime--;
                if (countdownTime <= 0) {
                    gameStarted = true;
                    Bukkit.getServer().getScheduler().cancelTask(countdownTask);
                    countdownTime = Main.getInstance().getSettings().getCountdownTime() + 1;
                    broadcastMessage("The game has started!");
                    playerTeams.clear();
                    if (gameMap == null) gameMap = ArenaMap.getNextMap(null);
                    if (gameMap != null) {
                        int teamAmount1 = 0, teamAmount2 = 0;
                        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                            if (Math.random() > 0.5) {
                                if (teamAmount2 > teamAmount1) {
                                    playerTeams.put(onlinePlayer.getName(), Team.IMC);
                                    teamAmount1++;
                                } else {
                                    playerTeams.put(onlinePlayer.getName(), Team.MILITIA);
                                    teamAmount2++;
                                }
                            } else {
                                if (teamAmount1 > teamAmount2) {
                                    playerTeams.put(onlinePlayer.getName(), Team.MILITIA);
                                    teamAmount2++;
                                } else {
                                    playerTeams.put(onlinePlayer.getName(), Team.IMC);
                                    teamAmount1++;
                                }
                            }
                        }
                        Location imcSpawn = gameMap.getSpawn(Team.IMC);
                        Location militiaSpawn = gameMap.getSpawn(Team.MILITIA);
                        PlayerClass defaultClass = Main.getInstance().getClass(Main.DEFAULT_CLASS);
                        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                            Team playerTeam = getTeam(player);
                            if (playerTeam == Team.IMC) {
                                if (imcSpawn != null) {
                                    player.teleport(imcSpawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
                                }
                            } else if (playerTeam == Team.MILITIA) {
                                if (militiaSpawn != null) {
                                    player.teleport(militiaSpawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
                                }
                            }
                            if (!Main.getInstance().hasClass(player))
                                Main.getInstance().setClass(player, defaultClass);
                            player.getInventory().setHeldItemSlot(0);
                            player.getInventory().clear();
                            player.getInventory().setArmorContents(null);
                            for (PotionEffect potionEffect : player.getActivePotionEffects())
                                player.removePotionEffect(potionEffect.getType());
                            player.setScoreboard(getScoreboard(player));
                            player.setGameMode(GameMode.ADVENTURE);
                            Utils.resetPlayer(player);
                            player.setAllowFlight(true);

                            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));

                            PlayerClass playerClass = Main.getInstance().getClass(player);
                            if (playerClass == null) {
                                Main.getInstance().setClass(player, defaultClass);
                                playerClass = Main.getInstance().getClass(player);
                            }
                            player.getInventory().setContents(playerClass.getContents());
                            player.getInventory().setArmorContents(playerClass.getArmour());
                            player.setLevel(0);

                            player.getInventory().setItem(3, playerClass.getGrenade(Grenade.GrenadeType.FRAG));
                            player.getInventory().setItem(4, playerClass.getGrenade(Grenade.GrenadeType.FLASH));
                            player.getInventory().setItem(5, playerClass.getGrenade(Grenade.GrenadeType.STUN));
                            player.getInventory().setItem(6, playerClass.getGrenade(Grenade.GrenadeType.INCENDIARY));
                            player.getInventory().setItem(7, Utils.ItemUtils.setName(new ItemStack(Material.NETHER_STAR), "&aSpawn Titan"));
                            player.getInventory().setItem(8, Utils.ItemUtils.setName(new ItemStack(Material.COMPASS), "&6Change Class"));
                            player.getInventory().addItem(playerClass.getAmmo());

                            player.updateInventory();

                            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1F, 1F);
                        }
                        gameTime = -1;
                        gameTask = Bukkit.getServer().getScheduler().runTaskTimer(Main.getInstance(), new Runnable() {
                            public void run() {
                                gameTime++;
                                updateScoreboards();
                                int timeLeft = Main.getInstance().getSettings().getMaximumTime() - gameTime;
                                if (timeLeft <= 0) {
                                    endGame(getWinnerTeam());
                                } else {
                                    if (gameTime % 60 == 0) {
                                        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                                            onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 200, 1));
                                        }
                                    }
                                }
                            }
                        }, 0L, 20L).getTaskId();
                    } else {
                        broadcastMessage("&cCould not locate the game map!");
                        endGame(null);
                    }
                } else {
                    if (countdownTime == 60) {
                        broadcastMessage("Starting in 1 minute!");
                    } else if (countdownTime == 30) {
                        broadcastMessage("Starting in 30 seconds. Have you chosen a class?");
                    } else if (countdownTime == 10) {
                        broadcastMessage("Starting in 10 seconds. Make sure you've chosen a class!");
                    } else if (countdownTime > 1 && countdownTime <= 5) {
                        broadcastMessage("Starting in " + countdownTime + " seconds...");
                    } else if (countdownTime == 1) {
                        broadcastMessage("Starting in 1 second!");
                    }
                    for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                        onlinePlayer.setScoreboard(getScoreboard(onlinePlayer));
                        if (countdownTime <= 5)
                            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ITEM_PICKUP, 1F, 1F);
                    }
                }
            }
        }, 0L, 20L).getTaskId();
    }

    public static void updateScoreboards() {
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers())
            onlinePlayer.setScoreboard(getScoreboard(onlinePlayer));
    }

    private static String formatIntoHHMMSS(int secsIn) {
        int hours = secsIn / 3600, remainder = secsIn % 3600, minutes = remainder / 60, seconds = remainder % 60;
        return ((hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds);
    }

}
