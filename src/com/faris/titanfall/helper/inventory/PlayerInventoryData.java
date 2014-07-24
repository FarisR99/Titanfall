package com.faris.titanfall.helper.inventory;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author KingFaris10
 */
public class PlayerInventoryData extends InventoryData {
    private ItemStack[] armourContents = null;

    public PlayerInventoryData(ItemStack[] contents, ItemStack[] armour) {
        super(contents);
        this.armourContents = armour;
    }

    public ItemStack[] getArmour() {
        return this.armourContents;
    }
}
