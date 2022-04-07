package me.hsgamer.gamesinthebox;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class Permissions {
    public static final Permission SKIP_TIME = new Permission("gitb.skiptime", PermissionDefault.OP);
    public static final Permission RELOAD = new Permission("gitb.reload", PermissionDefault.OP);
    public static final Permission SET_GAME = new Permission("gitb.setgame", PermissionDefault.OP);
    public static final Permission EDITOR = new Permission("gitb.editor", PermissionDefault.OP);

    private Permissions() {
        // EMPTY
    }

    public static void addPermissions() {
        Bukkit.getPluginManager().addPermission(SKIP_TIME);
        Bukkit.getPluginManager().addPermission(RELOAD);
        Bukkit.getPluginManager().addPermission(SET_GAME);
        Bukkit.getPluginManager().addPermission(EDITOR);
    }

    public static void removePermissions() {
        Bukkit.getPluginManager().removePermission(SKIP_TIME);
        Bukkit.getPluginManager().removePermission(RELOAD);
        Bukkit.getPluginManager().removePermission(SET_GAME);
        Bukkit.getPluginManager().removePermission(EDITOR);
    }
}
