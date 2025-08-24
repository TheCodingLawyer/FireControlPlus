package me.confuser.banmanager.bukkit.tigerreports.managers;

import me.confuser.banmanager.bukkit.tigerreports.objects.users.User;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Collection;

/**
 * Users manager for TigerReports integration
 * Adapted from original TigerReports UsersManager class
 */
public class UsersManager {
    
    private final Map<UUID, User> onlineUsers = new HashMap<>();
    
    public void processUserConnection(Player player) {
        UUID uuid = player.getUniqueId();
        User user = new User(player);
        onlineUsers.put(uuid, user);
    }
    
    public void processUserDisconnection(Player player) {
        UUID uuid = player.getUniqueId();
        User user = onlineUsers.get(uuid);
        if (user != null && user.getOpenedMenu() != null) {
            player.closeInventory();
        }
        onlineUsers.remove(uuid);
    }
    
    public User getOnlineUser(Player player) {
        return onlineUsers.get(player.getUniqueId());
    }
    
    public User getOnlineUser(UUID uuid) {
        return onlineUsers.get(uuid);
    }
    
    public Collection<User> getUsers() {
        return onlineUsers.values();
    }
    
    public void closeAllMenus() {
        for (User user : onlineUsers.values()) {
            if (user.getOpenedMenu() != null && user.getPlayer() != null) {
                user.getPlayer().closeInventory();
            }
        }
    }
}
