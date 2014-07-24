package com.faris.titanfall.helper.gui;

import com.faris.titanfall.Lang;
import com.faris.titanfall.Main;
import com.faris.titanfall.equipment.Titan;
import com.faris.titanfall.helper.Utils;
import org.apache.commons.lang.WordUtils;
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
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author KingFaris10
 */
public class TitanMenu implements Listener {
    public static final Map<String, TitanMenu> titanMenu = new HashMap<>();

    private String inventoryTitle = "";
    private final Player player;
    private final String playerName;
    private Inventory titanInventory = null;

    public TitanMenu(Player player) {
        this.player = player;
        this.playerName = this.player != null ? this.player.getName() : null;
        if (this.player != null) {
            this.inventoryTitle = Utils.replaceChatColour("&a" + this.player.getName() + "'s Titan Menu");
            this.titanInventory = this.player.getServer().createInventory(this.player, 9, this.inventoryTitle);
        }
    }

    public void openMenu() {
        if (this.player != null && !titanMenu.containsValue(this.player.getName())) {
            this.titanInventory.clear();
            String permissionPrefix = "titanfall.titans.";
            for (Titan titan : Titan.titanList.values()) {
                String titanName = ChatColor.stripColor(titan.getName());
                List<String> titanDescription = new ArrayList<>();
                titanDescription.add(ChatColor.DARK_GREEN + "Maximum Health:");
                titanDescription.add(ChatColor.BLUE + String.valueOf(titan.getHealth()));
                titanDescription.add(ChatColor.DARK_GREEN + "Primary:");
                if (titan.getPrimaryGun() != null) {
                    ItemStack gunItem = titan.getPrimaryGun();
                    if (gunItem != null) {
                        String primaryItemName = Utils.ItemUtils.getName(gunItem);
                        if (primaryItemName != "") {
                            titanDescription.add(ChatColor.BLUE + ChatColor.stripColor(primaryItemName));
                        } else {
                            titanDescription.add(ChatColor.BLUE + "None");
                        }
                    } else {
                        titanDescription.add(ChatColor.BLUE + "None");
                    }
                }
                titanDescription.add(ChatColor.DARK_GREEN + "Potion Effects:");
                for (PotionEffect potionEffect : titan.getPotionEffects()) {
                    if (potionEffect != null)
                        titanDescription.add(WordUtils.capitalizeFully(potionEffect.getType().toString().toLowerCase().replace("_", " ")) + ":" + potionEffect.getAmplifier());
                }
                this.titanInventory.addItem(Utils.ItemUtils.setLores(Utils.ItemUtils.setName(titan.getPrimaryGun(), (player.hasPermission(permissionPrefix + titanName.toLowerCase()) ? "&a" : "&c") + titanName), titanDescription));
            }
            HandlerList.unregisterAll(this);
            this.player.getServer().getPluginManager().registerEvents(this, Main.getInstance());
            titanMenu.put(this.playerName, this);
            this.player.openInventory(this.titanInventory);
        }
    }

    public void closeMenu(boolean closeInventory, boolean unregisterEvents) {
        if (unregisterEvents) HandlerList.unregisterAll(this);
        if (closeInventory && this.player != null) this.player.closeInventory();
        titanMenu.remove(this.playerName);
    }

    @EventHandler
    public void onPlayerClickItem(InventoryClickEvent event) {
        try {
            if (this.player != null && this.playerName.equals(event.getWhoClicked().getName())) {
                ItemStack clickedItem = event.getCurrentItem();
                event.setCancelled(true);
                if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                    if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                        String itemName = ChatColor.stripColor(Utils.ItemUtils.getName(clickedItem));
                        if (itemName != "") {
                            Titan targetClass = Main.getInstance().getTitanClass(itemName);
                            if (targetClass != null) {
                                if (this.player.hasPermission(itemName.toLowerCase())) {
                                    this.closeMenu(true, true);
                                    Main.getInstance().setTitanClass(this.player, targetClass);
                                    Lang.sendMessage(this.player, Lang.GENERAL_CLASS_TITAN_CHANGED, ChatColor.stripColor(targetClass.getName()));
                                } else {
                                    this.player.sendMessage(ChatColor.DARK_RED + "You do not have permission to use that titan.");
                                }
                            } else {
                                this.player.sendMessage(ChatColor.RED + "That titan class does not exist, please choose another titan class.");
                            }
                        } else {
                            this.player.sendMessage(ChatColor.RED + "That titan class does not exist, please choose another titan class.");
                        }
                    } else {
                        this.player.sendMessage(ChatColor.RED + "Please choose a titan class from the class inventory.");
                    }
                } else {
                    this.player.sendMessage(ChatColor.RED + "Please choose a titan class from the class inventory.");
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
