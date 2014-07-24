package com.faris.titanfall;

import com.faris.titanfall.command.CommandBase;
import com.faris.titanfall.command.commands.CommandTeamChat;
import com.faris.titanfall.command.commands.CommandTitanfall;
import com.faris.titanfall.entities.CustomIronGolem;
import com.faris.titanfall.equipment.Grenade;
import com.faris.titanfall.equipment.PlayerClass;
import com.faris.titanfall.equipment.Titan;
import com.faris.titanfall.helper.ArenaMap;
import com.faris.titanfall.helper.CustomEntityType;
import com.faris.titanfall.helper.Team;
import com.faris.titanfall.helper.Utils;
import com.faris.titanfall.helper.inventory.PlayerInventoryData;
import com.faris.titanfall.listener.EventListener;
import com.faris.titanfall.listener.PlayerListener;
import com.faris.titanfall.listener.TagListener;
import com.shampaggon.crackshot.CSUtility;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftIronGolem;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class Main extends JavaPlugin {
    private static Main pluginInstance = null;
    private static Scoreboard plainScoreboard = null;
    public static boolean pluginDisabled = false;
    public static boolean economyEnabled = false;
    private static int pluginDisabledTask = -1;
    public static final Material GRENADE_MATERIAL = Material.EGG;
    public static final int AMMO_ID_ASSAULT = 370;
    public static final int AMMO_ID_MACHINE = AMMO_ID_ASSAULT + 1;
    public static final int AMMO_ID_SNIPER = AMMO_ID_MACHINE + 1;
    public static final int AMMO_ID_SHOTGUN = AMMO_ID_SNIPER + 1;
    public static final int AMMO_ID_PISTOL = AMMO_ID_SHOTGUN + 1;
    public static final int AMMO_ID_ROCKET = AMMO_ID_PISTOL + 1;
    public static final int AMMO_ID_ANTITITAN = AMMO_ID_ROCKET + 1;

    public static String DEFAULT_CLASS = "Assault";

    private CSUtility csUtility = null;
    private Chat vaultChat = null;
    private Settings pluginSettings = null;
    private EventListener eventListener = null;
    private PlayerListener playerListener = null;

    private final Map<String, PlayerClass> playerClass = new HashMap<>();
    private final Map<String, Titan> playerTitanClass = new HashMap<>();
    private final Map<String, UUID> playerGolems = new HashMap<>();
    private final Map<String, PlayerInventoryData> playerInventories = new HashMap<>();
    private final List<String> playersInGolem = new ArrayList<>();
    private com.shampaggon.crackshot.CSUtility CSUtility;

    @Override
    public void onEnable() {
        /** Set up static variables **/
        pluginInstance = this;
        plainScoreboard = this.getServer().getScoreboardManager().getNewScoreboard();
        Permissions.init();
        Lang.init();
        CustomEntityType.registerEntities();

        /** Initialise BungeeCord **/
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        /** Initialise variables **/
        this.pluginSettings = new Settings();
        try {
            this.csUtility = new CSUtility();
            this.loadConfiguration();
        } catch (Exception ex) {
            pluginDisabled = true;
            ex.printStackTrace();
            for (Player onlinePlayer : this.getServer().getOnlinePlayers()) {
                Utils.sendPlayerToHub(onlinePlayer, ChatColor.RED + "There was an error while loading the Titanfall plugin! Please try again in 5 minutes.");
            }
            pluginDisabledTask = this.getServer().getScheduler().runTaskTimer(this, new Runnable() {
                public void run() {
                    try {
                        loadConfiguration();
                        pluginDisabled = false;
                        if (pluginDisabledTask != -1) Bukkit.getServer().getScheduler().cancelTask(pluginDisabledTask);
                        Game.updateScoreboards();
                        if (!Game.hasStarted() && !Game.isStarting()) {
                            if (getServer().getOnlinePlayers().length >= getSettings().getMinimumPlayers()) {
                                Game.startGame();
                            }
                        }

                        getServer().getScheduler().runTaskTimer(pluginInstance, new Runnable() {
                            public void run() {
                                if (!Game.hasStarted() && !Game.isStarting() && Bukkit.getServer().getOnlinePlayers().length >= pluginSettings.getMinimumPlayers())
                                    Game.startGame();
                            }
                        }, 20L, 20L);
                    } catch (Exception ex) {
                        pluginDisabled = true;
                    }
                }
            }, 6000L, 6000L).getTaskId();
        }
        this.setupChat();

        for (Permission permission : Permissions.getPermissions()) {
            this.getServer().getPluginManager().addPermission(permission);
        }
        this.eventListener = new EventListener();
        this.playerListener = new PlayerListener();
        this.getServer().getPluginManager().registerEvents(this.eventListener, this);
        this.getServer().getPluginManager().registerEvents(this.playerListener, this);
        if (this.getServer().getPluginManager().isPluginEnabled("TagAPI"))
            this.getServer().getPluginManager().registerEvents(new TagListener(), this);

        this.registerCommands();

        for (Player onlinePlayer : this.getServer().getOnlinePlayers()) {
            onlinePlayer.setAllowFlight(false);
        }
        if (!pluginDisabled) {
            Game.updateScoreboards();
            if (!Game.hasStarted() && !Game.isStarting()) {
                if (this.getServer().getOnlinePlayers().length >= this.pluginSettings.getMinimumPlayers()) {
                    Game.startGame();
                }
            }

            this.getServer().getScheduler().runTaskTimer(this, new Runnable() {
                public void run() {
                    if (!Game.hasStarted() && !Game.isStarting() && Bukkit.getServer().getOnlinePlayers().length >= pluginSettings.getMinimumPlayers())
                        Game.startGame();
                }
            }, 20L, 20L);
        }

        economyEnabled = this.getServer().getPluginManager().isPluginEnabled("Vault");
        this.getServer().getScheduler().runTaskTimer(this, new Runnable() {
            public void run() {
                try {
                    economyEnabled = getServer().getPluginManager().isPluginEnabled("Vault");
                } catch (Exception ex) {
                }
            }
        }, 20L, 18000L);
    }

    @Override
    public void onDisable() {
        for (Permission permission : Permissions.getPermissions()) {
            this.getServer().getPluginManager().removePermission(permission);
        }
        Bukkit.getServer().getScheduler().cancelTasks(this);
        if (Game.hasStarted()) {
            Game.endGame(Game.getWinnerTeam());
        } else {
            Game.endGame(Team.NONE);
        }
        for (World loadedWorld : this.getServer().getWorlds()) {
            for (Item item : loadedWorld.getEntitiesByClass(Item.class)) {
                item.remove();
            }
        }

        this.playerClass.clear();
        this.playerGolems.clear();
        this.playerTitanClass.clear();
        this.playerInventories.clear();
        this.playersInGolem.clear();
        this.playerListener.grenadeLocations.clear();
        this.playerListener.grenadeItems.clear();
        this.playerListener.jumpDelay.clear();
        this.playerListener.playerGolemTasks.clear();
        ArenaMap.arenaMaps.clear();
        PlayerClass.classList.clear();
        Titan.titanList.clear();
        Permissions.clearPermissions();
        CommandBase.clearCommands();
        this.csUtility = null;
        this.pluginSettings = null;
        this.eventListener = null;
        this.playerListener = null;
        this.vaultChat = null;
    }

    /**
     * Load the configuration.
     */
    public void loadConfiguration() {
        String strDefaultLocation1 = "world " + 0D + " " + 100D + " " + 0D;
        String strDefaultLocation2 = "world " + 0D + " " + 100D + " " + 0D + " " + 0F + " " + 0F;

        this.getConfig().options().header("Titanfall configuration");
        this.getConfig().addDefault("Plugin prefix", "&7[&5Titanfall&7] &e");
        this.getConfig().addDefault("Plugin name", "Titanfall");
        this.getConfig().addDefault("Hub server name", "Lobby");
        this.getConfig().addDefault("Lobby location", strDefaultLocation2);
        this.getConfig().addDefault("Reward coins", 20);
        this.getConfig().addDefault("Contribution coins", 5);
        this.getConfig().addDefault("Maximum time", 1200);
        this.getConfig().addDefault("Minimum players", 4);
        this.getConfig().addDefault("Maximum players", 30);
        this.getConfig().addDefault("Countdown time", 60);
        this.getConfig().addDefault("Invincibility time", 15);
        this.getConfig().addDefault("Maximum points", 50);
        this.getConfig().addDefault("Kill points", 1);
        this.getConfig().addDefault("Grenade cooldown", 5D);
        this.getConfig().addDefault("Double jump.Delay", 2F);
        this.getConfig().addDefault("Double jump.Velocity", 1.2F);
        this.getConfig().addDefault("Double jump.Y Height", 1.5F);
        if (!this.getConfig().contains("Maps") || !this.getConfig().isConfigurationSection("Maps") || this.getConfig().getConfigurationSection("Maps").getValues(false).isEmpty()) {
            this.getConfig().addDefault("Maps.Map1.Spawns." + Team.IMC.getName(), strDefaultLocation2.replaceFirst("world", "Map1"));
            this.getConfig().addDefault("Maps.Map1.Spawns." + Team.MILITIA.getName(), strDefaultLocation2.replaceFirst("world", "Map1"));
            this.getConfig().addDefault("Maps.Map2.Spawns." + Team.IMC.getName(), strDefaultLocation2.replaceFirst("world", "Map2"));
            this.getConfig().addDefault("Maps.Map2.Spawns." + Team.MILITIA.getName(), strDefaultLocation2.replaceFirst("world", "Map2"));
        }
        this.getConfig().addDefault("Smart Pistol.Radius.X", 7.5D);
        this.getConfig().addDefault("Smart Pistol.Radius.Y", 5D);
        this.getConfig().addDefault("Smart Pistol.Radius.Z", 7.5D);
        this.getConfig().addDefault("Grenades.Frag.Name", "&4Frag");
        this.getConfig().addDefault("Grenades.Frag.Damage", 5D);
        this.getConfig().addDefault("Grenades.Frag.Time", 5D);
        this.getConfig().addDefault("Grenades.Frag.Radius", 3D);
        this.getConfig().addDefault("Grenades.Frag.Duration", 5D);
        this.getConfig().addDefault("Grenades.Flash.Name", "&fFlash");
        this.getConfig().addDefault("Grenades.Flash.Damage", 1D);
        this.getConfig().addDefault("Grenades.Flash.Time", 5D);
        this.getConfig().addDefault("Grenades.Flash.Radius", 2.5D);
        this.getConfig().addDefault("Grenades.Flash.Duration", 4D);
        this.getConfig().addDefault("Grenades.Stun.Name", "&eStun");
        this.getConfig().addDefault("Grenades.Stun.Damage", 1.5D);
        this.getConfig().addDefault("Grenades.Stun.Time", 4.5D);
        this.getConfig().addDefault("Grenades.Stun.Radius", 3D);
        this.getConfig().addDefault("Grenades.Stun.Duration", 5D);
        this.getConfig().addDefault("Grenades.Incendiary.Name", "&cIncendiary");
        this.getConfig().addDefault("Grenades.Incendiary.Damage", 0D);
        this.getConfig().addDefault("Grenades.Incendiary.Time", 5D);
        this.getConfig().addDefault("Grenades.Incendiary.Radius", 3.5D);
        this.getConfig().addDefault("Grenades.Incendiary.Duration", 3D);
        if (!this.getConfig().contains("Classes") || !this.getConfig().isConfigurationSection("Classes") || this.getConfig().getConfigurationSection("Classes").getValues(false).isEmpty()) {
            DEFAULT_CLASS = "Assault";
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Primary Gun", "R-97 Compact SMG");
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Secondary Gun", "B3 Wingman");
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Anti-Titan Gun", "Sidewinder AT-SMR");
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Frag grenade", 5);
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Ammo.Primary.Type", "Machine");
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Ammo.Primary.Amount", 150);
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Ammo.Secondary.Amount", 30);
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Ammo.Anti-Titan.Amount", 20);
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Armour.Helmet.Name", "&4Helmet");
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Armour.Helmet.Material", Material.IRON_HELMET.toString());
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Armour.Chestplate.Name", "&4Chestplate");
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Armour.Chestplate.Material", Material.IRON_CHESTPLATE.toString());
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Armour.Leggings.Name", "&4Leggings");
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Armour.Leggings.Material", Material.IRON_LEGGINGS.toString());
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Armour.Boots.Name", "&4Boots");
            this.getConfig().addDefault("Classes." + DEFAULT_CLASS + ".Armour.Boots.Material", Material.IRON_BOOTS.toString());
        }
        this.getConfig().addDefault("Titans.Game delay", 60);
        this.getConfig().addDefault("Titans.Call delay", 90);
        this.getConfig().addDefault("Titans.Spawn delay", 3F);
        if (!this.getConfig().contains("Titans.Titan") || !this.getConfig().isConfigurationSection("Titans.Titan") || this.getConfig().getConfigurationSection("Titans.Titan").getValues(false).isEmpty()) {
            this.getConfig().addDefault("Titans.Titan.Atlas.Nametag", "&7Atlas");
            this.getConfig().addDefault("Titans.Titan.Atlas.Health", 60D);
            this.getConfig().addDefault("Titans.Titan.Atlas.Primary Gun", "XO-16 Chaingun");
            this.getConfig().addDefault("Titans.Titan.Atlas.Ammo", 250);
            this.getConfig().addDefault("Titans.Titan.Atlas.Mode", Titan.TitanMode.AUTO_EJECT.toString());
            this.getConfig().addDefault("Titans.Titan.Atlas.Special", Titan.TitanSpecial.AUTO_ROCKETS.toString());
            this.getConfig().addDefault("Titans.Titan.Atlas.Potion Effects." + PotionEffectType.INCREASE_DAMAGE.getName(), 1);
            this.getConfig().addDefault("Titans.Titan.Stryder.Nametag", "&bStryder");
            this.getConfig().addDefault("Titans.Titan.Stryder.Health", 40D);
            this.getConfig().addDefault("Titans.Titan.Stryder.Primary Gun", "XO-16 Chaingun");
            this.getConfig().addDefault("Titans.Titan.Stryder.Ammo", 300);
            this.getConfig().addDefault("Titans.Titan.Stryder.Mode", Titan.TitanMode.AUTO_EJECT.toString());
            this.getConfig().addDefault("Titans.Titan.Stryder.Special", Titan.TitanSpecial.LASER.toString());
            this.getConfig().addDefault("Titans.Titan.Stryder.Potion Effects." + PotionEffectType.SPEED.getName(), 1);
            this.getConfig().addDefault("Titans.Titan.Ogre.Nametag", "&8Ogre");
            this.getConfig().addDefault("Titans.Titan.Ogre.Health", 60D);
            this.getConfig().addDefault("Titans.Titan.Ogre.Primary Gun", "40MM Cannon");
            this.getConfig().addDefault("Titans.Titan.Ogre.Ammo", 300);
            this.getConfig().addDefault("Titans.Titan.Ogre.Mode", Titan.TitanMode.AUTO_EXPLODE.toString());
            this.getConfig().addDefault("Titans.Titan.Ogre.Special", Titan.TitanSpecial.AUTO_ROCKETS.toString());
            this.getConfig().addDefault("Titans.Titan.Ogre.Potion Effects." + PotionEffectType.DAMAGE_RESISTANCE.getName(), 1);
            this.getConfig().addDefault("Titans.Titan.Ogre.Potion Effects." + PotionEffectType.SLOW.getName(), 1);
        }
        this.getConfig().options().copyDefaults(true);
        this.getConfig().options().copyHeader(true);
        this.saveConfig();

        DEFAULT_CLASS = new ArrayList<>(this.getConfig().getConfigurationSection("Classes").getKeys(false)).get(0);
        this.pluginSettings.setPrefix(this.getConfig().getString("Plugin prefix", "&7[&5Titanfall&7] &e"));
        this.pluginSettings.setPluginName(this.getConfig().getString("Plugin name", "Titanfall"));
        this.pluginSettings.setHub(this.getConfig().getString("Hub server name", "Lobby"));
        this.pluginSettings.setLobbyLocation(Utils.convertStringToLocation(this.getConfig().getString("Lobby location", strDefaultLocation2)));
        this.pluginSettings.setRewardCoins(this.getConfig().getInt("Reward coins", 20));
        this.pluginSettings.setContributionCoins(this.getConfig().getInt("Contribution coins", 5));
        this.pluginSettings.setMaximumTime(this.getConfig().getInt("Maximum time", 1200));
        this.pluginSettings.setMinimumPlayers(this.getConfig().getInt("Minimum players", 4));
        this.pluginSettings.setMaximumPlayers(this.getConfig().getInt("Maximum players", 30));
        this.pluginSettings.setCountdownTime(this.getConfig().getInt("Countdown time", 60));
        this.pluginSettings.setInvincibilityTime(this.getConfig().getInt("Invincibility time", 15));
        this.pluginSettings.setMaximumPoints(this.getConfig().getInt("Maximum points", 50));
        this.pluginSettings.setKillPoints(this.getConfig().getInt("Kill points", 1));
        this.pluginSettings.setGrenadeCooldown(this.getConfig().getDouble("Grenade cooldown", 5D));
        this.pluginSettings.setSmartPistolRadius(this.getConfig().getDouble("Smart Pistol.Radius.X", 7.5D), this.getConfig().getDouble("Smart Pistol.Radius.Y", 5D), this.getConfig().getDouble("Smart Pistol.Radius.Z", 7.5D));
        this.pluginSettings.setDoubleJumpDelay((float) this.getConfig().getDouble("Double jump.Delay", 2F));
        this.pluginSettings.setDoubleJumpVelocity((float) this.getConfig().getDouble("Double jump.Velocity", 1.2F));
        this.pluginSettings.setDoubleJumpYHeight((float) this.getConfig().getDouble("Double jump.Y Height", 1.5F));
        this.pluginSettings.setTitanGameDelay(this.getConfig().getInt("Titans.Game delay", 60));
        this.pluginSettings.setTitanCallDelay(this.getConfig().getInt("Titans.Call delay", 90));
        this.pluginSettings.setTitanSpawnDelay((float) this.getConfig().getDouble("Titans.Spawn delay", 3F));

        ArenaMap.arenaMaps.clear();
        for (String mapName : this.getConfig().getConfigurationSection("Maps").getKeys(false)) {
            mapName = mapName.replaceAll(" ", "_");
            String strTeam1 = this.getConfig().getString("Maps." + mapName + ".Spawns." + Team.IMC.getName(), strDefaultLocation2);
            String strTeam2 = this.getConfig().getString("Maps." + mapName + ".Spawns." + Team.MILITIA.getName(), strDefaultLocation2);
            World mapWorld = this.getServer().getWorld(mapName);
            if (mapWorld == null) mapWorld = this.getServer().createWorld(WorldCreator.name(mapName));
            Location[] spawnLocations = new Location[]{Utils.convertStringToLocation(strTeam1), Utils.convertStringToLocation(strTeam2)};
            ArenaMap.arenaMaps.add(new ArenaMap(mapWorld.getUID(), spawnLocations));
            this.getLogger().info("Added map: " + mapWorld.getName());
        }

        Grenade.grenadeList.clear();
        for (String strGrenade : this.getConfig().getConfigurationSection("Grenades").getKeys(false)) {
            String grenadePath = "Grenades." + strGrenade + ".";
            String strName = this.getConfig().getString(grenadePath + "Name", "&4Unknown");
            Grenade.GrenadeType grenadeType = Grenade.GrenadeType.getByName(strGrenade);
            if (grenadeType != Grenade.GrenadeType.NONE) {
                double grenadeDamage = this.getConfig().getDouble(grenadePath + "Damage", 4D);
                double grenadeTime = this.getConfig().getDouble(grenadePath + "Time", 2D);
                double grenadeRadius = this.getConfig().getDouble(grenadePath + "Radius", 3D);
                double grenadeDuration = this.getConfig().getDouble(grenadePath + "Duration", 5D);
                Grenade.grenadeList.put(strGrenade, new Grenade(strName, grenadeType, grenadeDamage, grenadeTime, grenadeRadius, grenadeDuration));
                this.getLogger().info("Registered the grenade: " + ChatColor.stripColor(strGrenade));
            }
        }

        PlayerClass.classList.clear();
        for (String strClass : this.getConfig().getConfigurationSection("Classes").getKeys(false)) {
            PlayerClass gunClass = null;
            String classPath = "Classes." + strClass;
            if (this.getConfig().contains(classPath + ".Primary Gun") && this.getConfig().contains(classPath + ".Secondary Gun") && this.getConfig().contains(classPath + ".Anti-Titan Gun")) {
                ItemStack[] itemContents = new ItemStack[this.getConfig().getConfigurationSection(classPath).getValues(false).size() - 2];
                ItemStack[] armourContents = new ItemStack[4];
                try {
                    if (this.getConfig().contains(classPath + ".Armour.Helmet")) {
                        ItemStack helmetStack = new ItemStack(Material.getMaterial(this.getConfig().getString(classPath + ".Armour.Helmet.Material", Material.IRON_HELMET.toString())));
                        armourContents[3] = Utils.ItemUtils.setName(helmetStack, this.getConfig().getString(classPath + ".Armour.Helmet.Name", "&4Helmet"));
                    }
                    if (this.getConfig().contains(classPath + ".Armour.Chestplate")) {
                        ItemStack chestplateStack = new ItemStack(Material.getMaterial(this.getConfig().getString(classPath + ".Armour.Chestplate.Material", Material.IRON_CHESTPLATE.toString())));
                        armourContents[2] = Utils.ItemUtils.setName(chestplateStack, this.getConfig().getString(classPath + ".Armour.Chestplate.Name", "&4Chestplate"));
                    }
                    if (this.getConfig().contains(classPath + ".Armour.Leggings")) {
                        ItemStack leggingsStack = new ItemStack(Material.getMaterial(this.getConfig().getString(classPath + ".Armour.Leggings.Material", Material.IRON_LEGGINGS.toString())));
                        armourContents[1] = Utils.ItemUtils.setName(leggingsStack, this.getConfig().getString(classPath + ".Armour.Leggings.Name", "&4Leggings"));
                    }
                    if (this.getConfig().contains(classPath + ".Armour.Boots")) {
                        ItemStack bootsStack = new ItemStack(Material.getMaterial(this.getConfig().getString(classPath + ".Armour.Boots.Material", Material.IRON_BOOTS.toString())));
                        armourContents[0] = Utils.ItemUtils.setName(bootsStack, this.getConfig().getString(classPath + ".Armour.Boots.Name", "&4Boots"));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                itemContents[0] = this.csUtility.generateWeapon(this.getConfig().getString(classPath + ".Primary Gun", "R-97 Compact SMG"));
                if (itemContents[0] == null) itemContents[0] = new ItemStack(Material.AIR);
                itemContents[1] = this.csUtility.generateWeapon(this.getConfig().getString(classPath + ".Secondary Gun", "B3 Wingman"));
                if (itemContents[1] == null) itemContents[1] = new ItemStack(Material.AIR);
                itemContents[2] = this.csUtility.generateWeapon(this.getConfig().getString(classPath + ".Anti-Titan Gun", "XO-16 Chaingun"));
                if (itemContents[2] == null) itemContents[2] = new ItemStack(Material.AIR);

                List<ItemStack> classAmmo = new ArrayList<ItemStack>();

                String primaryAmmoType = this.getConfig().getString(classPath + ".Ammo.Primary.Type", "Assault");
                int primaryAmmo = this.getConfig().getInt(classPath + ".Ammo.Primary.Amount", 100);
                String secondaryAmmoType = this.getConfig().getString(classPath + ".Ammo.Secondary.Type", "Pistol");
                int secondaryAmmo = this.getConfig().getInt(classPath + ".Ammo.Secondary.Amount", 30);
                String antiTitanAmmoType = this.getConfig().getString(classPath + ".Ammo.Anti-Titan.Type", "Rocket");
                int antiTitanAmmo = this.getConfig().getInt(classPath + ".Ammo.Anti-Titan.Amount", 1);
                ItemStack[] primaryItems = new ItemStack[((int) (primaryAmmo / 64)) + 1];
                ItemStack[] secondaryItems = new ItemStack[((int) (secondaryAmmo / 64)) + 1];
                ItemStack[] antiTitanItems = new ItemStack[((int) (antiTitanAmmo / 64)) + 1];

                String currentType = primaryAmmoType;
                int primaryID = currentType != null ? (currentType.equalsIgnoreCase("Assault") ? AMMO_ID_ASSAULT : (currentType.equalsIgnoreCase("Shotgun") ? AMMO_ID_SHOTGUN : (currentType.equalsIgnoreCase("Sniper") ? AMMO_ID_SNIPER : (currentType.equalsIgnoreCase("Pistol") ? AMMO_ID_PISTOL : (currentType.equalsIgnoreCase("Rocket") ? AMMO_ID_ROCKET : currentType.equalsIgnoreCase("Anti-Titan") ? AMMO_ID_ANTITITAN : AMMO_ID_ASSAULT))))) : AMMO_ID_ASSAULT;
                for (int i = 0; i < primaryAmmo; i++) {
                    int index = (int) i / 64;
                    ItemStack primaryItem = primaryItems[index];
                    if (primaryItem == null)
                        primaryItem = Utils.ItemUtils.setName(new ItemStack(primaryID), "&6" + primaryAmmoType + " Ammo");
                    else primaryItem.setAmount(primaryItem.getAmount() + 1);
                    primaryItems[index] = primaryItem;
                }

                currentType = secondaryAmmoType;
                int secondaryID = currentType != null ? (currentType.equalsIgnoreCase("Assault") ? AMMO_ID_ASSAULT : (currentType.equalsIgnoreCase("Shotgun") ? AMMO_ID_SHOTGUN : (currentType.equalsIgnoreCase("Sniper") ? AMMO_ID_SNIPER : (currentType.equalsIgnoreCase("Pistol") ? AMMO_ID_PISTOL : (currentType.equalsIgnoreCase("Rocket") ? AMMO_ID_ROCKET : currentType.equalsIgnoreCase("Anti-Titan") ? AMMO_ID_ANTITITAN : AMMO_ID_PISTOL))))) : AMMO_ID_PISTOL;
                for (int i = 0; i < secondaryAmmo; i++) {
                    int index = (int) i / 64;
                    ItemStack secondaryItem = secondaryItems[index];
                    if (secondaryItem == null)
                        secondaryItem = Utils.ItemUtils.setName(new ItemStack(secondaryID), "&b" + currentType + " Ammo");
                    else secondaryItem.setAmount(secondaryItem.getAmount() + 1);
                    secondaryItems[index] = secondaryItem;
                }

                currentType = antiTitanAmmoType;
                int antiTitanID = currentType != null ? (currentType.equalsIgnoreCase("Assault") ? AMMO_ID_ASSAULT : (currentType.equalsIgnoreCase("Shotgun") ? AMMO_ID_SHOTGUN : (currentType.equalsIgnoreCase("Sniper") ? AMMO_ID_SNIPER : (currentType.equalsIgnoreCase("Pistol") ? AMMO_ID_PISTOL : (currentType.equalsIgnoreCase("Rocket") ? AMMO_ID_ROCKET : currentType.equalsIgnoreCase("Anti-Titan") ? AMMO_ID_ANTITITAN : AMMO_ID_ROCKET))))) : AMMO_ID_ROCKET;
                for (int i = 0; i < antiTitanAmmo; i++) {
                    int index = (int) i / 64;
                    ItemStack antiTitanItem = antiTitanItems[index];
                    if (antiTitanItem == null)
                        antiTitanItem = Utils.ItemUtils.setName(new ItemStack(antiTitanID), "&c" + antiTitanAmmoType + " Ammo");
                    else antiTitanItem.setAmount(antiTitanItem.getAmount() + 1);
                    antiTitanItems[index] = antiTitanItem;
                }

                classAmmo.addAll(Arrays.asList(primaryItems));
                classAmmo.addAll(Arrays.asList(secondaryItems));
                classAmmo.addAll(Arrays.asList(antiTitanItems));
                gunClass = new PlayerClass(strClass, classAmmo.toArray(new ItemStack[classAmmo.size()]));//, new ItemStack[]{itemContents[0], itemContents[1], itemContents[2]});

                ItemStack[] grenades = new ItemStack[4];
                if (this.getConfig().contains(classPath + ".Frag Grenade")) {
                    grenades[0] = Grenade.grenadeList.get("Frag").getItem();
                    grenades[0].setAmount(this.getConfig().getInt(classPath + ".Frag Grenade", 1));
                }
                if (this.getConfig().contains(classPath + ".Flash Grenade")) {
                    grenades[1] = Grenade.grenadeList.get("Flash").getItem();
                    grenades[1].setAmount(this.getConfig().getInt(classPath + ".Flash Grenade", 1));
                }
                if (this.getConfig().contains(classPath + ".Stun Grenade")) {
                    grenades[2] = Grenade.grenadeList.get("Stun").getItem();
                    grenades[2].setAmount(this.getConfig().getInt(classPath + ".Stun Grenade", 1));
                }
                if (this.getConfig().contains(classPath + ".Incendiary Grenade")) {
                    grenades[3] = Grenade.grenadeList.get("Incendiary").getItem();
                    grenades[3].setAmount(this.getConfig().getInt(classPath + ".Incendiary Grenade", 1));
                }

                PlayerClass.classList.put(strClass, gunClass.setContents(itemContents).setArmour(armourContents).setGrenade(grenades[0], Grenade.GrenadeType.FRAG).setGrenade(grenades[1], Grenade.GrenadeType.FLASH).setGrenade(grenades[2], Grenade.GrenadeType.STUN).setGrenade(grenades[3], Grenade.GrenadeType.INCENDIARY));
            }
        }

        Titan.titanList.clear();
        for (String strTitan : this.getConfig().getConfigurationSection("Titans.Titan").getKeys(false)) {
            String titanPath = "Titans.Titan." + strTitan + ".";
            String nameTag = this.getConfig().getString(titanPath + "Nametag", "&7Invalid_Name");
            String strPrimaryGun = this.getConfig().getString(titanPath + "Primary Gun", "XO-16 Chaingun");
            ItemStack primaryGun = this.csUtility.generateWeapon(strPrimaryGun);
            if (primaryGun != null) {
                int primaryAmmo = this.getConfig().getInt(titanPath + "Ammo", 250);
                if (primaryAmmo <= 0) primaryAmmo = 250;
                Titan.TitanMode titanMode = Titan.TitanMode.getMode(this.getConfig().getString(titanPath + "Mode"));
                Titan.TitanSpecial titanSpecial = Titan.TitanSpecial.getSpecial(this.getConfig().getString(titanPath + "Special"));
                double titanHealth = this.getConfig().getDouble(titanPath + "Health", 40D);
                List<PotionEffect> potionEffects = new ArrayList<>();
                if (this.getConfig().contains(strTitan + "Potion Effects")) {
                    for (Map.Entry<String, Object> potionEntry : this.getConfig().getConfigurationSection(strTitan + "Potion Effects").getValues(false).entrySet()) {
                        PotionEffectType potionEffectType = PotionEffectType.getByName(potionEntry.getKey());
                        if (potionEffectType != null) {
                            String strLevel = String.valueOf(potionEntry.getValue());
                            int potionLevel = 1;
                            if (Utils.isInteger(strLevel)) {
                                potionLevel = Integer.parseInt(strLevel);
                                if (potionLevel < 0) potionLevel = 0;
                            }
                            potionEffects.add(new PotionEffect(potionEffectType, Integer.MAX_VALUE, potionLevel));
                        } else {
                            this.getLogger().warning("Unknown potion effect type for titan class '" + strTitan + "': " + potionEntry.getKey());
                        }
                    }
                }
                Titan.titanList.put(strTitan, new Titan(strTitan, nameTag, titanHealth, primaryGun).setTitanMode(titanMode).setTitanSpecial(titanSpecial).setPotionEffects(potionEffects));
            }
        }
    }

    /**
     * Add a player in a titan to the list.
     *
     * @param player - The player.
     */
    public void addPlayerToTitan(Player player) {
        if (player != null && !this.playersInGolem.contains(player.getName()))
            this.playersInGolem.add(player.getName());
    }

    /**
     * Clear all players' classes.
     */
    public void clearClasses() {
        this.playerClass.clear();
    }

    /**
     * Clear all inventories and titan inventories.
     */
    public void clearInventories() {
        this.playerInventories.clear();
    }

    /**
     * Clear all player's titan classes.
     */
    public void clearTitanClasses() {
        this.playerTitanClass.clear();
    }

    /**
     * Clear all player's titans.
     */
    public void clearTitanPlayers() {
        this.playerGolems.clear();
        this.playersInGolem.clear();
    }

    public Chat getChat() {
        return this.vaultChat;
    }

    /**
     * Get the class of a player.
     *
     * @param player - The player.
     * @return The class of the player.
     */
    public PlayerClass getClass(Player player) {
        return this.hasClass(player) ? this.playerClass.get(player.getName()) : null;
    }

    /**
     * Get the class by the name.
     *
     * @param className - The class name.
     * @return The class.
     */
    public PlayerClass getClass(String className) {
        for (PlayerClass gunClass : PlayerClass.classList.values()) {
            if (gunClass.getName().equalsIgnoreCase(className)) return gunClass;
        }
        return PlayerClass.classList.get(className);
    }

    /**
     * Get the CrackShot API utility class.
     *
     * @return The CrackShot API instance.
     */
    public CSUtility getCSUtility() {
        return this.csUtility;
    }

    /**
     * Get the event listener instance.
     *
     * @return The event listener instance.
     */
    public EventListener getEventListener() {
        return this.eventListener;
    }

    /**
     * Get the grenade by the item name.
     *
     * @param itemName - The item name.
     * @return The grenade.
     */
    public Grenade getGrenade(String itemName) {
        if (itemName != null) {
            itemName = ChatColor.stripColor(itemName).replaceAll(" Grenade", "").trim();
            for (Map.Entry<String, Grenade> grenadeEntry : Grenade.grenadeList.entrySet()) {
                if (grenadeEntry.getKey().equalsIgnoreCase(itemName) || ChatColor.stripColor(grenadeEntry.getValue().getName()).equalsIgnoreCase(itemName))
                    return grenadeEntry.getValue();
            }
        }
        return itemName == null ? null : Grenade.grenadeList.get(ChatColor.stripColor(itemName));
    }

    /**
     * Get a player's inventory.
     * Note: Will return null if the player doesn't have an inventory.
     *
     * @param player - The player.
     * @return The player's inventory.
     */
    public PlayerInventoryData getInventory(Player player) {
        if (player != null) return this.playerInventories.get(player.getName());
        else return null;
    }

    /**
     * Get the player listener instance.
     *
     * @return The player listener instance.
     */
    public PlayerListener getPlayerListener() {
        return this.playerListener;
    }

    /**
     * Get the plugin settings instance.
     *
     * @return The plugin settings instance.
     */
    public Settings getSettings() {
        return this.pluginSettings;
    }

    /**
     * Get the titan by the owner.
     *
     * @param player - The owner.
     * @return The titan.
     */
    public IronGolem getTitanEntity(Player player) {
        if (this.playerGolems.containsKey(player.getName())) {
            UUID playerUUID = this.getTitanUUID(player);
            for (IronGolem ironGolem : player.getWorld().getEntitiesByClass(IronGolem.class)) {
                if (ironGolem.getUniqueId().equals(playerUUID))
                    return ironGolem;
            }
        }
        return null;
    }

    /**
     * Get the titan by the owner.
     *
     * @param player - The owner.
     * @return The titan.
     */
    public CustomIronGolem getTitan(Player player) {
        IronGolem ironGolem = this.getTitanEntity(player);
        return ironGolem != null ? (CustomIronGolem) ((CraftIronGolem) ironGolem).getHandle() : null;
    }

    /**
     * Get a player's titan class.
     *
     * @param player - The player.
     * @return The Titan class.
     */
    public Titan getTitanClass(Player player) {
        if (player != null) return this.playerTitanClass.get(player.getName());
        else return null;
    }

    /**
     * Get the Titan class by the titan class name.
     *
     * @param titanClassName - The titan class name.
     * @return The Titan class.
     */
    public Titan getTitanClass(String titanClassName) {
        for (Titan titan : Titan.titanList.values()) {
            if (ChatColor.stripColor(titan.getName()).equalsIgnoreCase(titanClassName)) return titan;
        }
        return Titan.titanList.get(titanClassName);
    }

    /**
     * Get the titan type by the name tag of a titan.
     *
     * @param titanTag - The name tag.
     * @return The titan type.
     */
    public Titan getTitanType(String titanTag) {
        if (titanTag != null) {
            if (titanTag.contains(" ")) {
                String[] tagSplit = titanTag.split(" ");
                if (tagSplit.length > 0)
                    titanTag = tagSplit[tagSplit.length - 1];
            }
            for (Titan titan : Titan.titanList.values()) {
                if (titan.getTag().endsWith(titanTag)) return titan;
            }
            return Titan.titanList.get(titanTag);
        }
        return null;
    }

    /**
     * Get the UUID of the titan by the owner.
     *
     * @param player - The owner.
     * @return The titan's UUID.
     */
    public UUID getTitanUUID(Player player) {
        if (this.playerGolems.containsKey(player.getName())) {
            return this.playerGolems.get(player.getName());
        }
        return null;
    }

    /**
     * Gets the owner of a Titan by its UUID.
     *
     * @param titanUUID - The UUID of the Titan.
     * @return The owner of the titan.
     */
    public Player getTitanOwner(UUID titanUUID) {
        if (this.playerGolems.containsValue(titanUUID)) {
            for (Map.Entry<String, UUID> playerEntry : this.playerGolems.entrySet()) {
                if (playerEntry.getValue().compareTo(titanUUID) == 0)
                    return Bukkit.getPlayerExact(playerEntry.getKey());
            }
        }
        return null;
    }

    /**
     * Gets the owner's UUID of a Titan by its UUID.
     *
     * @param titanUUID - The UUID of the Titan.
     * @return The owner's UUID of the titan.
     */
    public UUID getTitanOwnerUUID(UUID titanUUID) {
        Player titanOwner = this.getTitanOwner(titanUUID);
        return titanOwner != null ? titanOwner.getUniqueId() : null;
    }

    /**
     * Check if a player has a class or not.
     *
     * @param player - The player.
     * @return True if the player has a class, false if not.
     */
    public boolean hasClass(Player player) {
        return player != null && this.playerClass.containsKey(player.getName());
    }

    /**
     * Check if a player has an inventory.
     *
     * @param player - The player.
     * @return Whether the player has an inventory or not.
     */
    public boolean hasInventory(Player player) {
        return player != null && this.playerInventories.containsKey(player.getName());
    }

    /**
     * Check if a player owns a titan.
     *
     * @param player - The player.
     * @return Returns whether the player has a titan or not.
     */
    public boolean hasTitan(Player player) {
        return player != null && this.playerGolems.containsKey(player.getName());
    }

    /**
     * Check if a player has a titan class.
     *
     * @param player - The player.
     * @return Returns whether the player has a titan class or not.
     */
    public boolean hasTitanClass(Player player) {
        return player != null && this.playerTitanClass.containsKey(player.getName());
    }

    public boolean inTitan(Player player) {
        return player != null && this.playersInGolem.contains(player.getName());
    }

    /**
     * Check if the entity's UUID is a titan's UUID.
     *
     * @param titanUUID - The entity's UUID.
     * @return Returns true if the UUID is that of a Titan.
     */
    public boolean isTitan(UUID titanUUID) {
        return this.playerGolems.containsValue(titanUUID);
    }

    /**
     * Remove a player no longer in a titan from the list.
     *
     * @param player - The player.
     */
    public void removePlayerFromTitan(Player player) {
        if (player != null && this.playersInGolem.contains(player.getName()))
            this.playersInGolem.remove(player.getName());
    }

    /**
     * Remove a player from the titan list.
     *
     * @param titanOwner - The titan's owner.
     */
    public void removePlayerTitan(Player titanOwner) {
        if (titanOwner != null) this.playerGolems.remove(titanOwner.getName());
    }

    /**
     * Set the class of a player.
     *
     * @param player - The player.
     * @param gunClass - The class, can be null if you want to remove it.
     */
    public void setClass(Player player, PlayerClass gunClass) {
        if (player != null) {
            if (gunClass == null) this.playerClass.remove(player.getName());
            else this.playerClass.put(player.getName(), gunClass);
        }
    }

    /**
     * Set a player's inventory.
     *
     * @param player - The player.
     * @param inventoryData - The inventory's data. Can be null if you want to remove it.
     * @return Returns the inventory data. If the inventoryData was null, returns the removed inventory data.
     */
    public PlayerInventoryData setInventory(Player player, PlayerInventoryData inventoryData) {
        if (player != null) {
            if (inventoryData == null) return this.playerInventories.remove(player.getName());
            else this.playerInventories.put(player.getName(), inventoryData);
        }
        return inventoryData;
    }

    /**
     * Assign a titan's UUID to a player.
     *
     * @param player - - The player.
     * @param titanUUID - The titan's UUID.
     * @return Returns the UUID of the added titan OR if removed, the removed titan.
     */
    public UUID setTitan(Player player, UUID titanUUID) {
        if (player != null) {
            if (titanUUID != null) return this.playerGolems.put(player.getName(), titanUUID);
            else return this.playerGolems.remove(player.getName());
        }
        return titanUUID;
    }

    /**
     * Set the titan class of a player.
     *
     * @param player - The player.
     * @param titanClass - The titan class, can be null if you want to remove it.
     */
    public void setTitanClass(Player player, Titan titanClass) {
        if (player != null) {
            if (titanClass == null) this.playerTitanClass.remove(player.getName());
            else this.playerTitanClass.put(player.getName(), titanClass);
        }
    }

    public void registerCommands() {
        CommandBase.clearCommands();
        CommandBase.registerCommands(new CommandTitanfall(), "titanfall");
        CommandBase.registerCommands(new CommandTeamChat(), "teamchat");

        for (CommandBase commandBase : CommandBase.getCommands()) {
            for (String command : commandBase) {
                this.getCommand(command).setExecutor(commandBase);
            }
        }

        this.getCommand("titanfall").setAliases(Arrays.asList("tf", "titanfalls", "tfs"));
        this.getCommand("teamchat").setAliases(Arrays.asList("tc"));
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) this.vaultChat = chatProvider.getProvider();
        return (this.vaultChat != null);
    }

    /**
     * Get the Titanfall plugin's instance.
     *
     * @return The plugin's instance. May be null if the plugin hasn't been enabled yet.
     */
    public static Main getInstance() {
        return pluginInstance;
    }

    /**
     * Get a blank plain scoreboard.
     *
     * @return A blank plain scoreboard. May be null if the plugin hasn't been enabled yet.
     */
    public static Scoreboard getPlainScoreboard() {
        return plainScoreboard;
    }
}
