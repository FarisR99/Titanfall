package com.faris.titanfall.command.commands;

import com.faris.titanfall.Game;
import com.faris.titanfall.Lang;
import com.faris.titanfall.command.CommandBasePlayer;
import com.faris.titanfall.helper.Team;
import com.faris.titanfall.helper.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * @author KingFaris10
 */
public class CommandTeamChat extends CommandBasePlayer {

    @Override
    public boolean onCommand(Player player, String command, String[] args) {
        if (args.length > 0) {
            if (Game.hasStarted()) {
                Team playerTeam = Game.getTeam(player);
                if (playerTeam != Team.NONE) {
                    StringBuilder sbChatMessage = new StringBuilder();
                    for (String arg : args) sbChatMessage.append(arg + " ");

                    String chatMessage = sbChatMessage.toString().trim();
                    if (player.hasPermission("essentials.chat.color")) chatMessage = Utils.replaceColours(chatMessage);
                    else chatMessage = Utils.stripColours(chatMessage);
                    if (player.hasPermission("essentials.chat.format")) chatMessage = Utils.replaceFormat(chatMessage);
                    else chatMessage = Utils.stripFormat(chatMessage);

                    if (chatMessage == null) chatMessage = "";
                    List<Player> playerList = Game.getPlayers(playerTeam);
                    for (Player teamPlayer : playerList) {
                        Lang.sendMessage(teamPlayer, Lang.TEAM_CHAT_FORMAT, playerTeam.getName(), player.getName(), chatMessage);
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You are not in a team!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "The game hasn't started yet!");
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "TeamChat by KingFaris10.");
            Lang.sendMessage(player, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " <message>");
        }
        return true;
    }

    @Override
    public List<String> getCommandNames() {
        return Arrays.asList("teamchat");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("tc");
    }
}
