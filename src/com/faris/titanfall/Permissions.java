package com.faris.titanfall;

import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KingFaris10
 */
public class Permissions {
    private static List<Permission> permissionList = null;

    /**
     * Initialise the permissions list and register all permissions.
     */
    public static void init() {
        if (permissionList == null) permissionList = new ArrayList<>();
        else permissionList.clear();
		// TODO: Add permissions.
    }

    private static Permission registerPermission(Permission permission) {
        if (!permissionList.contains(permission)) permissionList.add(permission);
        return permission;
    }

    public static void clearPermissions() {
        permissionList.clear();
        permissionList = null;
    }

    public static List<Permission> getPermissions() {
        return permissionList;
    }

}
