package me.confuser.banmanager.bukkit.aichatmod;

import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

/**
 * Configuration for AI Chat Moderation system
 */
public class AiChatModConfig {
    
    private final File dataFolder;
    private final CommonLogger logger;
    private YamlConfiguration conf;
    
    @Getter
    private boolean enabled;
    
    // Claude API settings
    @Getter
    private String apiKey;
    @Getter
    private String model;
    @Getter
    private int maxTokens;
    @Getter
    private int timeout;
    
    // Monitoring settings
    @Getter
    private int checkInterval;
    @Getter
    private int maxQueueSize;
    @Getter
    private boolean playerMessagesOnly;
    
    // Filters
    @Getter
    private List<String> badWords;
    @Getter
    private List<String> patterns;
    @Getter
    private List<String> categories;
    
    // Punishments
    @Getter
    private int offenseWindow;
    @Getter
    private Map<Integer, PunishmentLevel> punishmentLevels;
    @Getter
    private Map<String, SeverityOverride> severityOverrides;
    
    // Notifications
    @Getter
    private boolean statusMessageEnabled;
    @Getter
    private int statusMessageInterval;
    @Getter
    private String statusMessage;
    @Getter
    private boolean actionAlertsEnabled;
    @Getter
    private String actionAlertMessage;
    
    // Exemptions
    @Getter
    private List<String> exemptPermissions;
    @Getter
    private boolean exemptOps;
    @Getter
    private List<String> exemptGroups;
    
    // Logging
    @Getter
    private boolean logToConsole;
    @Getter
    private boolean logToFile;
    @Getter
    private String logFilePath;
    @Getter
    private boolean logAnalyzedMessages;
    
    // Advanced
    @Getter
    private String customPrompt;
    @Getter
    private double temperature;
    @Getter
    private double confidenceThreshold;
    @Getter
    private int maxApiCallsPerMinute;
    @Getter
    private int retryAttempts;
    @Getter
    private int retryDelay;
    
    public AiChatModConfig(File dataFolder, CommonLogger logger) {
        this.dataFolder = dataFolder;
        this.logger = logger;
    }
    
    public void load(InputStreamReader defaultConfig) {
        File configFile = new File(dataFolder, "aichatmod.yml");
        
        if (!configFile.exists()) {
            try {
                // Create default config
                configFile.getParentFile().mkdirs();
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defaultConfig);
                defConfig.save(configFile);
            } catch (IOException e) {
                logger.warning("Failed to save default aichatmod.yml: " + e.getMessage());
            }
        }
        
        conf = YamlConfiguration.loadConfiguration(configFile);
        
        // Merge with defaults
        if (defaultConfig != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defaultConfig);
            conf.setDefaults(defConfig);
            conf.options().copyDefaults(true);
            try {
                conf.save(configFile);
            } catch (IOException e) {
                logger.warning("Failed to save aichatmod.yml: " + e.getMessage());
            }
        }
        
        loadValues();
    }
    
    private void loadValues() {
        enabled = conf.getBoolean("enabled", false);
        
        // Claude API
        apiKey = conf.getString("claude.api-key", "");
        model = conf.getString("claude.model", "claude-haiku-4-5-20251001");
        maxTokens = conf.getInt("claude.max-tokens", 150);
        timeout = conf.getInt("claude.timeout", 10);
        
        // Monitoring
        checkInterval = conf.getInt("monitoring.check-interval", 5);
        maxQueueSize = conf.getInt("monitoring.max-queue-size", 20);
        playerMessagesOnly = conf.getBoolean("monitoring.player-messages-only", true);
        
        // Filters
        badWords = conf.getStringList("filters.bad-words");
        patterns = conf.getStringList("filters.patterns");
        categories = conf.getStringList("filters.categories");
        
        // Punishments
        offenseWindow = conf.getInt("punishments.offense-window", 60);
        punishmentLevels = new HashMap<>();
        ConfigurationSection levelsSection = conf.getConfigurationSection("punishments.levels");
        if (levelsSection != null) {
            for (String key : levelsSection.getKeys(false)) {
                try {
                    int level = Integer.parseInt(key);
                    ConfigurationSection levelConf = levelsSection.getConfigurationSection(key);
                    punishmentLevels.put(level, new PunishmentLevel(
                        levelConf.getString("action", "warn"),
                        levelConf.getString("duration", ""),
                        levelConf.getString("reason", "[AI Mod] Violation detected")
                    ));
                } catch (NumberFormatException ignored) {}
            }
        }
        
        // Severity overrides
        severityOverrides = new HashMap<>();
        ConfigurationSection overridesSection = conf.getConfigurationSection("severity-overrides");
        if (overridesSection != null) {
            for (String category : overridesSection.getKeys(false)) {
                ConfigurationSection override = overridesSection.getConfigurationSection(category);
                severityOverrides.put(category.toLowerCase(), new SeverityOverride(
                    override.getString("action", "mute"),
                    override.getString("duration", "1h"),
                    override.getString("reason", "[AI Mod] Severe violation")
                ));
            }
        }
        
        // Notifications
        statusMessageEnabled = conf.getBoolean("notifications.status-message.enabled", true);
        statusMessageInterval = conf.getInt("notifications.status-message.interval", 15);
        statusMessage = conf.getString("notifications.status-message.message", "&7[&aAI Chat Mod&7] &8System is active");
        actionAlertsEnabled = conf.getBoolean("notifications.action-alerts.enabled", true);
        actionAlertMessage = conf.getString("notifications.action-alerts.message", "&7[&cAI Mod&7] &e%player% &7received &c%action% &7for &f%category%");
        
        // Exemptions
        exemptPermissions = conf.getStringList("exemptions.permissions");
        exemptOps = conf.getBoolean("exemptions.exempt-ops", true);
        exemptGroups = conf.getStringList("exemptions.exempt-groups");
        
        // Logging
        logToConsole = conf.getBoolean("logging.console", true);
        logToFile = conf.getBoolean("logging.file", true);
        logFilePath = conf.getString("logging.file-path", "logs/aichatmod.log");
        logAnalyzedMessages = conf.getBoolean("logging.log-analyzed-messages", false);
        
        // Advanced
        customPrompt = conf.getString("advanced.custom-prompt", "");
        temperature = conf.getDouble("advanced.temperature", 0.1);
        confidenceThreshold = conf.getDouble("advanced.confidence-threshold", 0.7);
        maxApiCallsPerMinute = conf.getInt("advanced.max-api-calls-per-minute", 30);
        retryAttempts = conf.getInt("advanced.retry-attempts", 2);
        retryDelay = conf.getInt("advanced.retry-delay", 1000);
    }
    
    public void reload(InputStreamReader defaultConfig) {
        load(defaultConfig);
    }
    
    /**
     * Punishment level configuration
     */
    @Getter
    public static class PunishmentLevel {
        private final String action;
        private final String duration;
        private final String reason;
        
        public PunishmentLevel(String action, String duration, String reason) {
            this.action = action;
            this.duration = duration;
            this.reason = reason;
        }
    }
    
    /**
     * Severity override for specific violation types
     */
    @Getter
    public static class SeverityOverride {
        private final String action;
        private final String duration;
        private final String reason;
        
        public SeverityOverride(String action, String duration, String reason) {
            this.action = action;
            this.duration = duration;
            this.reason = reason;
        }
    }
}

