package com.faris.titanfall.listener;

import com.faris.titanfall.Game;
import com.faris.titanfall.helper.Team;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author KingFaris10
 */
public class TagListener implements Listener {

    @EventHandler
    public void onPlayerLook(org.kitteh.tag.AsyncPlayerReceiveNameTagEvent event) {
        if (Game.hasStarted()) {
            Team playerTeam = Game.getTeam(event.getNamedPlayer());
            event.setTag(ChatColor.DARK_GRAY + "[" + playerTeam.getColour() + playerTeam.getName() + ChatColor.DARK_GRAY + "] " + ChatColor.RESET + event.getTag());
        }
    }

}
