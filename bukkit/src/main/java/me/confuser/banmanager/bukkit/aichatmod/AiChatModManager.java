package me.confuser.banmanager.bukkit.aichatmod;

import lombok.Getter;
import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.gson.Gson;
import me.confuser.banmanager.common.gson.JsonArray;
import me.confuser.banmanager.common.gson.JsonObject;
import me.confuser.banmanager.common.gson.JsonParser;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Main manager for AI Chat Moderation
 * Handles Claude API calls, message analysis, and punishment execution
 */
public class AiChatModManager {
    
    private final BMBukkitPlugin bukkitPlugin;
    private final BanManagerPlugin plugin;
    private final AiChatModConfig config;
    
    @Getter
    private AiModActionStorage actionStorage;
    
    private final Gson gson = new Gson();
    private final ExecutorService apiExecutor;
    private final ConcurrentLinkedQueue<QueuedMessage> messageQueue = new ConcurrentLinkedQueue<>();
    private final List<Pattern> compiledPatterns = new ArrayList<>();
    
    // Rate limiting
    private final LinkedList<Long> apiCallTimestamps = new LinkedList<>();
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    
    // Status message tracking
    private long lastStatusMessageTime = 0;
    
    // Punishment cooldown tracking (UUID -> last punishment time)
    // Prevents spamming punishments for same player
    private final ConcurrentHashMap<UUID, Long> punishmentCooldowns = new ConcurrentHashMap<>();
    private static final long PUNISHMENT_COOLDOWN_MS = 30000; // 30 seconds between punishments for same player
    
    @Getter
    private boolean running = false;
    
    public AiChatModManager(BMBukkitPlugin bukkitPlugin, AiChatModConfig config) {
        this.bukkitPlugin = bukkitPlugin;
        this.plugin = bukkitPlugin.getPlugin();
        this.config = config;
        this.apiExecutor = Executors.newFixedThreadPool(2);
        
        // Compile regex patterns
        for (String pattern : config.getPatterns()) {
            try {
                compiledPatterns.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid regex pattern: " + pattern);
            }
        }
    }
    
    /**
     * Initialize the AI Chat Mod system
     */
    public void enable() {
        if (!config.isEnabled()) {
            plugin.getLogger().info("AI Chat Mod is disabled in config");
            return;
        }
        
        if (config.getApiKey().isEmpty() || config.getApiKey().equals("your-api-key-here")) {
            plugin.getLogger().warning("AI Chat Mod enabled but API key not configured!");
            return;
        }
        
        // Initialize storage
        try {
            actionStorage = new AiModActionStorage(plugin);
            plugin.getLogger().info("AI Chat Mod action storage initialized");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize AI Chat Mod storage: " + e.getMessage());
            return;
        }
        
        running = true;
        
        // Start message processing task
        Bukkit.getScheduler().runTaskTimerAsynchronously(bukkitPlugin, this::processMessageQueue, 
            20L * config.getCheckInterval(), 20L * config.getCheckInterval());
        
        // Start status message task
        if (config.isStatusMessageEnabled()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(bukkitPlugin, this::sendStatusMessage,
                20L * 60, 20L * 60 * config.getStatusMessageInterval());
        }
        
        // Start cache cleanup task
        Bukkit.getScheduler().runTaskTimerAsynchronously(bukkitPlugin, 
            () -> actionStorage.cleanupOffenseCache(config.getOffenseWindow()),
            20L * 300, 20L * 300); // Every 5 minutes
        
        plugin.getLogger().info("AI Chat Mod enabled with model: " + config.getModel());
    }
    
    /**
     * Disable the AI Chat Mod system
     */
    public void disable() {
        running = false;
        apiExecutor.shutdown();
        try {
            if (!apiExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                apiExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            apiExecutor.shutdownNow();
        }
    }
    
    /**
     * Queue a message for analysis
     * Skips if player is already muted/banned or on punishment cooldown
     */
    public void queueMessage(UUID playerUuid, String playerName, String message) {
        if (!running) return;
        
        // IMPORTANT: Skip if player is already muted - they shouldn't be able to speak anyway
        // This prevents spam in the punishment list
        if (plugin.getPlayerMuteStorage().isMuted(playerUuid)) {
            if (config.isLogToConsole()) {
                plugin.getLogger().info("[AI Mod] Skipping already muted player: " + playerName);
            }
            return;
        }
        
        // Skip if player is banned (shouldn't happen but just in case)
        if (plugin.getPlayerBanStorage().isBanned(playerUuid)) {
            return;
        }
        
        // Check punishment cooldown - prevent spamming punishments for same player
        Long lastPunishment = punishmentCooldowns.get(playerUuid);
        if (lastPunishment != null && System.currentTimeMillis() - lastPunishment < PUNISHMENT_COOLDOWN_MS) {
            if (config.isLogToConsole()) {
                plugin.getLogger().info("[AI Mod] Skipping " + playerName + " - on punishment cooldown");
            }
            return;
        }
        
        // Quick filter check first (before AI) - only queue if potentially bad
        if (containsBadWord(message) || matchesPattern(message)) {
            messageQueue.add(new QueuedMessage(playerUuid, playerName, message, System.currentTimeMillis()));
            
            // Force process if queue is too large
            if (messageQueue.size() >= config.getMaxQueueSize()) {
                processMessageQueue();
            }
        }
        // Note: Removed always-queue behavior to reduce API calls and spam
        // Only messages containing bad words or matching patterns will be analyzed
    }
    
    /**
     * Process queued messages
     */
    private void processMessageQueue() {
        if (!running || messageQueue.isEmpty()) return;
        
        List<QueuedMessage> toProcess = new ArrayList<>();
        QueuedMessage msg;
        while ((msg = messageQueue.poll()) != null && toProcess.size() < config.getMaxQueueSize()) {
            toProcess.add(msg);
        }
        
        if (toProcess.isEmpty()) return;
        
        apiExecutor.submit(() -> {
            for (QueuedMessage message : toProcess) {
                try {
                    analyzeAndAct(message);
                } catch (Exception e) {
                    if (config.isLogToConsole()) {
                        plugin.getLogger().warning("Error analyzing message: " + e.getMessage());
                    }
                }
            }
        });
    }
    
    /**
     * Analyze a message and take action if needed
     */
    private void analyzeAndAct(QueuedMessage message) {
        // Check rate limit
        if (!canMakeApiCall()) {
            if (config.isLogToConsole()) {
                plugin.getLogger().warning("Rate limit reached, skipping AI analysis");
            }
            return;
        }
        
        try {
            ModerationResult result = analyzeWithClaude(message.playerName, message.message);
            
            if (result == null) return;
            
            if (config.isLogAnalyzedMessages()) {
                plugin.getLogger().info(String.format("[AI Mod] Analyzed %s: %s (Violation: %s, Confidence: %.2f)",
                    message.playerName, message.message, result.isViolation, result.confidence));
            }
            
            if (result.isViolation && result.confidence >= config.getConfidenceThreshold()) {
                executePunishment(message.playerUuid, message.playerName, message.message, result);
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to analyze message from " + message.playerName + ": " + e.getMessage());
        }
    }
    
    /**
     * Call Claude API to analyze a message
     */
    private ModerationResult analyzeWithClaude(String playerName, String message) {
        for (int attempt = 0; attempt <= config.getRetryAttempts(); attempt++) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(CLAUDE_API_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-api-key", config.getApiKey());
                conn.setRequestProperty("anthropic-version", "2023-06-01");
                conn.setConnectTimeout(config.getTimeout() * 1000);
                conn.setReadTimeout(config.getTimeout() * 1000);
                conn.setDoOutput(true);
                
                String prompt = buildModerationPrompt(playerName, message);
                JsonObject requestBody = buildRequestBody(prompt);
                
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(gson.toJson(requestBody).getBytes(StandardCharsets.UTF_8));
                }
                
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    recordApiCall();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        return parseClaudeResponse(response.toString());
                    }
                } else {
                    String errorMsg = "Claude API error: " + responseCode;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                        StringBuilder error = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            error.append(line);
                        }
                        errorMsg += " - " + error.toString();
                    } catch (Exception ignored) {}
                    
                    if (config.isLogToConsole()) {
                        plugin.getLogger().warning(errorMsg);
                    }
                }
                
            } catch (Exception e) {
                if (attempt < config.getRetryAttempts()) {
                    try {
                        Thread.sleep(config.getRetryDelay());
                    } catch (InterruptedException ignored) {}
                } else {
                    plugin.getLogger().warning("Claude API failed after " + (attempt + 1) + " attempts: " + e.getMessage());
                }
            }
        }
        
        return null;
    }
    
    /**
     * Build the moderation prompt for Claude
     */
    private String buildModerationPrompt(String playerName, String message) {
        if (!config.getCustomPrompt().isEmpty()) {
            return config.getCustomPrompt()
                .replace("{player}", playerName)
                .replace("{message}", message)
                .replace("{badwords}", String.join(", ", config.getBadWords()))
                .replace("{categories}", String.join(", ", config.getCategories()));
        }
        
        return String.format("""
            You are a chat moderation AI for a Minecraft server. Analyze the following chat message and determine if it violates server rules.
            
            Player: %s
            Message: %s
            
            Watch for these categories: %s
            Known bad words: %s
            
            Respond in JSON format ONLY (no other text):
            {
                "isViolation": true/false,
                "confidence": 0.0-1.0,
                "category": "category name or null",
                "severity": "low/medium/high/critical",
                "explanation": "brief explanation"
            }
            
            Consider context, intent, and severity. Avoid false positives. Only flag clear violations.
            """, playerName, message, 
            String.join(", ", config.getCategories()),
            String.join(", ", config.getBadWords()));
    }
    
    /**
     * Build the Claude API request body
     */
    private JsonObject buildRequestBody(String prompt) {
        JsonObject body = new JsonObject();
        body.addProperty("model", config.getModel());
        body.addProperty("max_tokens", config.getMaxTokens());
        body.addProperty("temperature", config.getTemperature());
        
        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);
        
        body.add("messages", messages);
        return body;
    }
    
    /**
     * Parse Claude's response into a ModerationResult
     */
    @SuppressWarnings("deprecation")
    private ModerationResult parseClaudeResponse(String response) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(response).getAsJsonObject();
            JsonArray content = json.getAsJsonArray("content");
            if (content != null && content.size() > 0) {
                String text = content.get(0).getAsJsonObject().get("text").getAsString();
                
                // Extract JSON from response (Claude might add extra text)
                int jsonStart = text.indexOf("{");
                int jsonEnd = text.lastIndexOf("}") + 1;
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    text = text.substring(jsonStart, jsonEnd);
                }
                
                JsonObject result = parser.parse(text).getAsJsonObject();
                return new ModerationResult(
                    result.get("isViolation").getAsBoolean(),
                    result.get("confidence").getAsDouble(),
                    result.has("category") && !result.get("category").isJsonNull() 
                        ? result.get("category").getAsString() : "unknown",
                    result.has("severity") ? result.get("severity").getAsString() : "medium",
                    result.has("explanation") ? result.get("explanation").getAsString() : ""
                );
            }
        } catch (Exception e) {
            if (config.isLogToConsole()) {
                plugin.getLogger().warning("Failed to parse Claude response: " + e.getMessage());
            }
        }
        return null;
    }
    
    /**
     * Execute punishment based on moderation result
     * Includes smart checks to prevent duplicate punishments
     */
    private void executePunishment(UUID playerUuid, String playerName, String originalMessage, ModerationResult result) {
        try {
            // SMART CHECK: Don't punish if player is already muted
            if (plugin.getPlayerMuteStorage().isMuted(playerUuid)) {
                if (config.isLogToConsole()) {
                    plugin.getLogger().info("[AI Mod] Skipping punishment - " + playerName + " is already muted");
                }
                return;
            }
            
            // SMART CHECK: Don't punish if player is already banned
            if (plugin.getPlayerBanStorage().isBanned(playerUuid)) {
                if (config.isLogToConsole()) {
                    plugin.getLogger().info("[AI Mod] Skipping punishment - " + playerName + " is already banned");
                }
                return;
            }
            
            // SMART CHECK: Don't punish if on cooldown (prevents rapid-fire punishments)
            Long lastPunishment = punishmentCooldowns.get(playerUuid);
            if (lastPunishment != null && System.currentTimeMillis() - lastPunishment < PUNISHMENT_COOLDOWN_MS) {
                if (config.isLogToConsole()) {
                    plugin.getLogger().info("[AI Mod] Skipping punishment - " + playerName + " on cooldown");
                }
                return;
            }
            
            // Check for severity override
            AiChatModConfig.SeverityOverride override = config.getSeverityOverrides().get(result.category.toLowerCase());
            
            String action;
            String duration;
            String reason;
            
            if (override != null && (result.severity.equals("high") || result.severity.equals("critical"))) {
                // Use severity override
                action = override.getAction();
                duration = override.getDuration();
                reason = override.getReason();
            } else {
                // Use escalation system
                int offenseCount = actionStorage.getOffenseCount(playerUuid, config.getOffenseWindow()) + 1;
                AiChatModConfig.PunishmentLevel level = config.getPunishmentLevels().get(
                    Math.min(offenseCount, config.getPunishmentLevels().size())
                );
                
                if (level == null) {
                    level = config.getPunishmentLevels().get(config.getPunishmentLevels().size());
                }
                
                action = level.getAction();
                duration = level.getDuration();
                reason = level.getReason();
            }
            
            // Get or create player data
            PlayerData playerData = plugin.getPlayerStorage().retrieve(playerName, true);
            if (playerData == null) {
                plugin.getLogger().warning("Could not find player data for " + playerName);
                return;
            }
            
            // Get console actor for punishments
            PlayerData actor = plugin.getPlayerStorage().getConsole();
            
            // Execute the punishment
            switch (action.toLowerCase()) {
                case "warn":
                    executeWarn(playerData, actor, reason);
                    break;
                case "mute":
                case "tempmute":
                    executeMute(playerData, actor, reason, duration);
                    break;
                case "tempban":
                case "ban":
                    executeBan(playerData, actor, reason, duration);
                    break;
                default:
                    plugin.getLogger().warning("Unknown punishment action: " + action);
                    return;
            }
            
            // Record punishment cooldown - IMPORTANT: prevents spam punishments
            punishmentCooldowns.put(playerUuid, System.currentTimeMillis());
            
            // Record the action
            int currentOffenseCount = actionStorage.getOffenseCount(playerUuid, config.getOffenseWindow()) + 1;
            AiModActionData actionData = new AiModActionData(
                playerUuid, playerName, action, duration, reason, result.category,
                originalMessage, result.confidence, result.explanation,
                currentOffenseCount, Bukkit.getServer().getName()
            );
            actionStorage.recordAction(actionData);
            
            // Notify admins
            if (config.isActionAlertsEnabled()) {
                notifyAdmins(playerName, action, result.category, reason);
            }
            
            // Log to console
            if (config.isLogToConsole()) {
                plugin.getLogger().info(String.format("[AI Mod] %s received %s for %s (offense #%d)",
                    playerName, action, result.category, currentOffenseCount));
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to execute AI mod punishment: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Execute a warning
     * Uses BanManager's message system from messages.yml
     */
    private void executeWarn(PlayerData player, PlayerData actor, String reason) throws SQLException {
        PlayerWarnData warning = new PlayerWarnData(player, actor, reason, 1.0, false);
        plugin.getPlayerWarnStorage().create(warning);
        
        // Notify player using BanManager's message format
        CommonPlayer commonPlayer = plugin.getServer().getPlayer(player.getUUID());
        if (commonPlayer != null && commonPlayer.isOnline()) {
            Message warnMessage = Message.get("warn.player.warned")
                .set("displayName", commonPlayer.getDisplayName())
                .set("player", player.getName())
                .set("playerId", player.getUUID().toString())
                .set("reason", warning.getReason())
                .set("actor", actor.getName())
                .set("id", warning.getId());
            
            commonPlayer.sendMessage(warnMessage.toString());
        }
    }
    
    /**
     * Execute a mute
     * Uses BanManager's message system from messages.yml
     */
    private void executeMute(PlayerData player, PlayerData actor, String reason, String duration) throws SQLException {
        long expires = 0;
        if (duration != null && !duration.isEmpty()) {
            try {
                expires = DateUtils.parseDateDiff(duration, true) / 1000L;
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid duration: " + duration);
            }
        }
        
        PlayerMuteData mute = new PlayerMuteData(player, actor, reason, true, false, expires);
        plugin.getPlayerMuteStorage().mute(mute);
        
        // Notify player using BanManager's message format
        CommonPlayer commonPlayer = plugin.getServer().getPlayer(player.getUUID());
        if (commonPlayer != null && commonPlayer.isOnline()) {
            Message muteMessage;
            if (expires > 0) {
                // Temp mute message
                muteMessage = Message.get("tempmute.player.disallowed")
                    .set("displayName", commonPlayer.getDisplayName())
                    .set("player", player.getName())
                    .set("playerId", player.getUUID().toString())
                    .set("reason", mute.getReason())
                    .set("actor", actor.getName())
                    .set("id", mute.getId())
                    .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
            } else {
                // Permanent mute message
                muteMessage = Message.get("mute.player.disallowed")
                    .set("displayName", commonPlayer.getDisplayName())
                    .set("player", player.getName())
                    .set("playerId", player.getUUID().toString())
                    .set("reason", mute.getReason())
                    .set("actor", actor.getName())
                    .set("id", mute.getId());
            }
            
            commonPlayer.sendMessage(muteMessage.toString());
        }
    }
    
    /**
     * Execute a ban
     * Uses BanManager's message system from messages.yml
     */
    private void executeBan(PlayerData player, PlayerData actor, String reason, String duration) throws SQLException {
        long expires = 0;
        if (duration != null && !duration.isEmpty()) {
            try {
                expires = DateUtils.parseDateDiff(duration, true) / 1000L;
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid duration: " + duration);
            }
        }
        
        PlayerBanData ban = new PlayerBanData(player, actor, reason, true, expires);
        plugin.getPlayerBanStorage().ban(ban);
        
        // Kick player using BanManager's message format
        CommonPlayer commonPlayer = plugin.getServer().getPlayer(player.getUUID());
        if (commonPlayer != null && commonPlayer.isOnline()) {
            Message kickMessage;
            if (expires > 0) {
                // Temp ban kick message
                kickMessage = Message.get("tempban.player.kick")
                    .set("displayName", commonPlayer.getDisplayName())
                    .set("player", player.getName())
                    .set("playerId", player.getUUID().toString())
                    .set("reason", ban.getReason())
                    .set("actor", actor.getName())
                    .set("id", ban.getId())
                    .set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
            } else {
                // Permanent ban kick message
                kickMessage = Message.get("ban.player.kick")
                    .set("displayName", commonPlayer.getDisplayName())
                    .set("player", player.getName())
                    .set("playerId", player.getUUID().toString())
                    .set("reason", ban.getReason())
                    .set("actor", actor.getName())
                    .set("id", ban.getId());
            }
            
            // Must kick on main thread
            final String finalKickMessage = kickMessage.toString();
            Bukkit.getScheduler().runTask(bukkitPlugin, () -> commonPlayer.kick(finalKickMessage));
        }
    }
    
    /**
     * Notify admins of an action
     */
    private void notifyAdmins(String playerName, String action, String category, String reason) {
        String message = ChatColor.translateAlternateColorCodes('&', config.getActionAlertMessage()
            .replace("%player%", playerName)
            .replace("%action%", action)
            .replace("%category%", category)
            .replace("%reason%", reason));
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("bm.notify.aichatmod") || player.isOp()) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * Send periodic status message to admins
     */
    private void sendStatusMessage() {
        if (!running || !config.isStatusMessageEnabled()) return;
        
        long now = System.currentTimeMillis();
        if (now - lastStatusMessageTime < config.getStatusMessageInterval() * 60 * 1000) {
            return;
        }
        lastStatusMessageTime = now;
        
        String message = ChatColor.translateAlternateColorCodes('&', config.getStatusMessage());
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("bm.admin") || player.isOp()) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * Check if a message contains a bad word
     */
    private boolean containsBadWord(String message) {
        String lower = message.toLowerCase();
        for (String badWord : config.getBadWords()) {
            if (lower.contains(badWord.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if a message matches any pattern
     */
    private boolean matchesPattern(String message) {
        for (Pattern pattern : compiledPatterns) {
            if (pattern.matcher(message).find()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if we can make an API call (rate limiting)
     */
    private synchronized boolean canMakeApiCall() {
        long now = System.currentTimeMillis();
        long oneMinuteAgo = now - 60000;
        
        // Remove old timestamps
        while (!apiCallTimestamps.isEmpty() && apiCallTimestamps.peek() < oneMinuteAgo) {
            apiCallTimestamps.poll();
        }
        
        return apiCallTimestamps.size() < config.getMaxApiCallsPerMinute();
    }
    
    /**
     * Record an API call timestamp
     */
    private synchronized void recordApiCall() {
        apiCallTimestamps.add(System.currentTimeMillis());
    }
    
    /**
     * Check if a player is exempt from moderation
     */
    public boolean isExempt(Player player) {
        // Check OP exemption
        if (config.isExemptOps() && player.isOp()) {
            return true;
        }
        
        // Check permissions
        for (String perm : config.getExemptPermissions()) {
            if (player.hasPermission(perm)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get config
     */
    public AiChatModConfig getConfig() {
        return config;
    }
    
    /**
     * Queued message for processing
     */
    private static class QueuedMessage {
        final UUID playerUuid;
        final String playerName;
        final String message;
        final long timestamp;
        
        QueuedMessage(UUID playerUuid, String playerName, String message, long timestamp) {
            this.playerUuid = playerUuid;
            this.playerName = playerName;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Result from Claude moderation analysis
     */
    private static class ModerationResult {
        final boolean isViolation;
        final double confidence;
        final String category;
        final String severity;
        final String explanation;
        
        ModerationResult(boolean isViolation, double confidence, String category, String severity, String explanation) {
            this.isViolation = isViolation;
            this.confidence = confidence;
            this.category = category;
            this.severity = severity;
            this.explanation = explanation;
        }
    }
}

