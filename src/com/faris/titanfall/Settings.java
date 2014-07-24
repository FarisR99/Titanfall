package com.faris.titanfall;

import com.faris.titanfall.helper.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;

/**
 * @author KingFaris10
 */
public class Settings {
    private String pluginPrefix = ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + "Titanfall" + ChatColor.GRAY + "] " + ChatColor.YELLOW;
    private String pluginName = "Minefall";
    private String hubServerName = "Lobby";
    private int rewardCoins = 20;
    private int contributionCoins;

    private int maximumTime = 1200;
    private int minimumPlayers = 4;
    private int maximumPlayers = 30;
    private int countdownTime = 60;
    private int invincibilityTime = 15;
    private int maximumPoints = 50;
    private int killPoints = 1;

    private double doubleJumpDelay = 2F;
    private double doubleJumpVelocity = 1.2F;
    private double doubleJumpYHeight = 1.5F;

    private int titanGameDelay = 60;
    private int titanCallDelay = 90;
    private float titanSpawnDelay = 3F;

    private double[] smartPistolRadius = new double[3];
    private double grenadeCooldown = 5;

    private Location lobbyLocation = null;

    public int getContributionCoins() {
        return this.contributionCoins;
    }

    public int getCountdownTime() {
        return this.countdownTime;
    }

    public double getDoubleJumpDelay() {
        return this.doubleJumpDelay;
    }

    public double getDoubleJumpVelocity() {
        return this.doubleJumpVelocity;
    }

    public double getDoubleJumpYHeight() {
        return this.doubleJumpYHeight;
    }

    public double getGrenadeCooldown() {
        return this.grenadeCooldown;
    }

    public String getHub() {
        return this.hubServerName;
    }

    public int getInvincibilityTime() {
        return this.invincibilityTime;
    }

    public int getKillPoints() {
        return this.killPoints;
    }

    public Location getLobbyLocation() {
        return this.lobbyLocation;
    }

    public int getMaximumPlayers() {
        return this.maximumPlayers;
    }

    public int getMaximumPoints() {
        return this.maximumPoints;
    }

    public int getMaximumTime() {
        return this.maximumTime;
    }

    public int getMinimumPlayers() {
        return this.minimumPlayers;
    }

    public String getPluginName() {
        return this.pluginName;
    }

    public String getPrefix() {
        return this.pluginPrefix;
    }

    public int getRewardCoins() {
        return this.rewardCoins;
    }

    public double[] getSmartPistolRadius() {
        return this.smartPistolRadius;
    }

    public int getTitanGameDelay() {
        return this.titanGameDelay;
    }

    public int getTitanCallDelay() {
        return this.titanCallDelay;
    }

    public float getTitanSpawnDelay() {
        return this.titanSpawnDelay;
    }

    public void setContributionCoins(int contributionCoins) {
        this.contributionCoins = contributionCoins;
    }

    public void setCountdownTime(int countdownTime) {
        this.countdownTime = countdownTime;
    }

    public void setDoubleJumpDelay(float delay) {
        this.doubleJumpDelay = delay;
    }

    public void setDoubleJumpVelocity(float velocity) {
        this.doubleJumpVelocity = velocity;
    }

    public void setDoubleJumpYHeight(float yHeight) {
        this.doubleJumpYHeight = yHeight;
    }

    public void setGrenadeCooldown(double cooldown) {
        this.grenadeCooldown = cooldown;
    }

    public void setHub(String serverName) {
        this.hubServerName = serverName;
    }

    public void setInvincibilityTime(int invincibilityTime) {
        this.invincibilityTime = invincibilityTime;
    }

    public void setKillPoints(int killPoints) {
        this.killPoints = killPoints;
    }

    public void setLobbyLocation(Location lobbyLocation) {
        this.lobbyLocation = lobbyLocation;
    }

    public void setMaximumPlayers(int maximumPlayers) {
        this.maximumPlayers = maximumPlayers;
    }

    public void setMaximumPoints(int maximumPoints) {
        this.maximumPoints = maximumPoints;
    }

    public void setMaximumTime(int maximumTime) {
        this.maximumTime = maximumTime;
    }

    public void setMinimumPlayers(int minimumPlayers) {
        this.minimumPlayers = minimumPlayers;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public void setPrefix(String prefix) {
        this.pluginPrefix = Utils.replaceChatColour(prefix);
    }

    public void setRewardCoins(int rewardCoins) {
        this.rewardCoins = rewardCoins;
    }

    public void setSmartPistolRadius(double xRadius, double yRadius, double zRadius) {
        this.smartPistolRadius = new double[3];
        this.smartPistolRadius[0] = xRadius;
        this.smartPistolRadius[1] = yRadius;
        this.smartPistolRadius[2] = zRadius;
    }

    public void setTitanGameDelay(int delay) {
        this.titanGameDelay = delay;
    }

    public void setTitanCallDelay(int delay) {
        this.titanCallDelay = delay;
    }

    public void setTitanSpawnDelay(float delay) {
        this.titanSpawnDelay = delay;
    }

}
