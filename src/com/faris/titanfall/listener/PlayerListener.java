package com.faris.titanfall.listener;

import com.faris.titanfall.Game;
import com.faris.titanfall.Lang;
import com.faris.titanfall.Main;
import com.faris.titanfall.entities.CustomIronGolem;
import com.faris.titanfall.equipment.Grenade;
import com.faris.titanfall.equipment.PlayerClass;
import com.faris.titanfall.equipment.Titan;
import com.faris.titanfall.helper.CustomEntityType;
import com.faris.titanfall.helper.Team;
import com.faris.titanfall.helper.Utils;
import com.faris.titanfall.helper.effects.ParticleEffect;
import com.faris.titanfall.helper.gui.ClassMenu;
import com.faris.titanfall.helper.gui.TitanMenu;
import com.faris.titanfall.helper.inventory.PlayerInventoryData;
import net.minecraft.util.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftIronGolem;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.*;


/**
 * @author KingFaris10
 */
public class PlayerListener implements Listener {
    public final Map<String, Integer> jumpDelay = new HashMap<>();
    public final List<String> titanDelays = new ArrayList<>();

    private Main getPlugin() {
        return Main.getInstance();
    }

    private int getOnlinePlayers(boolean ignoreOPs) {
        if (ignoreOPs) {
            int playersOnline = 0;
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (!onlinePlayer.isOp()) playersOnline++;
            }
            return playersOnline;
        } else {
            return Bukkit.getServer().getOnlinePlayers().length;
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        try {
            if (!event.getPlayer().isOp()) {
                if (Main.pluginDisabled) {
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Error while loading the Titanfall plugin. You have been kicked for safety.");
                } else if (this.getOnlinePlayers(true) >= this.getPlugin().getSettings().getMaximumPlayers()) {
                    event.disallow(PlayerLoginEvent.Result.KICK_FULL, Lang.LEAVE_KICKED_MAX_PLAYERS.getMessage());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            event.getPlayer().getInventory().setHeldItemSlot(0);
            for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects())
                event.getPlayer().removePotionEffect(potionEffect.getType());
            event.getPlayer().getInventory().clear();
            event.getPlayer().getInventory().setArmorContents(null);
            Utils.resetPlayer(event.getPlayer());
            event.getPlayer().setScoreboard(Game.getScoreboard(event.getPlayer()));
            if (Game.hasStarted()) {
                event.getPlayer().setGameMode(GameMode.ADVENTURE);
                event.getPlayer().setAllowFlight(true);
                final PlayerClass defaultKit = this.getPlugin().getClass(Main.DEFAULT_CLASS);
                this.getPlugin().setClass(event.getPlayer(), defaultKit);
                int teamAmount1 = Game.getPlayers(Team.IMC).size();
                int teamAmount2 = Game.getPlayers(Team.MILITIA).size();
                if (Math.random() > 0.5) {
                    if (teamAmount2 > teamAmount1) {
                        Game.addPlayer(event.getPlayer(), Team.IMC);
                    } else {
                        Game.addPlayer(event.getPlayer(), Team.MILITIA);
                    }
                } else {
                    if (teamAmount1 > teamAmount2) {
                        Game.addPlayer(event.getPlayer(), Team.MILITIA);
                    } else {
                        Game.addPlayer(event.getPlayer(), Team.IMC);
                    }
                }
                event.getPlayer().setLevel(0);

                final Player player = event.getPlayer();
                player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
                    public void run() {
                        if (player != null) {
                            if (Game.hasStarted()) {
                                player.getInventory().setContents(defaultKit.getContents());
                                player.getInventory().setArmorContents(defaultKit.getArmour());
                                player.getInventory().setItem(3, defaultKit.getGrenade(Grenade.GrenadeType.FRAG));
                                player.getInventory().setItem(4, defaultKit.getGrenade(Grenade.GrenadeType.FLASH));
                                player.getInventory().setItem(5, defaultKit.getGrenade(Grenade.GrenadeType.STUN));
                                player.getInventory().setItem(6, defaultKit.getGrenade(Grenade.GrenadeType.INCENDIARY));
                                player.getInventory().setItem(7, Utils.ItemUtils.setName(new ItemStack(Material.NETHER_STAR), "&aSpawn Titan"));
                                player.getInventory().setItem(8, Utils.ItemUtils.setName(new ItemStack(Material.COMPASS), "&6Change Class"));
                                player.getInventory().addItem(defaultKit.getAmmo());
                                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 0));
                                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
                            }
                            Utils.updateInventory(player, 0L);

                            Team playerTeam = Game.getTeam(player);
                            Location teleportLocation = Game.getMap().getSpawn(playerTeam);
                            if (teleportLocation != null)
                                player.teleport(teleportLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        }
                    }
                }, 2L);
            } else {
                event.getPlayer().setGameMode(GameMode.SURVIVAL);
                event.getPlayer().setAllowFlight(false);
                event.getPlayer().getInventory().setItem(8, Utils.ItemUtils.setName(new ItemStack(Material.COMPASS), "&6Change Class"));
                Utils.updateInventory(event.getPlayer(), 0L);
                if (!Game.isStarting()) {
                    if (event.getPlayer().getServer().getOnlinePlayers().length >= this.getPlugin().getSettings().getMinimumPlayers()) {
                        Game.startGame();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.setQuitMessage("");
        this.onPlayerLeave(event.getPlayer());
    }

    @EventHandler
    public void onPlayerKicked(PlayerKickEvent event) {
        event.setLeaveMessage("");
        this.onPlayerLeave(event.getPlayer());
    }

    private final List<String> blindEffect = new ArrayList<>();
    public final Map<UUID, Location> grenadeLocations = new HashMap<>();
    public final Map<UUID, UUID> grenadeItems = new HashMap<>();
    private final List<String> grenadeDelays = new ArrayList<>();

    public final Map<String, Integer> playerGolemTasks = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        try {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (event.getItem() != null) {
                    final Player player = event.getPlayer();
                    final String playerName = player.getName();
                    Material itemType = event.getItem().getType();
                    String itemName = Utils.ItemUtils.getName(event.getItem());
                    if (itemType == Material.COMPASS) {
                        event.setCancelled(true);
                        if (!ClassMenu.classMenu.containsKey(event.getPlayer().getName()))
                            new ClassMenu(event.getPlayer()).openMenu();
                    } else if (itemType == Material.NETHER_STAR) {
                        event.setCancelled(true);
                        if (Game.hasStarted()) {
                            if (Game.getTime() > this.getPlugin().getSettings().getTitanGameDelay()) {
                                if (!this.getPlugin().hasTitan(player)) {
                                    if (!this.titanDelays.contains(playerName) && !this.playerGolemTasks.containsKey(playerName)) {
                                        if (this.getPlugin().hasTitanClass(player)) {
                                            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                                                final Location spawnLocation = event.getClickedBlock().getLocation().add(0D, 1D, 0D);
                                                spawnLocation.setYaw(player.getLocation().getYaw());
                                                spawnLocation.setPitch(player.getLocation().getPitch());
                                                if (spawnLocation.clone().add(0D, 4D, 0D).getBlock().getType() == Material.AIR) {
                                                    final World spawnWorld = spawnLocation.getWorld();
                                                    this.playerGolemTasks.put(playerName, player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
                                                        public void run() {
                                                            if (player != null && Game.hasStarted() && player.isOnline() && !player.isDead() && spawnWorld != null && getPlugin().hasTitanClass(player)) {
                                                                try {
                                                                    final Titan playerTitan = getPlugin().getTitanClass(player);
                                                                    if (playerTitan != null && spawnLocation != null && spawnLocation.clone().add(0D, 4D, 0D).getBlock().getType() == Material.AIR) {
                                                                        final Team playerTeam = Game.getTeam(player);
                                                                        CustomIronGolem customIronGolem = CustomEntityType.spawnIronGolem(spawnLocation.clone().add(0D, 3D, 0D), playerTitan);
                                                                        ItemStack[] mergedItems = ArrayUtils.addAll(new ItemStack[]{playerTitan.getPrimaryGun()}, playerTitan.getAmmoItems());
                                                                        for (int i = 0; i < mergedItems.length; i++) {
                                                                            if (mergedItems[i] == null)
                                                                                mergedItems[i] = new ItemStack(Material.AIR);
                                                                        }
                                                                        customIronGolem.getInventory().addItem(mergedItems);
                                                                        Player nearestTarget = null;
                                                                        double targetRadius = 50D;
                                                                        IronGolem ironGolem = (IronGolem) customIronGolem.getBukkitEntity();
                                                                        List<Entity> nearbyEntities = ironGolem.getNearbyEntities(targetRadius, targetRadius / 2, targetRadius);
                                                                        for (Entity nearbyEntity : nearbyEntities) {
                                                                            if (nearbyEntity instanceof Player) {
                                                                                Player nearestPlayer = (Player) nearbyEntity;
                                                                                if (Game.getTeam(nearestPlayer).getID() != playerTeam.getID()) {
                                                                                    nearestTarget = nearestPlayer;
                                                                                    break;
                                                                                }
                                                                            }
                                                                        }
                                                                        ironGolem.setTarget(nearestTarget);
                                                                        customIronGolem.setCustomName(String.format(playerTitan.getTag(), playerName));
                                                                        customIronGolem.setCustomNameVisible(true);
                                                                        ironGolem.setMaxHealth(playerTitan.getHealth());
                                                                        ironGolem.setHealth(ironGolem.getMaxHealth());
                                                                        getPlugin().setTitan(player, ironGolem.getUniqueId());
                                                                    } else {
                                                                        if (titanDelays.contains(playerName))
                                                                            titanDelays.remove(playerName);
                                                                    }
                                                                } catch (Exception ex) {
                                                                    ex.printStackTrace();
                                                                }
                                                            } else {
                                                                if (titanDelays.contains(playerName))
                                                                    titanDelays.remove(playerName);
                                                            }
                                                            playerGolemTasks.remove(playerName);
                                                        }
                                                    }, (long) (this.getPlugin().getSettings().getTitanSpawnDelay() * 20L)).getTaskId());

                                                    ParticleEffect.sendToLocation(ParticleEffect.HAPPY_VILLAGER, spawnLocation, 0.25F, 0F, 0.25F, 10F, 10);

                                                    this.titanDelays.add(playerName);
                                                    player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
                                                        public void run() {
                                                            titanDelays.remove(playerName);
                                                        }
                                                    }, this.getPlugin().getSettings().getTitanCallDelay() * 20L);
                                                } else {
                                                    player.sendMessage(Main.getInstance().getSettings().getPrefix() + ChatColor.RED + "You cannot spawn a Titan here!");
                                                }
                                            } else {
                                                if (!TitanMenu.titanMenu.containsKey(playerName))
                                                    new TitanMenu(player).openMenu();
                                            }
                                        } else {
                                            if (!TitanMenu.titanMenu.containsKey(playerName))
                                                new TitanMenu(player).openMenu();
                                        }
                                    } else {
                                        Lang.sendMessage(player, Lang.TITAN_CALL_DELAY);
                                    }
                                } else {
                                    player.sendMessage(Main.getInstance().getSettings().getPrefix() + ChatColor.RED + "You already have a Titan!");
                                }
                            } else {
                                Lang.sendMessage(player, Lang.TITAN_CALL_DELAY);
                            }
                        } else {
                            player.sendMessage(Main.getInstance().getSettings().getPrefix() + ChatColor.RED + "The game hasn't started yet!");
                        }
                    } else if (itemType == Main.GRENADE_MATERIAL) {
                        event.setCancelled(true);
                        final Grenade playerGrenade = this.getPlugin().getGrenade(itemName);
                        if (playerGrenade != null) {
                            if (Game.hasStarted()) {
                                if (!this.grenadeDelays.contains(player.getName())) {
                                    final String pName = player.getName();
                                    final Grenade.GrenadeType grenadeType = playerGrenade.getType();
                                    final Egg grenade = player.launchProjectile(Egg.class);
                                    final UUID grenadeUUID = grenade.getUniqueId();
                                    grenade.setVelocity(player.getLocation().getDirection().multiply(2));
                                    grenade.setShooter(player);
                                    grenade.setBounce(true);
                                    final Team shooterTeam = Game.getTeam(player);
                                    this.grenadeDelays.add(pName);
                                    player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
                                        public void run() {
                                            grenadeDelays.remove(pName);
                                        }
                                    }, (long) (this.getPlugin().getSettings().getGrenadeCooldown() * 20L));
                                    player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
                                        public void run() {
                                            Location grenadeLocation = null;
                                            if (grenadeLocations.containsKey(grenadeUUID)) {
                                                grenadeLocation = grenadeLocations.remove(grenadeUUID);
                                            } else {
                                                if (grenade != null && grenade.isValid() && !grenade.isDead()) {
                                                    grenadeLocation = grenade.getLocation();
                                                    grenade.remove();
                                                }
                                            }
                                            if (grenadeLocation != null) {
                                                if (grenadeItems.containsKey(grenadeUUID)) {
                                                    UUID itemUUID = grenadeItems.get(grenadeUUID);
                                                    for (Item item : grenadeLocation.getWorld().getEntitiesByClass(Item.class)) {
                                                        if (item.getUniqueId().equals(itemUUID)) {
                                                            item.remove();
                                                            break;
                                                        }
                                                    }
                                                    grenadeItems.remove(itemUUID);
                                                }
                                                grenadeLocation.getWorld().createExplosion(grenadeLocation, 0F);

                                                if (Game.hasStarted()) {
                                                    int duration = (int) (playerGrenade.getDuration() * 20);
                                                    for (LivingEntity livingEntity : grenadeLocation.getWorld().getLivingEntities()) {
                                                        if (livingEntity.getLocation().distanceSquared(grenadeLocation) <= playerGrenade.getRadius()) {
                                                            boolean isPlayer = livingEntity.getType() == EntityType.PLAYER && ((Player) livingEntity).getName().equals(playerName);
                                                            if (livingEntity instanceof Player) {
                                                                Player worldPlayer = (Player) livingEntity;
                                                                if (!isPlayer) {
                                                                    if (shooterTeam.getID() == Game.getTeam(worldPlayer).getID()) {
                                                                        continue;
                                                                    }
                                                                }
                                                            }
                                                            if (playerGrenade.getDamage() > 0) {
                                                                if (!isPlayer)
                                                                    livingEntity.damage(playerGrenade.getDamage(), player);
                                                                else livingEntity.damage(playerGrenade.getDamage());
                                                            }
                                                            if (grenadeType == Grenade.GrenadeType.FLASH) {
                                                                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, 1));
                                                            } else if (grenadeType == Grenade.GrenadeType.STUN) {
                                                                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, duration, 1));
                                                                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, 1));
                                                            } else if (grenadeType == Grenade.GrenadeType.INCENDIARY) {
                                                                if (livingEntity.getFireTicks() + duration > livingEntity.getMaxFireTicks())
                                                                    livingEntity.setFireTicks(livingEntity.getMaxFireTicks());
                                                                else
                                                                    livingEntity.setFireTicks(livingEntity.getFireTicks() + duration);
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (grenadeItems.containsKey(grenadeUUID)) {
                                                    UUID itemUUID = grenadeItems.get(grenadeUUID);
                                                    for (World world : Bukkit.getServer().getWorlds()) {
                                                        for (Item item : grenadeLocation.getWorld().getEntitiesByClass(Item.class)) {
                                                            if (item.getUniqueId().equals(itemUUID)) {
                                                                item.remove();
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    grenadeItems.remove(itemUUID);
                                                }
                                            }
                                        }
                                    }, (long) (playerGrenade.getExplosionTime() * 20L));
                                    ItemStack itemStack = event.getItem();
                                    int itemAmount = itemStack.getAmount();
                                    if (itemAmount - 1 <= 0) {
                                        itemStack.setAmount(1);
                                        itemStack.setType(Material.AIR);
                                    } else {
                                        itemStack.setAmount(itemAmount - 1);
                                    }
                                    event.getPlayer().setItemInHand(itemStack);
                                    Utils.updateInventory(event.getPlayer());
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "The game has not started!");
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangeSlot(PlayerItemHeldEvent event) {
        try {
            ItemStack heldItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
            if (heldItem == null || heldItem.getType() == Material.AIR) {
                event.setCancelled(true);
                Utils.updateInventory(event.getPlayer(), 1L);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        try {
            event.setCancelled(true);
            Utils.updateInventory(event.getPlayer(), 2L);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        try {
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        for (PotionEffect potionEffect : event.getEntity().getActivePotionEffects())
            event.getEntity().removePotionEffect(potionEffect.getType());
        event.getEntity().getInventory().clear();
        event.getEntity().getInventory().setArmorContents(null);
        event.setKeepLevel(false);
        event.setDeathMessage("");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (Game.hasStarted()) {
            event.setRespawnLocation(Game.getMap().getSpawn(Game.getTeam(event.getPlayer())));
            if (event.getPlayer().getKiller() != null && !event.getPlayer().getKiller().getName().equals(event.getPlayer().getName())) {
                Game.getTeam(event.getPlayer().getKiller()).addScore(this.getPlugin().getSettings().getKillPoints());
            }
        } else {
            event.setRespawnLocation(this.getPlugin().getSettings().getLobbyLocation());
        }
        this.onPlayerRespawn(event.getPlayer(), false);
    }

    @EventHandler
    public void onPlayerChangeHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        try {
            if (event.isSprinting()) {
                if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    event.setCancelled(true);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleFly(PlayerToggleFlightEvent event) {
        try {
            if (event.isFlying()) {
                final Player player = event.getPlayer();
                if (player.getGameMode() != GameMode.CREATIVE) {
                    event.setCancelled(true);
                    final String playerName = player.getName();
                    if (!this.jumpDelay.containsKey(player.getName())) {
                        this.jumpDelay.put(player.getName(), player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
                            public void run() {
                                jumpDelay.remove(playerName);
                                if (player != null && player.isOnline())
                                    player.setAllowFlight(true);
                            }
                        }, (long) (this.getPlugin().getSettings().getDoubleJumpDelay() * 20L)).getTaskId());
                        player.setVelocity(player.getLocation().getDirection().multiply(this.getPlugin().getSettings().getDoubleJumpVelocity()).setY(this.getPlugin().getSettings().getDoubleJumpYHeight()));
                        player.setAllowFlight(false);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangeGamemode(PlayerGameModeChangeEvent event) {
        try {
            if (event.getNewGameMode() != GameMode.CREATIVE) {
                final Player player = event.getPlayer();
                player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
                    public void run() {
                        if (player != null && player.isOnline()) player.setAllowFlight(true);
                    }
                }, 1L);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerClickInventory(InventoryClickEvent event) {
        try {
            if (!ClassMenu.classMenu.containsKey(event.getWhoClicked().getName())) {
                if (event.getWhoClicked().isOp()) {
                    if (event.getInventory().getHolder() instanceof Player) {
                        if (!event.getWhoClicked().getName().equals(((Player) event.getInventory().getHolder()).getName()))
                            return;
                    }
                }
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
                final Player player = (Player) event.getWhoClicked();
                Utils.updateInventory(player);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        try {
            String playerPrefix = this.getPlugin().getChat().getPlayerPrefix(event.getPlayer());
            if (playerPrefix == null) playerPrefix = "";
            else if (ChatColor.stripColor(playerPrefix).length() <= 0) playerPrefix = "";
            playerPrefix = Utils.replaceChatColour(playerPrefix);
            String chatMessage = event.getMessage();
            if (event.getPlayer().hasPermission("essentials.chat.color"))
                chatMessage = Utils.replaceColours(chatMessage);
            else chatMessage = Utils.stripColours(chatMessage);
            if (event.getPlayer().hasPermission("essentials.chat.format"))
                chatMessage = Utils.replaceFormat(chatMessage);
            else chatMessage = Utils.stripFormat(chatMessage);
            if (Game.hasStarted()) {
                Team playerTeam = Game.getTeam(event.getPlayer());
                event.setFormat(ChatColor.DARK_GRAY + "[" + playerTeam.getColour() + playerTeam.getName() + ChatColor.DARK_GRAY + "] " + ChatColor.RESET + playerPrefix + ChatColor.RESET + "%s" + ChatColor.RESET + ": " + chatMessage);
            } else {
                event.setFormat(playerPrefix + ChatColor.RESET + "%s" + ChatColor.RESET + ": " + chatMessage);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRightClickEntity(PlayerInteractEntityEvent event) {
        try {
            if (!Game.hasStarted()) {
                event.setCancelled(true);
            } else if (event.getRightClicked() instanceof IronGolem) {
                event.setCancelled(true);
                IronGolem rightClicked = (IronGolem) event.getRightClicked();
                if (this.getPlugin().isTitan(rightClicked.getUniqueId())) {
                    if (Utils.isTitanOwner(rightClicked, event.getPlayer())) {
                        if (!Utils.inTitan(rightClicked, event.getPlayer())) {
                            for (PotionEffect potionEffect : event.getPlayer().getActivePotionEffects())
                                event.getPlayer().removePotionEffect(potionEffect.getType());
                            this.getPlugin().setInventory(event.getPlayer(), new PlayerInventoryData(event.getPlayer().getInventory().getContents(), event.getPlayer().getInventory().getArmorContents()));
                            event.getPlayer().getInventory().clear();
                            event.getPlayer().getInventory().setArmorContents(null);
                            rightClicked.setPassenger(event.getPlayer());
                            this.getPlugin().addPlayerToTitan(event.getPlayer());
                            try {
                                CustomIronGolem customIronGolem = (CustomIronGolem) ((CraftIronGolem) rightClicked).getHandle();
                                Titan titanType = customIronGolem.getTitanType();
                                if (titanType != null) {
                                    event.getPlayer().getInventory().setContents(customIronGolem.getInventory().getContents());
                                    if (customIronGolem.getAmmo() == -1) {
                                        customIronGolem.setAmmo(titanType.getAmmo());
                                    }
                                    event.getPlayer().setLevel(customIronGolem.getAmmo());
                                    event.getPlayer().addPotionEffects(titanType.getPotionEffects());
                                } else {
                                    customIronGolem.setAmmo(150);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            event.getPlayer().getInventory().setHeldItemSlot(0);
                        }
                        Utils.updateInventory(event.getPlayer());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerDismountTitan(EntityDismountEvent event) {
        try {
            if (event.getDismounted() != null && event.getDismounted() instanceof IronGolem) {
                if (!event.getDismounted().isDead() && event.getDismounted().isValid() && this.getPlugin().isTitan(event.getDismounted().getUniqueId())) {
                    final Player titanOwner = this.getPlugin().getTitanOwner(event.getDismounted().getUniqueId());
                    if (titanOwner != null) {
                        this.getPlugin().removePlayerFromTitan(titanOwner);
                        for (PotionEffect potionEffect : titanOwner.getActivePotionEffects()) {
                            titanOwner.removePotionEffect(potionEffect.getType());
                        }
                        if (Game.hasStarted()) {
                            titanOwner.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
                                public void run() {
                                    if (getPlugin().hasInventory(titanOwner)) {
                                        PlayerInventoryData playerInventory = getPlugin().setInventory(titanOwner, null);
                                        if (playerInventory != null) {
                                            ItemStack[] itemContents = playerInventory.getContents();
                                            titanOwner.getInventory().setContents(itemContents);
                                            titanOwner.getInventory().setArmorContents(playerInventory.getArmour());
                                            titanOwner.setLevel(0);
                                        }
                                    }
                                    titanOwner.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
                                    titanOwner.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                                }
                            }, 2L);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        try {
            event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onPlayerRespawn(final Player player, final boolean teleportPlayer) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        for (PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());
        Utils.resetPlayer(player);
        if (teleportPlayer) {
            if (Game.hasStarted())
                player.teleport(Game.getMap().getSpawn(Game.getTeam(player)), PlayerTeleportEvent.TeleportCause.PLUGIN);
            else
                player.teleport(this.getPlugin().getSettings().getLobbyLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
        player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
            public void run() {
                if (player != null && player.isOnline()) {
                    if (!teleportPlayer) {
                        if (Game.hasStarted()) {
                            player.teleport(Game.getMap().getSpawn(Game.getTeam(player)), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        } else {
                            player.teleport(getPlugin().getSettings().getLobbyLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        }
                    }
                    if (Game.hasStarted()) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                    }
                }
            }
        }, 1L);
        PlayerClass defaultClass = this.getPlugin().getClass(Main.DEFAULT_CLASS);
        if (Game.hasStarted()) {
            if (!this.getPlugin().hasClass(player))
                this.getPlugin().setClass(player, defaultClass);

            player.setAllowFlight(true);

            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));

            PlayerClass playerClass = this.getPlugin().getClass(player);
            if (playerClass == null) {
                this.getPlugin().setClass(player, defaultClass);
                playerClass = this.getPlugin().getClass(player);
            }
            player.getInventory().setContents(playerClass.getContents());
            player.getInventory().setArmorContents(playerClass.getArmour());
            player.getInventory().setItem(3, playerClass.getGrenade(Grenade.GrenadeType.FRAG));
            player.getInventory().setItem(4, playerClass.getGrenade(Grenade.GrenadeType.FLASH));
            player.getInventory().setItem(5, playerClass.getGrenade(Grenade.GrenadeType.STUN));
            player.getInventory().setItem(6, playerClass.getGrenade(Grenade.GrenadeType.INCENDIARY));
            player.getInventory().setItem(7, Utils.ItemUtils.setName(new ItemStack(Material.NETHER_STAR), "&aSpawn Titan"));
            player.getInventory().setItem(8, Utils.ItemUtils.setName(new ItemStack(Material.COMPASS), "&6Change Class"));

            player.getInventory().addItem(playerClass.getAmmo());
        }
        Utils.updateInventory(player, 0L);
        Game.updateScoreboards();
        Game.checkScores();

        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            if (!player.getName().equals(onlinePlayer.getName())) onlinePlayer.showPlayer(player);
        }
    }

    private void onPlayerLeave(Player player) {
        try {
            Bukkit.getServer().broadcastMessage(Lang.LEAVE_NORMAL.format(player.getName()));
            for (PotionEffect potionEffect : player.getActivePotionEffects())
                player.removePotionEffect(potionEffect.getType());
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.teleport(this.getPlugin().getSettings().getLobbyLocation());
            Utils.resetPlayer(player);
            player.setFoodLevel(20);
            player.setScoreboard(Main.getPlainScoreboard());
            Game.removePlayer(player);
            if (Game.hasStarted() || Game.isStarting()) {
                if (Game.isStarting())
                    Game.broadcastMessage(Lang.GENERAL_NOT_ENOUGH_PLAYERS.getMessage());

                int playersLeft = Game.getSize();
                List<Team> teamsLeft = Game.getTeamsLeft();
                if (Game.getSize() <= 0 || teamsLeft.size() <= 0) {
                    Game.endGame(Team.NONE);
                } else if (teamsLeft.size() < 2) {
                    Game.endGame(teamsLeft.get(0));
                }
                Game.updateScoreboards();
            }
            Utils.updateInventory(player, 0L);

            if (this.jumpDelay.containsKey(player.getName())) {
                int taskID = this.jumpDelay.remove(player.getName());
                if (Bukkit.getServer().getScheduler().isQueued(taskID) || Bukkit.getServer().getScheduler().isCurrentlyRunning(taskID))
                    Bukkit.getServer().getScheduler().cancelTask(taskID);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
