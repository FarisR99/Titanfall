package com.faris.titanfall.helper;

import com.faris.titanfall.Main;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author KingFaris10
 */
public class Utils {
    private static final Random randomInstance = new Random(); // Random instance.

    public static Random getRandom() {
        return randomInstance;
    }

    public static String convertLocationToString(Location location) {
        return convertLocationToString(location, true, false);
    }

    public static String convertLocationToString(Location location, boolean includeYawAndPitch) {
        return convertLocationToString(location, includeYawAndPitch, false);
    }

    public static String convertLocationToString(Location location, boolean includeYawAndPitch, boolean roundNumbers) {
        String strLocation = "world " + 0D + " " + 100D + " " + 0D + (includeYawAndPitch ? " " + 0F + " " + 0F : "");
        if (location != null) {
            World world = location.getWorld();
            String strWorld = world == null ? "world" : world.getName();
            strLocation = strWorld + " ";
            strLocation += (roundNumbers ? (double) ((int) location.getX()) : location.getX()) + " ";
            strLocation += (roundNumbers ? (double) ((int) location.getY()) : location.getY()) + " ";
            strLocation += (roundNumbers ? (double) ((int) location.getZ()) : location.getZ()) + (includeYawAndPitch ? " " : "");
            if (includeYawAndPitch) strLocation += location.getYaw() + " " + location.getPitch();
        }
        return strLocation;
    }

    public static List<String> convertLocationListToStringList(List<Location> locationList, boolean includeYawAndPitch, boolean roundNumbers) {
        List<String> strLocationList = new ArrayList<>();
        for (Location location : locationList)
            strLocationList.add(convertLocationToString(location, includeYawAndPitch, roundNumbers));
        return strLocationList;
    }

    public static Location convertStringToLocation(String strLocation) {
        if (strLocation != null) {
            if (strLocation.contains(" ")) {
                String[] locationSplit = strLocation.split(" ");
                int splitLength = locationSplit.length;
                World world = null;
                double xPos = 0D, yPos = 100D, zPos = 0D;
                float yaw = 0F, pitch = 0F;
                if (splitLength > 0) world = Bukkit.getWorld(locationSplit[0]);
                if (splitLength > 1) {
                    if (isDouble(locationSplit[1])) xPos = Double.parseDouble(locationSplit[1]);
                }
                if (splitLength > 2) {
                    if (isDouble(locationSplit[2])) yPos = Double.parseDouble(locationSplit[2]);
                }
                if (splitLength > 3) {
                    if (isDouble(locationSplit[3])) zPos = Double.parseDouble(locationSplit[3]);
                }
                if (splitLength > 4) {
                    if (isFloat(locationSplit[4])) yaw = Float.parseFloat(locationSplit[4]);
                }
                if (splitLength > 5) {
                    if (isFloat(locationSplit[5])) pitch = Float.parseFloat(locationSplit[5]);
                }
                return new Location(world, xPos, yPos, zPos, yaw, pitch);
            }
        }
        return Bukkit.getWorlds().isEmpty() ? null : new Location(Bukkit.getWorlds().get(0), 0D, 100D, 0D);
    }

    public static List<Location> convertStringListToLocationList(List<String> strLocationList) {
        List<Location> locationList = new ArrayList<>();
        for (String strLocation : strLocationList) locationList.add(convertStringToLocation(strLocation));
        return locationList;
    }

    public static boolean inTitan(IronGolem ironGolem, Player player) {
        if (ironGolem != null && player != null) {
            if (ironGolem.getPassenger() != null && ironGolem.getPassenger() instanceof Player) {
                return ((Player) ironGolem.getPassenger()).getName().equalsIgnoreCase(player.getName());
            }
        }
        return false;
    }

    @Deprecated
    /**
     * Use Main.getInstance().inTitan(player);
     */
    public static boolean inTitan(Player player) {
        if (player != null) {
            if (Main.getInstance().hasTitan(player)) {
                return inTitan((IronGolem) Main.getInstance().getTitan(player).getBukkitEntity().getHandle(), player);
            }
        }
        return false;
    }

    public static boolean isDouble(String aString) {
        try {
            Double.parseDouble(aString);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isFloat(String aString) {
        try {
            Float.parseFloat(aString);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isInteger(String aString) {
        try {
            Integer.parseInt(aString);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isTitanOwner(LivingEntity titan, Player player) {
        if (player != null && titan != null) {
            Player titanOwner = Main.getInstance().getTitanOwner(titan.getUniqueId());
            if (titanOwner != null) return player.getUniqueId().equals(titanOwner.getUniqueId());
        }
        return false;
    }

    public static Location moveLocation(Location start, float distance) {
        Location ret = start.clone();
        return ret.add(ret.getDirection().normalize().multiply(distance).toLocation(start.getWorld()));
    }

    public static String replaceColours(String aString) {
        char[] b = aString.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFf".indexOf(b[i + 1]) > -1) {
                b[i] = ChatColor.COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    public static String replaceFormat(String aString) {
        char[] b = aString.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "KkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = ChatColor.COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    public static String replaceChatColour(String aString) {
        if (aString == null) return "";
        return ChatColor.translateAlternateColorCodes('&', aString);
    }

    public static List<String> replaceChatColours(List<String> someStrings) {
        List<String> stringList = new ArrayList<>();
        if (someStrings != null) {
            for (String aString : someStrings) {
                if (aString != null)
                    stringList.add(ChatColor.translateAlternateColorCodes('&', aString));
            }
        }
        return stringList;
    }

    public static void resetPlayer(Player player) {
        player.getInventory().setHeldItemSlot(0);
        player.setAllowFlight(false);
        player.setExp(0F);
        player.setLevel(0);
        player.setTotalExperience(0);
        player.setMaxHealth(20D);
        player.setHealth(20D);
        player.setFoodLevel(20);
    }

    public static void sendPlayerToHub(Player player) {
        sendPlayerToHub(player, null);
    }

    public static void sendPlayerToHub(Player player, String message) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Connect");
            out.writeUTF(Main.getInstance().getSettings().getHub());
            player.sendPluginMessage(Main.getInstance(), "BungeeCord", b.toByteArray());
        } catch (Exception ex) {
            player.kickPlayer(ChatColor.RED + "Error: Could not send you to the hub server!\nYou have been kicked instead.");
        }
    }

    private static final Pattern STRIP_COLOUR_PATTERN = Pattern.compile("(?i)" + String.valueOf(ChatColor.COLOR_CHAR) + "[0-9A-F]");
    private static final Pattern STRIP_FORMAT_PATTERN = Pattern.compile("(?i)" + String.valueOf(ChatColor.COLOR_CHAR) + "[K-OR]");

    public static String stripColours(final String aString) {
        if (aString == null) {
            return null;
        }

        return STRIP_COLOUR_PATTERN.matcher(aString).replaceAll("");
    }

    public static String stripFormat(final String aString) {
        if (aString == null) {
            return null;
        }

        return STRIP_FORMAT_PATTERN.matcher(aString).replaceAll("");
    }

    public static void updateInventory(Player player) {
        updateInventory(player, 3L);
    }

    public static void updateInventory(final Player player, long delay) {
        Validate.notNull(player);
        if (delay > 0) {
            player.getServer().getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                public void run() {
                    if (player != null && player.isOnline()) player.updateInventory();
                }
            }, delay);
        } else {
            player.updateInventory();
        }
    }

    public static class ItemUtils {
        public static String getName(ItemStack itemStack) {
            if (itemStack != null) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    if (itemMeta.hasDisplayName()) return itemMeta.getDisplayName();
                    else return "";
                }
            }
            return "";
        }

        public static ItemStack setName(ItemStack itemStack, String itemName) {
            if (itemStack != null && itemName != null) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    itemMeta.setDisplayName(replaceChatColour(itemName));
                    itemStack.setItemMeta(itemMeta);
                }
            }
            return itemStack;
        }

        public static ItemStack setLores(ItemStack itemStack, List<String> itemLores) {
            if (itemStack != null && itemLores != null) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    itemMeta.setLore(replaceChatColours(itemLores));
                    itemStack.setItemMeta(itemMeta);
                }
            }
            return itemStack;
        }
    }

    public static class RandomUtils {
        public static double getRandomDouble() {
            return randomInstance.nextInt() + Math.random();
        }

        public static double getRandomDouble(double minimumNumber, double maximumNumber) {
            if (minimumNumber < 0D) minimumNumber = 0D;
            if (maximumNumber < 0.01D) maximumNumber = 0.01D;
            if (minimumNumber != maximumNumber) {
                minimumNumber = Math.min(minimumNumber, maximumNumber);
                maximumNumber = Math.max(minimumNumber, maximumNumber);
                return minimumNumber + (maximumNumber - minimumNumber) * randomInstance.nextDouble();
            }
            return maximumNumber;
        }

        public static int getRandomInteger() {
            return randomInstance.nextInt();
        }

        public static int getRandomInteger(int minimumNumber, int maximumNumber) {
            minimumNumber = Math.min(minimumNumber, maximumNumber);
            maximumNumber = Math.max(minimumNumber, maximumNumber);
            if (minimumNumber != maximumNumber) {
                if (maximumNumber < 1) maximumNumber = 1;
                return randomInstance.nextInt(maximumNumber - minimumNumber + 1) + minimumNumber;
            } else {
                return maximumNumber;
            }
        }
    }
}