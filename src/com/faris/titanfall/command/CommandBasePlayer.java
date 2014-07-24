package com.faris.titanfall.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author KingFaris10
 */
public abstract class CommandBasePlayer extends CommandBase {

    public boolean onCommand(CommandSender sender, String command, String[] args) {
        if (this.isConsole(sender)) {
            sender.sendMessage("§cYou must be a player to use that command.");
            return true;
        }
        return this.onCommand(this.getPlayer(sender), command, args);
    }

    public abstract boolean onCommand(Player player, String command, String[] args);

}
