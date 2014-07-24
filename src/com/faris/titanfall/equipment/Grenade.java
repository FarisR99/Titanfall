package com.faris.titanfall.equipment;

import com.faris.titanfall.Main;
import com.faris.titanfall.helper.Utils;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author KingFaris10
 */
public class Grenade {
    public static final Map<String, Grenade> grenadeList = new HashMap<>();

    private String grenadeName = "";
    private GrenadeType grenadeType = GrenadeType.NONE;
    private double explosionDamage = 4D;
    private double explosionTime = 1D;
    private double grenadeRadius = 2.5D;
    private double effectDuration = 3D;

    public Grenade(String grenadeName, GrenadeType grenadeType, double damage, double time, double radius, double duration) {
        this.grenadeName = grenadeName;
        this.grenadeType = grenadeType;
        this.explosionDamage = damage;
        this.explosionTime = time;
        this.grenadeRadius = radius * radius;
        this.effectDuration = duration;
    }

    public double getDamage() {
        return this.explosionDamage;
    }

    public double getDuration() {
        return this.effectDuration;
    }

    public double getExplosionTime() {
        return this.explosionTime;
    }

    public ItemStack getItem() {
        return Utils.ItemUtils.setName(new ItemStack(Main.GRENADE_MATERIAL), this.grenadeName + " Grenade");
    }

    public String getName() {
        return this.grenadeName;
    }

    public double getRadius() {
        return this.grenadeRadius;
    }

    public GrenadeType getType() {
        return this.grenadeType;
    }

    public static enum GrenadeType {
        NONE(0), FRAG(1), FLASH(2), STUN(3), INCENDIARY(4);

        private int grenadeID = 0;

        private GrenadeType(int id) {
            this.grenadeID = id;
        }

        public int getID() {
            return this.grenadeID;
        }

        public static GrenadeType getByName(String name) {
            if (name != null) {
                if (name.equalsIgnoreCase("Frag")) return FRAG;
                else if (name.equalsIgnoreCase("Flash")) return FLASH;
                else if (name.equalsIgnoreCase("Stun")) return STUN;
                else if (name.equalsIgnoreCase("Incendiary") || name.equalsIgnoreCase("Fire")) return INCENDIARY;
            }
            return NONE;
        }
    }
}
