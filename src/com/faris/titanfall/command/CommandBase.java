package com.faris.titanfall.command;

import com.faris.titanfall.Lang;
import com.faris.titanfall.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author KingFaris10
 */
public abstract class CommandBase implements CommandExecutor, Iterable<String> {
    private static final Map<String, CommandBase> commandList = new HashMap<String, CommandBase>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            boolean containsAlias = false;
            for (String alias : this.getAliases()) {
                if (alias.equalsIgnoreCase(label)) containsAlias = true;
            }
            if (!this.getCommandNames().contains(label.toLowerCase()) && !containsAlias)
                return false;
            return this.onCommand(sender, label.toLowerCase(), args);
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "A(n) " + ex.getClass().getSimpleName() + " error occurred.");
            ex.printStackTrace();
            return true;
        }
    }


    public abstract boolean onCommand(CommandSender sender, String command, String[] args);

    protected boolean isConsole(CommandSender sender) {
        return !(sender instanceof Player);
    }

    public abstract List<String> getCommandNames();

    public List<String> getAliases() {
        return new ArrayList<String>();
    }

    @Override
    public Iterator<String> iterator() {
        return this.getCommandNames().listIterator();
    }

    protected Player getPlayer(CommandSender sender) {
        return (Player) sender;
    }

    protected Main getPlugin() {
        return Main.getInstance();
    }

    protected String getUsageMessage(String usage) {
        return Lang.COMMAND_GEN_USAGE.format(usage);
    }

    public static void clearCommands() {
        commandList.clear();
    }

    public static Collection<CommandBase> getCommands() {
        return commandList.values();
    }

    public static Map<String, CommandBase> getRegisteredCommands() {
        return Collections.unmodifiableMap(commandList);
    }

    public static void registerCommands(CommandBase commandBase, String... commands) {
        if (commands != null && commandBase != null) {
            for (String command : commands) {
                if (command != null) commandList.put(command.toLowerCase().replace(" ", ""), commandBase);
            }
        }
    }

    public static void unregisterCommand(String command) {
        if (command != null) commandList.remove(command.toLowerCase().replace(" ", ""));
    }
}
