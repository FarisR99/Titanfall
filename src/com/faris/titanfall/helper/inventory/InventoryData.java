package com.faris.titanfall.helper.inventory;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author KingFaris10
 */
public class InventoryData {
    protected ItemStack[] inventoryContents = null;

    public InventoryData(ItemStack[] contents) {
        this.inventoryContents = contents == null ? new ItemStack[0] : contents;
    }

    public ItemStack[] getContents() {
        return this.inventoryContents;
    }
}
