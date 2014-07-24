package com.faris.titanfall.listener;

import com.faris.titanfall.Game;
import com.faris.titanfall.Lang;
import com.faris.titanfall.Main;
import com.faris.titanfall.equipment.Titan;
import com.faris.titanfall.helper.Utils;
import com.faris.titanfall.helper.effects.ParticleEffect;
import com.faris.titanfall.helper.inventory.PlayerInventoryData;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * @author KingFaris10
 */
public class EventListener implements Listener {

    private Main getPlugin() {
        return Main.getInstance();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.getDrops().clear();
        event.setDroppedExp(0);

        if (event.getEntity() instanceof IronGolem) {
            IronGolem ironGolem = (IronGolem) event.getEntity();
            if (this.getPlugin().isTitan(ironGolem.getUniqueId())) {
                Titan titanType = this.getPlugin().getTitanType(ironGolem.getCustomName());
                Player titanOwner = this.getPlugin().getTitanOwner(ironGolem.getUniqueId());
                if (titanOwner != null) {
                    if (Utils.inTitan(ironGolem, titanOwner)) {
                        titanOwner.getInventory().clear();
                        titanOwner.getInventory().setArmorContents(null);
                        titanOwner.getInventory().setHeldItemSlot(0);
                        for (PotionEffect potionEffect : titanOwner.getActivePotionEffects()) {
                            titanOwner.removePotionEffect(potionEffect.getType());
                        }
                        titanOwner.eject();
                        if (titanType != null) {
                            if (titanType.getMode() == Titan.TitanMode.AUTO_EJECT) {
                                titanOwner.setVelocity(titanOwner.getVelocity().setY(5F));
                            } else if (titanType.getMode() == Titan.TitanMode.AUTO_EXPLODE) {
                                try {
                                    ParticleEffect.sendToLocation(ParticleEffect.LARGE_EXPLODE, ironGolem.getLocation(), 0F, 1F, 0F, 10, 1);
                                } catch (Exception ex) {
                                }
                                List<Entity> nearbyEntities = titanOwner.getNearbyEntities(7.5D, 5D, 7.5D);
                                for (Entity nearbyEntity : nearbyEntities) {
                                    if (nearbyEntity instanceof LivingEntity) {
                                        LivingEntity nearbyLivingEntity = (LivingEntity) nearbyEntity;
                                        double explosionDamage = 6D;
                                        if (nearbyLivingEntity.getUniqueId().compareTo(titanOwner.getUniqueId()) != 0) {
                                            if (nearbyLivingEntity instanceof IronGolem) {
                                                nearbyLivingEntity.damage(explosionDamage / 2);
                                            } else {
                                                nearbyLivingEntity.damage(explosionDamage);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (Game.hasStarted() && this.getPlugin().hasInventory(titanOwner)) {
                            PlayerInventoryData playerInventory = this.getPlugin().setInventory(titanOwner, null);
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
                    titanOwner.sendMessage(ChatColor.RED + "Your titan has been destroyed!");
                }
                ironGolem.setPassenger(null);
                this.getPlugin().removePlayerTitan(titanOwner);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamaged(EntityDamageEvent event) {
        try {
            if (event.isCancelled()) return;
            if (!Game.hasStarted()) {
                event.setCancelled(true);
            } else if (Game.getTime() <= Main.getInstance().getSettings().getInvincibilityTime()) {
                event.setCancelled(true);
            } else {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    if (event.getEntity() instanceof Player) {
                        Player entity = (Player) event.getEntity();
                        if (entity.getFallDistance() <= 30F) {
                            event.setCancelled(true);
                            return;
                        } else {
                            event.setDamage(2D);
                        }
                    }
                }
                if (event instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent eEvent = (EntityDamageByEntityEvent) event;
                    if (eEvent.getDamager() instanceof Player) {
                        event.setDamage(2D);
                    }
                }
                if (event.getEntity() instanceof Player) {
                    Player damaged = (Player) event.getEntity();
                    if (this.getPlugin().hasTitan(damaged)) {
                        IronGolem titan = (IronGolem) this.getPlugin().getTitan(damaged).getBukkitEntity().getHandle();
                        if (titan != null) {
                            if (Utils.inTitan(titan, damaged)) {
                                titan.damage(event.getDamage(), event instanceof EntityDamageByEntityEvent ? ((EntityDamageByEntityEvent) event).getDamager() : null);
                                event.setCancelled(true);
                            }
                        }
                    }
                } else if (event instanceof EntityDamageByEntityEvent && event.getEntity() instanceof IronGolem) {
                    EntityDamageByEntityEvent eEvent = (EntityDamageByEntityEvent) event;
                    if (eEvent.getDamager() instanceof Player) {
                        IronGolem titan = (IronGolem) event.getEntity();
                        if (this.getPlugin().isTitan(titan.getUniqueId())) {
                            Player titanOwner = this.getPlugin().getTitanOwner(titan.getUniqueId());
                            if (titanOwner != null) {
                                if (Game.getTeam(titanOwner).getID() == Game.getTeam((Player) ((EntityDamageByEntityEvent) event).getDamager()).getID()) {
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                }
            }
            if (!event.isCancelled() && event.getDamage() > 0) {
                if (event.getEntity() instanceof Player) {
                    Player entity = (Player) event.getEntity();
                    if (entity.getHealth() - event.getDamage() < 1D) {
                        event.setCancelled(true);
                        if (event instanceof EntityDamageByEntityEvent) {
                            EntityDamageByEntityEvent eEvent = (EntityDamageByEntityEvent) event;
                            Player damager = null;
                            if (eEvent.getDamager() instanceof Projectile) {
                                Projectile bullet = (Projectile) eEvent.getDamager();
                                if (bullet.getShooter() != null && bullet.getShooter() instanceof Player)
                                    damager = (Player) bullet.getShooter();
                            } else if (eEvent.getDamager() instanceof Player) {
                                damager = (Player) eEvent.getDamager();
                            }
                            if (damager != null) {
                                if (!damager.getName().equals(entity.getName())) {
                                    int killPoints = Main.getInstance().getSettings().getKillPoints();
                                    Game.getTeam(damager).addScore(Main.getInstance().getSettings().getKillPoints());
                                    Lang.sendMessage(damager, Lang.GENERAL_PLAYER_KILL, entity.getName(), String.valueOf(killPoints));
                                    Lang.sendMessage(entity, Lang.GENERAL_PLAYER_DEATH, damager.getName());
                                }
                            }
                        }
                        this.getPlugin().getPlayerListener().onPlayerRespawn(entity, true);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBulletHit(WeaponDamageEntityEvent event) {
        try {
            Player shooter = event.getPlayer();
            if (event.getVictim() instanceof Player) {
                Player damaged = (Player) event.getVictim();
                if (Game.getTeam(shooter).getID() == Game.getTeam(damaged).getID()) {
                    event.setCancelled(true);
                    Lang.sendMessage(shooter, Lang.TEAM_FRIENDLY_FIRE, damaged.getName());
                }
            } else if (event.getVictim() instanceof IronGolem) {
                if (Utils.isTitanOwner((IronGolem) event.getVictim(), shooter)) {
                    this.getPlugin().getCSUtility();
                    event.setCancelled(true);
                } else {
                    if (!event.getWeaponTitle().endsWith(" - Titan"))
                        event.setDamage(event.getDamage() * Utils.RandomUtils.getRandomDouble(0.7D, 0.8D));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Egg) {
            this.getPlugin().getPlayerListener().grenadeLocations.put(event.getEntity().getUniqueId(), event.getEntity().getLocation());
            this.getPlugin().getPlayerListener().grenadeItems.put(event.getEntity().getUniqueId(), event.getEntity().getWorld().dropItem(event.getEntity().getLocation().add(0D, 0.5D, 0D), new ItemStack(Main.GRENADE_MATERIAL)).getUniqueId());
            event.getEntity().remove();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        try {
            if (!event.getPlayer().isOp()) event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        try {
            event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockDamaged(BlockDamageEvent event) {
        try {
            if (!event.getPlayer().isOp()) event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockDecay(LeavesDecayEvent event) {
        try {
            event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockEntityForm(EntityBlockFormEvent event) {
        try {
            event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockEXP(BlockExpEvent event) {
        try {
            event.setExpToDrop(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        try {
            event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockMove(BlockFromToEvent event) {
        try {
            if (event.getBlock().getType() == Material.DRAGON_EGG) event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        try {
            event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        try {
            if (event.getPlayer() == null || (event.getPlayer() != null && !event.getPlayer().isOp()))
                event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        try {
            if (!event.getPlayer().isOp()) event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        try {
            event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {
        try {
            event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        try {
            event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onEntityCreatePortal(EntityCreatePortalEvent event) {
        try {
            event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        try {
            event.blockList().clear();
            event.setYield(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        try {
            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DISPENSE_EGG || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG)
                event.setCancelled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (!Game.hasStarted()) {
            event.setCancelled(true);
        } else if (event.getReason() != EntityTargetEvent.TargetReason.CUSTOM) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTargetEntity(EntityTargetLivingEntityEvent event) {
        if (!Game.hasStarted()) {
            event.setCancelled(true);
        } else if (event.getEntityType() == EntityType.IRON_GOLEM) {
            event.setCancelled(true);
        }
    }
}