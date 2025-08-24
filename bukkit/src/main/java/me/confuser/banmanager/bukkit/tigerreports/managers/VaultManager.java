package me.confuser.banmanager.bukkit.tigerreports.managers;

/**
 * Vault manager for TigerReports integration
 * Adapted from original TigerReports VaultManager class
 */
public class VaultManager {
    
    private final boolean vaultEnabled;
    
    public VaultManager(boolean vaultEnabled) {
        this.vaultEnabled = vaultEnabled;
    }
    
    public boolean isVaultEnabled() {
        return vaultEnabled;
    }
    
    public String getPlayerDisplayName(String playerName) {
        // For now, just return the player name
        // This can be enhanced with Vault integration later
        return playerName;
    }
    
    public String getPlayerPrefix(String playerName) {
        return "";
    }
    
    public String getPlayerSuffix(String playerName) {
        return "";
    }
}
