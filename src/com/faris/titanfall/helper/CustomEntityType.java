package com.faris.titanfall.helper;

import com.faris.titanfall.entities.CustomIronGolem;
import com.faris.titanfall.equipment.Titan;
import net.minecraft.server.v1_7_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author KingFaris10
 */
public enum CustomEntityType {
    TITAN("Titan", EntityType.IRON_GOLEM.getTypeId(), EntityType.IRON_GOLEM, EntityIronGolem.class, CustomIronGolem.class);

    private final String name;
    private final int id;
    private final EntityType entityType;
    private final Class<? extends EntityInsentient> nmsClass;
    private final Class<? extends EntityInsentient> customClass;

    private CustomEntityType(String name, int id, EntityType entityType, Class<? extends EntityInsentient> nmsClass, Class<? extends EntityInsentient> customClass) {
        this.name = name;
        this.id = id;
        this.entityType = entityType;
        this.nmsClass = nmsClass;
        this.customClass = customClass;
    }

    public String getName() {
        return this.name;
    }

    public int getID() {
        return this.id;
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public Class<? extends EntityInsentient> getNMSClass() {
        return this.nmsClass;
    }

    public Class<? extends EntityInsentient> getCustomClass() {
        return this.customClass;
    }

    public static void registerEntities() {
        for (CustomEntityType entity : values()) {
            try {
                Field c = EntityTypes.class.getDeclaredField("c");
                Field d = EntityTypes.class.getDeclaredField("d");
                Field e = EntityTypes.class.getDeclaredField("e");
                Field f = EntityTypes.class.getDeclaredField("f");
                Field g = EntityTypes.class.getDeclaredField("g");

                c.setAccessible(true);
                d.setAccessible(true);
                e.setAccessible(true);
                f.setAccessible(true);
                g.setAccessible(true);

                Map cMap = (Map) c.get(null);
                Map dMap = (Map) d.get(null);
                Map eMap = (Map) e.get(null);
                Map fMap = (Map) f.get(null);
                Map gMap = (Map) g.get(null);

                cMap.put(entity.getName(), entity.getCustomClass());
                dMap.put(entity.getCustomClass(), entity.getName());
                eMap.put(entity.getID(), entity.getCustomClass());
                fMap.put(entity.getCustomClass(), entity.getID());
                gMap.put(entity.getName(), entity.getID());

                c.set(null, cMap);
                d.set(null, dMap);
                e.set(null, eMap);
                f.set(null, fMap);
                g.set(null, gMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (BiomeBase biomeBase : BiomeBase.n()) {
            if (biomeBase == null) {
                break;
            }

            for (String field : new String[]{"as", "at", "au", "av"}) {
                try {
                    Field list = BiomeBase.class.getDeclaredField(field);
                    list.setAccessible(true);
                    List<BiomeMeta> mobList = (List<BiomeMeta>) list.get(biomeBase);

                    for (BiomeMeta meta : mobList) {
                        for (CustomEntityType entity : values()) {
                            if (entity.getNMSClass().equals(meta.b)) {
                                meta.b = entity.getCustomClass();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static CustomIronGolem spawnIronGolem(Location location, Titan titanType) throws Exception {
        try {
            World mcWorld = ((CraftWorld) location.getWorld()).getHandle();
            CustomIronGolem customEntity = new CustomIronGolem(mcWorld, titanType);
            customEntity.initInventory();
            customEntity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            mcWorld.addEntity(customEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
            return customEntity;
        } catch (Exception ex) {
            throw ex;
        }
    }
}