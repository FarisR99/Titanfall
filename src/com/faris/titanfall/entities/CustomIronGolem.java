package com.faris.titanfall.entities;

import com.faris.titanfall.equipment.Titan;
import com.faris.titanfall.helper.Utils;
import net.minecraft.server.v1_7_R3.EntityHuman;
import net.minecraft.server.v1_7_R3.EntityIronGolem;
import net.minecraft.server.v1_7_R3.EntityLiving;
import net.minecraft.server.v1_7_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.lang.reflect.Field;

/**
 * @author KingFaris10
 */
public class CustomIronGolem extends EntityIronGolem implements InventoryHolder {
    private Titan titanType = null;
    private Inventory ironGolemInventory = null;
    private int ironGolemAmmo = -1;

    public CustomIronGolem(World world, Titan titanType) {
        super(world);
        this.titanType = titanType;
    }

    public void initInventory() {
        if (this.ironGolemInventory == null) {
            this.ironGolemInventory = Bukkit.getServer().createInventory(this, 36, ChatColor.AQUA + "Titan inventory");
        } else {
            this.ironGolemInventory.clear();
            if (this.passenger != null && this.passenger.getBukkitEntity() instanceof Player)
                Utils.updateInventory((Player) this.passenger.getBukkitEntity());
        }
    }

    public int getAmmo() {
        return this.ironGolemAmmo;
    }

    public Titan getTitanType() {
        return this.titanType;
    }

    public void e(float sideMot, float forMot) {
        if (this.passenger == null || !(this.passenger instanceof EntityHuman)) {
            // Make sure the titan can't move if it isn't being ridden on.
            super.e(0F, 0F);
            this.W = 0.5F;    // Make sure the entity can walk over half slabs, instead of jumping
            return;
        }

        this.lastYaw = this.yaw = this.passenger.yaw;
        this.pitch = this.passenger.pitch * 0.5F;

        // Set the entity's pitch, yaw, head rotation etc.
        this.b(this.yaw, this.pitch);
        this.aO = this.aM = this.yaw;

        this.W = 1.0F;    // The custom entity will now automatically climb up 1 high blocks

        sideMot = ((EntityLiving) this.passenger).bd * 0.5F;
        forMot = ((EntityLiving) this.passenger).be;

        if (forMot <= 0.0F) {
            forMot *= 0.5F;    // Make backwards slower
        }
        sideMot *= 0.75F;    // Also make sideways slower

        float speed = 0.25F;    // 0.2 is the default entity speed. I made it slightly faster so that riding is better than walking
        this.i(speed);    // Apply the speed
        super.e(sideMot, forMot);    // Apply the motion to the entity


        Field jump = null;
        try {
            jump = EntityLiving.class.getDeclaredField("bc");
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        if (jump != null && this.onGround) {    // Wouldn't want it jumping while on the ground would we?
            jump.setAccessible(true);
            try {
                if (jump.getBoolean(this.passenger)) {
                    double jumpHeight = 0.75D;
                    this.motY = jumpHeight;    // Used all the time in NMS for entity jumping
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void setAmmo(int ammo) {
        if (ammo < 0) ammo = 0;
        this.ironGolemAmmo = ammo;
    }

    @Override
    public Inventory getInventory() {
        return this.ironGolemInventory;
    }
}