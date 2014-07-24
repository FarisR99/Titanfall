package com.faris.titanfall.equipment;

import com.faris.titanfall.helper.Utils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author KingFaris10
 */
public class Titan {
    public static final Map<String, Titan> titanList = new HashMap<>();

    private String titanName = "", titanTag = "";
    private ItemStack primaryGun = null;
    private TitanSpecial specialType = null;
    private TitanMode titanMode = null;
    private List<PotionEffect> potionEffects = new ArrayList<>();
    private int primaryAmmo = 250;
    private double titanHealth = 40D;

    public Titan(String titanName, String titanTag, double titanHealth, ItemStack primaryGun) {
        this.titanName = titanName;
        this.titanTag = Utils.replaceChatColour(titanTag);
        this.titanHealth = titanHealth;
        this.primaryGun = primaryGun;
    }

    public int getAmmo() {
        return this.primaryAmmo;
    }

    public ItemStack[] getAmmoItems() {
        ItemStack[] titanGunAmmo = new ItemStack[((int) (this.primaryAmmo / 64)) + 1];
        for (int i = 0; i < this.primaryAmmo; i++) {
            int index = (int) i / 64;
            ItemStack titanGunItem = titanGunAmmo[index];
            if (titanGunItem == null)
                titanGunItem = Utils.ItemUtils.setName(new ItemStack(Material.ARROW), "&6Primary Ammo");
            else titanGunItem.setAmount(titanGunItem.getAmount() + 1);
            titanGunAmmo[index] = titanGunItem;
        }
        return titanGunAmmo;
    }

    public double getHealth() {
        return this.titanHealth;
    }

    public TitanMode getMode() {
        return this.titanMode;
    }

    public String getName() {
        return this.titanName;
    }

    public List<PotionEffect> getPotionEffects() {
        return this.potionEffects;
    }

    public ItemStack getPrimaryGun() {
        return this.primaryGun;
    }

    public TitanSpecial getSpecial() {
        return this.specialType;
    }

    public String getTag() {
        return this.titanTag;
    }

    public Titan setPotionEffects(List<PotionEffect> potions) {
        this.potionEffects = potions;
        return this;
    }

    public Titan setTitanMode(TitanMode titanMode) {
        this.titanMode = titanMode;
        return this;
    }

    public Titan setTitanSpecial(TitanSpecial titanSpecial) {
        this.specialType = titanSpecial;
        return this;
    }

    public static enum TitanSpecial {
        NONE, AUTO_ROCKETS, LASER;

        public String toString() {
            return WordUtils.capitalizeFully(this.name().toLowerCase().replaceAll("_", " "));
        }

        public static TitanSpecial getSpecial(String strSpecial) {
            if (strSpecial != null) {
                if (strSpecial.replace(" ", "").equalsIgnoreCase("AutoRocket") || strSpecial.equalsIgnoreCase("Auto_Rocket") || strSpecial.equalsIgnoreCase("Auto-Rocket"))
                    return AUTO_ROCKETS;
                else if (strSpecial.replace(" ", "").equalsIgnoreCase("Laser")) return LASER;
            }
            return NONE;
        }
    }

    public static enum TitanMode {
        NONE, AUTO_EJECT, AUTO_EXPLODE;

        public String toString() {
            return WordUtils.capitalizeFully(this.name().toLowerCase().replaceAll("_", " "));
        }

        public static TitanMode getMode(String strMode) {
            if (strMode != null) {
                if (strMode.replace(" ", "").equalsIgnoreCase("AutoEject") || strMode.equalsIgnoreCase("Auto_Eject") || strMode.equalsIgnoreCase("Auto-Eject"))
                    return AUTO_EJECT;
                else if (strMode.replace(" ", "").equalsIgnoreCase("AutoExplode") || strMode.equalsIgnoreCase("Auto_Explode") || strMode.equalsIgnoreCase("Auto-Rocket"))
                    return AUTO_EXPLODE;
            }
            return NONE;
        }
    }
}
