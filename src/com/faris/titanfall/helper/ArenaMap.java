package com.faris.titanfall.helper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author KingFaris10
 */
public class ArenaMap {
    public static final List<ArenaMap> arenaMaps = new ArrayList<>();

    private UUID mapUUID = null;
    private Location[] spawnLocations = null;

    public ArenaMap(UUID mapUUID, Location[] spawnLocations) {
        this.mapUUID = mapUUID;
        this.spawnLocations = spawnLocations;
        if (this.spawnLocations == null) this.spawnLocations = new Location[2];
        else if (this.spawnLocations.length <= 0 || this.spawnLocations.length > 2)
            this.spawnLocations = new Location[2];
        if (this.spawnLocations[0] == null)
            System.out.println(this.mapUUID.toString() + "'s spawn location for team " + Team.IMC.getName() + " is null!");
        if (this.spawnLocations[1] == null)
            System.out.println(this.mapUUID.toString() + "'s spawn location for team " + Team.MILITIA.getName() + " is null!");
    }

    /**
     * Get the name of the map.
     *
     * @return The name of the map. Returns null if the map does not exist or isn't loaded.
     */
    public String getName() {
        World world = this.getWorld();
        return world != null ? world.getName() : null;
    }

    /**
     * Get the spawn location of the team.
     *
     * @param team - The team.
     * @return The spawn location of the team.
     */
    public Location getSpawn(Team team) {
        if (team == Team.IMC) return this.spawnLocations[0];
        else if (team == Team.MILITIA) return this.spawnLocations[1];
        else return null;
    }

    /**
     * Get the map's UUID.
     *
     * @return The map's UUID.
     */
    public UUID getUUID() {
        return this.mapUUID;
    }

    public World getWorld() {
        return Bukkit.getServer().getWorld(this.mapUUID);
    }

    /**
     * Get a random new map.
     *
     * @param currentMap - The current map.
     * @return A random map.
     */
    public static ArenaMap getNextMap(ArenaMap currentMap) {
        ArenaMap arenaMap = null;
        if (currentMap != null) {
            if (arenaMaps.size() > 1) {
                arenaMap = arenaMaps.get(Utils.getRandom().nextInt(arenaMaps.size()));
                int attempts = 0;
                while (arenaMap == currentMap) {
                    if (attempts < 25) {
                        arenaMap = arenaMaps.get(Utils.getRandom().nextInt(arenaMaps.size()));
                        attempts++;
                    } else {
                        break;
                    }
                }
            } else {
                arenaMap = !arenaMaps.isEmpty() ? arenaMaps.get(0) : null;
            }
        } else {
            if (!arenaMaps.isEmpty())
                arenaMap = arenaMaps.get(Utils.getRandom().nextInt(arenaMaps.size()));
        }
        if (arenaMap == null) {
            if (!arenaMaps.isEmpty()) arenaMaps.get(0);
        }
        return arenaMap;
    }

}
