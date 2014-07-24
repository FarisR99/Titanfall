package com.faris.titanfall.equipment;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author KingFaris10
 */
public class PlayerClass {
    public static final Map<String, PlayerClass> classList = new HashMap<>();

    // public ItemStack primaryGun = null, secondaryGun = null, antiTitanGun = null;

    private String className = "";
    private ItemStack[] classContents = null;
    private ItemStack[] classAmmo = null;
    private ItemStack[] classArmour = null;

    private ItemStack fragGrenade = null;
    private ItemStack flashGrenade = null;
    private ItemStack stunGrenade = null;
    private ItemStack incendiaryGrenade = null;

    public PlayerClass(String className, ItemStack[] ammo){  //ItemStack[] guns, ItemStack[] ammo) {
        this.className = className;
        this.classAmmo = ammo;
        //if (guns == null)
            //guns = new ItemStack[]{new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)};
        //this.primaryGun = guns.length > 0 ? guns[0] : new ItemStack(Material.AIR);
        //this.secondaryGun = guns.length > 1 ? guns[1] : new ItemStack(Material.AIR);
        //this.antiTitanGun = guns.length > 2 ? guns[2] : new ItemStack(Material.AIR);
    }

    public ItemStack[] getAmmo() {
        return this.classAmmo;
    }

    public ItemStack[] getArmour() {
        return this.classArmour;
    }

    public ItemStack[] getContents() {
        return this.classContents;
    }

    public ItemStack getGrenade(Grenade.GrenadeType grenadeType) {
        if (grenadeType == Grenade.GrenadeType.FRAG)
            return this.fragGrenade != null ? this.fragGrenade : new ItemStack(Material.AIR);
        else if (grenadeType == Grenade.GrenadeType.FLASH)
            return this.flashGrenade != null ? this.flashGrenade : new ItemStack(Material.AIR);
        else if (grenadeType == Grenade.GrenadeType.STUN)
            return this.stunGrenade != null ? this.stunGrenade : new ItemStack(Material.AIR);
        else if (grenadeType == Grenade.GrenadeType.INCENDIARY)
            return this.incendiaryGrenade != null ? this.incendiaryGrenade : new ItemStack(Material.AIR);
        return new ItemStack(Material.AIR);
    }

    public String getName() {
        return this.className;
    }

    public PlayerClass setArmour(ItemStack[] armourContents) {
        this.classArmour = armourContents;
        return this;
    }

    public PlayerClass setContents(ItemStack[] contents) {
        this.classContents = contents;
        return this;
    }

    public PlayerClass setGrenade(ItemStack grenade, Grenade.GrenadeType grenadeType) {
        if (grenadeType == Grenade.GrenadeType.FRAG) this.fragGrenade = grenade;
        if (grenadeType == Grenade.GrenadeType.FLASH) this.flashGrenade = grenade;
        if (grenadeType == Grenade.GrenadeType.STUN) this.stunGrenade = grenade;
        if (grenadeType == Grenade.GrenadeType.INCENDIARY) this.incendiaryGrenade = grenade;
        return this;
    }

}
