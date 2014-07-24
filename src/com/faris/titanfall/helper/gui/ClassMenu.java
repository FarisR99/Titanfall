package com.faris.titanfall.helper.gui;

import com.faris.titanfall.Lang;
import com.faris.titanfall.Main;
import com.faris.titanfall.equipment.PlayerClass;
import com.faris.titanfall.helper.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author KingFaris10
 */
public class ClassMenu implements Listener {
    public static final Map<String, ClassMenu> classMenu = new HashMap<>();

    private String inventoryTitle = "";
    private final Player player;
    private final String playerName;
    private Inventory classInventory = null;

    public ClassMenu(Player player) {
        this.player = player;
        this.playerName = this.player != null ? this.player.getName() : null;
        if (this.player != null) {
            this.inventoryTitle = Utils.replaceChatColour("&a" + this.player.getName() + "'s Class Menu");
            this.classInventory = this.player.getServer().createInventory(this.player, 27, this.inventoryTitle);
            this.player.getServer().getPluginManager().registerEvents(this, Main.getInstance());
        }
    }

    public void openMenu() {
        if (this.player != null) {
            this.classInventory.clear();
            String permissionPrefix = "titanfall.guns.";
            for (PlayerClass gunClass : PlayerClass.classList.values()) {
                String gunName = ChatColor.stripColor(gunClass.getName());
                List<String> gunDescription = new ArrayList<>();
                if (gunClass.getContents().length > 2) {
                    gunDescription.add(ChatColor.DARK_GREEN + "Primary:");
                    ItemStack primaryItem = gunClass.getContents()[0];
                    if (primaryItem != null) {
                        String primaryItemName = Utils.ItemUtils.getName(primaryItem);
                        if (primaryItemName != "") {
                            gunDescription.add(ChatColor.BLUE + ChatColor.stripColor(primaryItemName));
                        } else {
                            gunDescription.add(ChatColor.BLUE + "None");
                        }
                    } else {
                        gunDescription.add(ChatColor.BLUE + "None");
                    }
                    gunDescription.add(ChatColor.DARK_GREEN + "Secondary:");
                    ItemStack secondaryItem = gunClass.getContents()[1];
                    if (secondaryItem != null) {
                        String secondaryItemName = Utils.ItemUtils.getName(secondaryItem);
                        if (secondaryItemName != "") {
                            gunDescription.add(ChatColor.BLUE + ChatColor.stripColor(secondaryItemName));
                        } else {
                            gunDescription.add(ChatColor.BLUE + "None");
                        }
                    } else {
                        gunDescription.add(ChatColor.BLUE + "None");
                    }
                    gunDescription.add(ChatColor.DARK_GREEN + "Anti-Titan:");
                    ItemStack antiTitanItem = gunClass.getContents()[2];
                    if (antiTitanItem != null) {
                        String antiTitanItemName = Utils.ItemUtils.getName(antiTitanItem);
                        if (antiTitanItemName != "") {
                            gunDescription.add(ChatColor.BLUE + ChatColor.stripColor(antiTitanItemName));
                        } else {
                            gunDescription.add(ChatColor.BLUE + "None");
                        }
                    } else {
                        gunDescription.add(ChatColor.BLUE + "None");
                    }
                } else {
                    gunDescription.add(ChatColor.DARK_GREEN + "Primary:");
                    gunDescription.add(ChatColor.BLUE + "None");
                    gunDescription.add(ChatColor.DARK_GREEN + "Secondary:");
                    gunDescription.add(ChatColor.BLUE + "None");
                    gunDescription.add(ChatColor.DARK_GREEN + "Anti-Titan:");
                    gunDescription.add(ChatColor.BLUE + "None");
                }
                this.classInventory.addItem(Utils.ItemUtils.setLores(Utils.ItemUtils.setName(new ItemStack(gunClass.getContents()[0] != null ? gunClass.getContents()[0].getType() : Material.getMaterial(Main.AMMO_ID_ASSAULT)), (player.hasPermission(permissionPrefix + gunName.toLowerCase()) ? "&a" : "&c") + gunName), gunDescription));
            }

            classMenu.put(this.playerName, this);
            this.player.openInventory(this.classInventory);
        }
    }

    public void closeMenu(boolean closeInventory, boolean unregisterEvents) {
        if (unregisterEvents) HandlerList.unregisterAll(this);
        if (closeInventory && this.player != null) this.player.closeInventory();
        classMenu.remove(this.playerName);
    }

    @EventHandler
    public void onPlayerClickItem(InventoryClickEvent event) {
        try {
            if (this.player != null && this.playerName.equals(event.getWhoClicked().getName())) {
                ItemStack clickedItem = event.getCurrentItem();
                event.setCancelled(true);
                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                        String itemName = Utils.ItemUtils.getName(clickedItem);
                        if (itemName != "") {
                            itemName = ChatColor.stripColor(itemName);
                            PlayerClass targetClass = Main.getInstance().getClass(itemName);
                            if (targetClass != null) {
                                if (player.hasPermission(itemName.toLowerCase())) {
                                    Main.getInstance().setClass(this.player, targetClass);
                                    this.closeMenu(true, true);
                                    Lang.sendMessage(player, Lang.GENERAL_CLASS_CHANGED, ChatColor.stripColor(targetClass.getName()));
                                }
                            } else {
                                this.player.sendMessage(ChatColor.RED + "That class does not exist, please choose another class.");
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        try {
            if (this.playerName.equals(event.getPlayer().getName())) this.closeMenu(false, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        try {
            if (this.playerName.equals(event.getPlayer().getName())) this.closeMenu(false, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        try {
            if (this.playerName.equals(event.getPlayer().getName())) this.closeMenu(false, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
