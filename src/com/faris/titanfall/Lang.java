package com.faris.titanfall;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * @author 1Rogue
 * @version 1.0.0
 * @since 1.0.0
 */
public enum Lang {

    LEAVE_NORMAL("Leave.General", "&8[&4-&8] &b%s left the game."), LEAVE_KICKED_MAX_PLAYERS("Leave.Kicked.Max players", "&cThe max players have been reached!"), GENERAL_NOT_ENOUGH_PLAYERS("General.Not enough players", "&aThere are not enough players to start the countdown!"), GENERAL_PLAYER_KILL("General.Kill", "&6You killed &c%s &6and gained &c%s &6points for your team."), GENERAL_PLAYER_DEATH("General.Death", "&6You were killed by &c%s."), GENERAL_CLASS_CHANGED("General.Class.Changed", "&6Your class will change to &4%s &6when you respawn."), GENERAL_CLASS_TITAN_CHANGED("General.Class.Titan.Changed", "&6Your titan class has been changed to &4%s&6."), GAME_WON("Game.Won", "&4%s &6won the game with a score of %s!"), GAME_WON_REWARD("Game.Reward.Won", "&6Your team won and received &c%s &6coins each!"), GAME_LOST_REWARD("Game.Reward.Lost", "&6Your team did not win but received &c%s &6coins each for playing!"), TEAM_FRIENDLY_FIRE("Team.Friendly fire", "&cFriendly fire is not enabled!"), TEAM_CHAT_FORMAT("Team.Chat format", "&8[&4%s&8] &c%s&f: &a%s"), TITAN_CALL_DELAY("Titan.Call delay", "&cYou cannot call your titan at this time!"), TITAN_CANT_SPAWN("Titan.Cannot spawn", "&cYou cannot spawn your Titan here."), TITAN_NAME("Titan.Name", "&b%s's Titan"), COMMAND_GEN_INGAME("Command.General.In game", "&cYou must be a player to use that command!"), COMMAND_GEN_USAGE("Command.General.Usage", "&cUsage: &4/%s");

    private static FileConfiguration langConfig;
    private static final String configName = "lang.yml";

    private final String configPath;
    private final String defaultValue;

    /**
     * {@link Lang} private constructor
     *
     * @param path The configPath to the value
     * @param def The default value
     * @since 1.0.0
     */
    private Lang(String path, String def) {
        this.configPath = path;
        this.defaultValue = def;
    }

    /**
     * Formats a {@link Lang} enum constant with the supplied arguments
     *
     * @param args The arguments to supply
     * @return The formatted string
     * @since 1.0.0
     */
    public String format(Object... args) {
        return Lang.__(String.format(langConfig.getString(this.configPath, this.defaultValue), args));
    }

    public String getMessage() {
        return Lang.__(langConfig.getString(this.configPath, this.defaultValue));
    }

    /**
     * Converts pre-made strings to have chat colors in them
     *
     * @param colour String with unconverted color codes
     * @return string with correct chat colors included
     * @since 1.0.0
     */
    public static String __(String colour) {
        return ChatColor.translateAlternateColorCodes('&', colour);
    }

    /**
     * Loads the lang values from the configuration file. Safe to use for
     * reloading.
     *
     * @since 1.0.0
     */
    public static void init() {
        File ref = new File(Main.getInstance().getDataFolder(), configName.toLowerCase());
        langConfig = YamlConfiguration.loadConfiguration(ref);
        for (Lang l : Lang.values()) {
            if (!langConfig.isSet(l.getPath())) {
                langConfig.set(l.getPath(), l.getDefault());
            }
        }
        try {
            langConfig.save(ref);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sends a formatted string.
     *
     * @param target The target to send to
     * @param message The message to format and send
     * @since 1.0.0
     * @deprecated
     */
    public static void sendMessage(CommandSender target, String message) {
        target.sendMessage(String.format(Main.getInstance().getSettings().getPrefix() + "%s", message));
    }

    /**
     * Sends a raw message without additional formatting aside from translating
     * color codes
     *
     * @param target The target to send to
     * @param message The message to colorize and send
     * @since 1.0.0
     * @deprecated
     */
    public static void sendRawMessage(CommandSender target, String message) {
        target.sendMessage(__(message));
    }

    /**
     * Sends a formatted string.
     *
     * @param target The target to send to
     * @param message The message to colorize and send
     * @param args Arguments to supply to the {@link Lang} message
     * @since 1.0.0
     */
    public static void sendMessage(CommandSender target, Lang message, Object... args) {
        String s = String.format(Main.getInstance().getSettings().getPrefix() + "%s", message.format(args));
        if (!s.isEmpty()) {
            target.sendMessage(s);
        }
    }

    /**
     * Sends a raw message without additional formatting aside from translating
     * color codes
     *
     * @param target The target to send to
     * @param message The message to colorize and send
     * @param args Arguments to supply to the {@link Lang} message
     * @since 1.0.0
     */
    public static void sendRawMessage(CommandSender target, Lang message, Object... args) {
        String s = __(message.format(args));
        if (!s.isEmpty()) {
            target.sendMessage(s);
        }
    }

    /**
     * The YAML configPath to store this value in
     *
     * @return The configPath to the YAML value
     * @since 1.0.0
     */
    private String getPath() {
        return this.configPath;
    }

    /**
     * The default value of this YAML string
     *
     * @return The default value
     * @since 1.0.0
     */
    private String getDefault() {
        return this.defaultValue;
    }
}