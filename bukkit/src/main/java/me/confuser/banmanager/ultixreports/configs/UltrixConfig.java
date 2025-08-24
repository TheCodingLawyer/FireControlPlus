package me.confuser.banmanager.ultixreports.configs;

import me.confuser.banmanager.ultixreports.UltrixReports;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

/**
 * Main configuration class for UltrixReports
 * 
 * @author confuser
 */
public class UltrixConfig {
    
    private final File dataFolder;
    private FileConfiguration config;
    
    public UltrixConfig(File dataFolder) {
        this.dataFolder = dataFolder;
        loadConfig();
    }
    
    private void loadConfig() {
        File configFile = new File(dataFolder, "config.yml");
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    public void reload() {
        loadConfig();
    }
    
    // Report Settings
    public boolean isPermissionRequired() {
        return config.getBoolean("UltrixReports.PermissionRequired", false);
    }
    
    public boolean isReportOnlineOnly() {
        return config.getBoolean("UltrixReports.ReportOnline", false);
    }
    
    public int getMaxReports() {
        return config.getInt("UltrixReports.MaxReports", 100);
    }
    
    public int getMinCharacters() {
        return config.getInt("UltrixReports.MinCharacters", 3);
    }
    
    public int getReportCooldown() {
        return config.getInt("UltrixReports.ReportCooldown", 300);
    }
    
    public int getReportedImmunity() {
        return config.getInt("UltrixReports.ReportedImmunity", 120);
    }
    
    public List<String> getAutoCommands() {
        List<String> commands = config.getStringList("UltrixReports.AutoCommands");
        return commands.contains("none") ? null : commands;
    }
    
    // Abusive Report Settings
    public int getAbusiveReportCooldown() {
        return config.getInt("UltrixReports.AbusiveReport.Cooldown", 3600);
    }
    
    public List<String> getAbusiveCommands() {
        List<String> commands = config.getStringList("UltrixReports.AbusiveReport.Commands");
        return commands.contains("none") ? null : commands;
    }
    
    // Chat and History Settings
    public int getMessagesHistory() {
        return config.getInt("UltrixReports.MessagesHistory", 5);
    }
    
    public List<String> getCommandsHistory() {
        List<String> commands = config.getStringList("UltrixReports.CommandsHistory");
        return commands.contains("none") ? null : commands;
    }
    
    public boolean isStackReports() {
        return config.getBoolean("UltrixReports.StackReports", true);
    }
    
    public boolean isNotifyStackedReports() {
        return config.getBoolean("UltrixReports.NotifyStackedReports", true);
    }
    
    // Notification Settings
    public int getNotificationDelay() {
        return config.getInt("UltrixReports.Notifications.Delay", 2);
    }
    
    public boolean isPlayerNotificationsEnabled() {
        return config.getBoolean("UltrixReports.Notifications.Players.Enabled", true);
    }
    
    public boolean isHoverableReport() {
        return config.getBoolean("UltrixReports.Notifications.Players.HoverableReport", true);
    }
    
    public boolean isStaffNotificationsEnabled() {
        return config.getBoolean("UltrixReports.Notifications.Staff.Connection", true);
    }
    
    public int getStaffNotificationsInterval() {
        return config.getInt("UltrixReports.Notifications.Staff.MinutesInterval", 0);
    }
    
    // Menu Settings
    public int getMenuUpdatesInterval() {
        return config.getInt("UltrixReports.MenuUpdatesInterval", 10);
    }
    
    public boolean isOnlyDoneArchives() {
        return config.getBoolean("UltrixReports.OnlyDoneArchives", false);
    }
    
    public boolean isCloseMenuAfterProcessing() {
        return config.getBoolean("UltrixReports.CloseMenuAfterReportProcessing", false);
    }
    
    // Sound Settings
    public String getMenuSound() {
        return config.getString("UltrixReports.MenuSound", "ENTITY_ITEM_PICKUP");
    }
    
    public String getErrorSound() {
        return config.getString("UltrixReports.ErrorSound", "ENTITY_ITEM_BREAK");
    }
    
    public String getReportSound() {
        return config.getString("UltrixReports.ReportSound", "ENTITY_BAT_DEATH");
    }
    
    public String getTeleportSound() {
        return config.getString("UltrixReports.TeleportSound", "ENTITY_ENDERMEN_TELEPORT");
    }
    
    public String getStaffSound() {
        return config.getString("UltrixReports.StaffSound", "ENTITY_ITEM_PICKUP");
    }
    
    // Feature Settings
    public boolean isCustomReasons() {
        return config.getBoolean("UltrixReports.CustomReasons", true);
    }
    
    public boolean isCollectSkulls() {
        return config.getBoolean("UltrixReports.CollectSkulls", true);
    }
    
    public boolean isDisplayNameForStaff() {
        return config.getBoolean("UltrixReports.DisplayNameForStaff", true);
    }
    
    public boolean isDisplayNameForPlayers() {
        return config.getBoolean("UltrixReports.DisplayNameForPlayers", false);
    }
    
    // Punishment Settings
    public boolean isPunishmentsEnabled() {
        return config.getBoolean("UltrixReports.Punishments.Enabled", true);
    }
    
    public String getPunishmentsCommand() {
        String cmd = config.getString("UltrixReports.Punishments.PunishmentsCommand", "none");
        return "none".equals(cmd) ? null : cmd;
    }
    
    public boolean isDefaultReasonsPunishments() {
        return config.getBoolean("UltrixReports.Punishments.DefaultReasons", true);
    }
    
    // BungeeCord Settings
    public boolean isBungeeCordEnabled() {
        return config.getBoolean("BungeeCord.Enabled", false);
    }
    
    // MySQL Settings (for TigerReports compatibility)
    public boolean isMySQLEnabled() {
        String host = config.getString("MySQL.Host", "");
        return !host.isEmpty();
    }
    
    public String getMySQLHost() {
        return config.getString("MySQL.Host", "localhost");
    }
    
    public int getMySQLPort() {
        return config.getInt("MySQL.Port", 3306);
    }
    
    public String getMySQLDatabase() {
        return config.getString("MySQL.Database", "");
    }
    
    public String getMySQLUsername() {
        return config.getString("MySQL.Username", "");
    }
    
    public String getMySQLPassword() {
        return config.getString("MySQL.Password", "");
    }
    
    public boolean isMySQLUseSSL() {
        return config.getBoolean("MySQL.UseSSL", false);
    }
    
    // Vault Settings
    public boolean isVaultChatEnabled() {
        return config.getBoolean("VaultChat.Enabled", true);
    }
    
    public String getVaultChatFormat() {
        return config.getString("VaultChat.Format", "_Prefix__Name__Suffix_");
    }
    
    // Internal method to get raw config
    public FileConfiguration getConfig() {
        return config;
    }
} 