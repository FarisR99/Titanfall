package com.faris.titanfall.command.commands;

import com.faris.titanfall.command.CommandBase;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

/**
 * @author KingFaris10
 */
public class CommandTitanfall extends CommandBase {

    public boolean onCommand(CommandSender sender, String label, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "Titanfall by KingFaris10");
        return true;
    }

    @Override
    public List<String> getCommandNames() {
        return Arrays.asList("titanfall");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("tf, titanfalls, tfs");
    }

}
