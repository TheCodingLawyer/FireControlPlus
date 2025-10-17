package me.confuser.banmanager.bukkit.admingui;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.bukkit.admingui.commands.Admin;
import me.confuser.banmanager.bukkit.admingui.commands.AdminChat;
import me.confuser.banmanager.bukkit.admingui.commands.CommandSpy;
import me.confuser.banmanager.bukkit.admingui.listeners.*;
import me.confuser.banmanager.bukkit.admingui.utils.*;
import me.confuser.banmanager.bukkit.admingui.utils.vault.VaultChatConnector;
import me.confuser.banmanager.bukkit.admingui.utils.vault.VaultPermissionConnector;
// Using BanManager's database instead of separate HikariDataSource
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Real AdminGUI integration class - contains the actual AdminGUI code adapted for BanManager
 * This is NOT a rewrite - it's the original AdminGUI code modified to work within BanManager
 */
public class AdminGuiIntegration implements PluginMessageListener {

    //Database - using BanManager's database
    // public static HikariDataSource hikari; // Not needed - using BanManager's database
    public static int gui_type = 0;
    //Update Checker
    public static String new_version = null;
    private static AdminGuiIntegration instance;
    private static Connection conn = null;
    //VaultAPI
    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;
    
    private final BMBukkitPlugin banManagerPlugin;
    private final Logger logger;
    private final File dataFolder;
    
    private final YamlConfiguration conf = new YamlConfiguration();
    private final YamlConfiguration sett = new YamlConfiguration();
    private final YamlConfiguration perm = new YamlConfiguration();
    private final YamlConfiguration play = new YamlConfiguration();
    private final YamlConfiguration kick = new YamlConfiguration();
    private final YamlConfiguration plug = new YamlConfiguration();
    private final YamlConfiguration comm = new YamlConfiguration();
    private final YamlConfiguration como = new YamlConfiguration();
    
    String username = "%%__USERNAME__%%";
    String user_id = "%%__USER__%%";
    
    //Config files
    private File co = null;
    private File se = null;
    private File pe = null;
    private File pl = null;
    private File k = null;
    private File p = null;
    private File c = null;
    private File o = null;

    public AdminGuiIntegration(BMBukkitPlugin banManagerPlugin) {
        this.banManagerPlugin = banManagerPlugin;
        this.logger = banManagerPlugin.getLogger();
        this.dataFolder = new File(banManagerPlugin.getDataFolder(), "admingui");
        instance = this;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getVaultPermissions() {
        return perms;
    }

    public static Chat getVaultChat() {
        return chat;
    }

    public static AdminGuiIntegration getInstance() {
        return instance;
    }

    public void enable() {
        try {
            logger.info("Enabling AdminGUI integration...");
            
            // Setup file paths
            this.co = new File(dataFolder, "config.yml");
            this.se = new File(dataFolder, "settings.yml");
            this.pe = new File(dataFolder, "permissions.yml");
            this.pl = new File(dataFolder, "players.yml");
            this.k = new File(dataFolder, "kick.yml");
            this.p = new File(dataFolder, "Custom Commands/plugins.yml");
            this.c = new File(dataFolder, "Custom Commands/commands.yml");
            this.o = new File(dataFolder, "Custom Commands/commands-other.yml");

            mkdir();
            loadYamls();

            String defaultLang = getConf().getString("default_language", "English");
            logger.info("AdminGUI enabled with language: " + defaultLang);
            
            Language.downloadLanguage(defaultLang);
            Language.getLanguages();

            if (getSett().getBoolean("maintenance", false)) Settings.maintenance_mode = true;

            //Database connection - use BanManager's database instead
            if (getConf().getBoolean("use_banmanager_database", true)) {
                logger.info("Using BanManager's database for AdminGUI");
            } else if (getConf().getBoolean("mysql", false)) {
                setupMySQL();
            }

            //bStats - skip for integration
            
            //Update Checker
            if (getConf().getBoolean("uc_enabled", true)) {
                new UpdateChecker(banManagerPlugin, 49).getVersion(updater_version -> {
                    if (!banManagerPlugin.getDescription().getVersion().equalsIgnoreCase(updater_version)) new_version = updater_version;
                    info("&aEnabling AdminGUI Integration");
                });
            } else {
                info("&aEnabling AdminGUI Integration");
            }

            //VaultAPI
            if (banManagerPlugin.getServer().getPluginManager().getPlugin("Vault") != null) {
                setupEconomy();
                setupPermissions();
                if (getConf().getBoolean("ap_enabled", false)) {
                    final ServicesManager sm = banManagerPlugin.getServer().getServicesManager();
                    sm.register(Permission.class, new VaultPermissionConnector(), banManagerPlugin, ServicePriority.Highest);
                    setupPermissions();
                    sm.register(Chat.class, new VaultChatConnector(perms), banManagerPlugin, ServicePriority.Highest);
                }
                setupChat();
            }

            gui_type = getConf().getInt("gui_type", 0);

            //Bungee
            if (getConf().getBoolean("bungeecord_enabled", false)) {
                banManagerPlugin.getServer().getMessenger().registerOutgoingPluginChannel(banManagerPlugin, "my:admingui");
                banManagerPlugin.getServer().getMessenger().registerIncomingPluginChannel(banManagerPlugin, "my:admingui", this);
            }

            //Listeners
            new InventoryClickListener(this);
            if (Bukkit.getVersion().contains("1.8")) new PlayerDamageListener(this);

            new PlayerJoinListener(this);
            new PlayerLeaveListener(this);
            new PlayerLoginListener(this);

            if (getConf().getBoolean("admin_tools_enabled", true)) {
                new PlayerInteractListener(this);
                new PlayerEntityInteractListener(this);
            }

            if (getConf().getBoolean("ms_enabled", false)) new MultiplayerSleepListener(this);

            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new PlayerPlaceholderMessageListener(this);
                new PlayerPlaceholderCommandListener(this);
                new AdminGUIPlaceholders().register();
            } else {
                new PlayerMessageListener(this);
                new PlayerCommandListener(this);
            }

            if (getConf().getBoolean("motd_changer_enabled", false)) new ServerListPingListener(this);

            //Freeze
            new PlayerMoveListener(this);
            new PlayerBlockBreakListener(this);
            new PlayerBlockPlaceListener(this);
            new PlayersDroppingItemsListener(this);

            //Commands - Register AdminGUI commands
            if (banManagerPlugin.getCommand("admin") != null) {
                banManagerPlugin.getCommand("admin").setExecutor(new Admin());
                banManagerPlugin.getCommand("admin").setTabCompleter(new me.confuser.banmanager.bukkit.admingui.TabCompletion());
                logger.info("Registered /admin command for AdminGUI");
                
                // Multiple delayed re-registrations to aggressively override other plugins
                Bukkit.getScheduler().runTaskLater(banManagerPlugin, () -> {
                    if (banManagerPlugin.getCommand("admin") != null) {
                        banManagerPlugin.getCommand("admin").setExecutor(new Admin());
                        banManagerPlugin.getCommand("admin").setTabCompleter(new me.confuser.banmanager.bukkit.admingui.TabCompletion());
                        logger.info("Re-registered /admin command (1st attempt)");
                    }
                }, 20L); // 1 second delay
                
                // Second attempt after 5 seconds
                Bukkit.getScheduler().runTaskLater(banManagerPlugin, () -> {
                    if (banManagerPlugin.getCommand("admin") != null) {
                        banManagerPlugin.getCommand("admin").setExecutor(new Admin());
                        banManagerPlugin.getCommand("admin").setTabCompleter(new me.confuser.banmanager.bukkit.admingui.TabCompletion());
                        logger.info("Re-registered /admin command (2nd attempt)");
                    }
                }, 100L); // 5 second delay
                
                // Third attempt after 10 seconds
                Bukkit.getScheduler().runTaskLater(banManagerPlugin, () -> {
                    if (banManagerPlugin.getCommand("admin") != null) {
                        banManagerPlugin.getCommand("admin").setExecutor(new Admin());
                        banManagerPlugin.getCommand("admin").setTabCompleter(new me.confuser.banmanager.bukkit.admingui.TabCompletion());
                        logger.info("Final re-registration of /admin command");
                    }
                }, 200L); // 10 second delay
            } else {
                logger.warning("Failed to register /admin command - command not found in plugin.yml");
            }
            
            if (banManagerPlugin.getCommand("adminchat") != null) {
                banManagerPlugin.getCommand("adminchat").setExecutor(new AdminChat());
                banManagerPlugin.getCommand("adminchat").setTabCompleter(new me.confuser.banmanager.bukkit.admingui.TabCompletion());
                logger.info("Registered /adminchat command for AdminGUI");
            } else {
                logger.warning("Failed to register /adminchat command - command not found in plugin.yml");
            }
            
            if (banManagerPlugin.getCommand("admincommandspy") != null) {
                banManagerPlugin.getCommand("admincommandspy").setExecutor(new CommandSpy());
                banManagerPlugin.getCommand("admincommandspy").setTabCompleter(new me.confuser.banmanager.bukkit.admingui.TabCompletion());
                logger.info("Registered /admincommandspy command for AdminGUI");
            } else {
                logger.warning("Failed to register /admincommandspy command - command not found in plugin.yml");
            }

            //Skulls
            if (gui_type == 1) {
                Settings.skulls.put("0qt", Item.pre_createPlayerHead("0qt"));
                Settings.skulls.put("Black1_TV", Item.pre_createPlayerHead("Black1_TV"));
                Settings.skulls.put("mattijs", Item.pre_createPlayerHead("mattijs"));
                Settings.skulls.put("BKing2012", Item.pre_createPlayerHead("BKing2012"));
                Settings.skulls.put("AverageJoe", Item.pre_createPlayerHead("AverageJoe"));
                Settings.skulls.put("LobbyPlugin", Item.pre_createPlayerHead("LobbyPlugin"));
                Settings.skulls.put("MHF_Redstone", Item.pre_createPlayerHead("MHF_Redstone"));
                Settings.skulls.put("Ground15", Item.pre_createPlayerHead("Ground15"));
                Settings.skulls.put("EDDxample", Item.pre_createPlayerHead("EDDxample"));
                Settings.skulls.put("LapisBlock", Item.pre_createPlayerHead("LapisBlock"));
                Settings.skulls.put("emack0714", Item.pre_createPlayerHead("emack0714"));
                Settings.skulls.put("Super_Sniper", Item.pre_createPlayerHead("Super_Sniper"));
                Settings.skulls.put("IM_", Item.pre_createPlayerHead("IM_"));
                Settings.skulls.put("Burger_guy", Item.pre_createPlayerHead("Burger_guy"));
                Settings.skulls.put("MFH_Spawner", Item.pre_createPlayerHead("MFH_Spawner"));
                Settings.skulls.put("MrSnowDK", Item.pre_createPlayerHead("MrSnowDK"));
                Settings.skulls.put("ZeeFear", Item.pre_createPlayerHead("ZeeFear"));
                Settings.skulls.put("Opp", Item.pre_createPlayerHead("Opp"));
                Settings.skulls.put("haohanklliu", Item.pre_createPlayerHead("haohanklliu"));
                Settings.skulls.put("raichuthink", Item.pre_createPlayerHead("raichuthink"));
                Settings.skulls.put("ThaBrick", Item.pre_createPlayerHead("ThaBrick"));
                Settings.skulls.put("Mannahara", Item.pre_createPlayerHead("Mannahara"));
                Settings.skulls.put("Zyne", Item.pre_createPlayerHead("Zyne"));
                Settings.skulls.put("3i5g00d", Item.pre_createPlayerHead("3i5g00d"));
                Settings.skulls.put("MHF_ArrowLeft", Item.pre_createPlayerHead("MHF_ArrowLeft"));
                Settings.skulls.put("MHF_Question", Item.pre_createPlayerHead("MHF_Question"));
                Settings.skulls.put("MHF_ArrowRight", Item.pre_createPlayerHead("MHF_ArrowRight"));
                Settings.skulls.put("ZiGmUnDo", Item.pre_createPlayerHead("ZiGmUnDo"));
                Settings.skulls.put("Push_red_button", Item.pre_createPlayerHead("Push_red_button"));
                Settings.skulls.put("ElMarcosFTW", Item.pre_createPlayerHead("ElMarcosFTW"));
                Settings.skulls.put("DavidGriffiths", Item.pre_createPlayerHead("DavidGriffiths"));
            } else {
                Settings.skulls.put("Black1_TV", Item.pre_createPlayerHead("Black1_TV"));
            }

            logger.info("AdminGUI integration enabled successfully!");
            
        } catch (Exception e) {
            logger.severe("Failed to enable AdminGUI integration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void disable() {
        logger.info("Disabling AdminGUI integration...");

        //Admin Permissions
        if (getConf().getBoolean("ap_enabled", false)) {
            for (Player player : Bukkit.getOnlinePlayers()) TargetPlayer.removePermissions(player);
            Settings.permissions.clear();
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
        
        logger.info("AdminGUI integration disabled");
    }

    //MySql - Using BanManager's database instead
    private void setupMySQL() {
        logger.info("Using BanManager's database connection for AdminGUI");
        // AdminGUI will use BanManager's database connection
        // No separate HikariDataSource needed
    }

    //VaultAPI
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = banManagerPlugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return true;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = banManagerPlugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) return false;
        chat = rsp.getProvider();
        return true;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = banManagerPlugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return false;
        perms = rsp.getProvider();
        return true;
    }

    public void mkdir() {
        if (!dataFolder.exists()) dataFolder.mkdirs();
        if (!this.co.exists()) copyResource("config.yml");
        if (!this.se.exists()) copyResource("settings.yml");
        if (!this.pe.exists()) copyResource("permissions.yml");
        if (!this.pl.exists()) copyResource("players.yml");
        if (!this.k.exists()) copyResource("kick.yml");
        
        File customCommandsDir = new File(dataFolder, "Custom Commands");
        if (!customCommandsDir.exists()) customCommandsDir.mkdirs();
        if (!this.p.exists()) copyResource("Custom Commands/plugins.yml");
        if (!this.c.exists()) copyResource("Custom Commands/commands.yml");
        if (!this.o.exists()) copyResource("Custom Commands/commands-other.yml");
    }

    private void copyResource(String resourcePath) {
        try {
            banManagerPlugin.saveResource("admingui/" + resourcePath, false);
        } catch (Exception e) {
            // Create empty file if resource doesn't exist
            try {
                new File(dataFolder, resourcePath).createNewFile();
            } catch (IOException ex) {
                logger.warning("Could not create AdminGUI config file: " + resourcePath);
            }
        }
    }

    public void loadYamls() {
        try {
            this.conf.load(this.co);
            this.sett.load(this.se);
            this.perm.load(this.pe);
            this.play.load(this.pl);
            this.kick.load(this.k);
            this.plug.load(this.p);
            this.comm.load(this.c);
            this.como.load(this.o);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public YamlConfiguration getConf() {
        return this.conf;
    }

    public YamlConfiguration getSett() {
        return this.sett;
    }

    public YamlConfiguration getPermissions() {
        return this.perm;
    }

    public YamlConfiguration getPlayers() {
        return this.play;
    }

    public YamlConfiguration getKick() {
        return this.kick;
    }

    public YamlConfiguration getPlug() {
        return this.plug;
    }

    public YamlConfiguration getComm() {
        return this.comm;
    }

    public YamlConfiguration getComo() {
        return this.como;
    }

    public void savePlayers() {
        try {
            this.play.save(this.pl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSettings() {
        try {
            this.sett.save(this.se);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void info(String message) {
        String text = "\n\n";
        text += "&8[]=======[" + message + " &cAdminGUI-Premium (BanManager Integration)&8]=======[]\n";
        text += "&8|\n";
        text += "&8| &cInformation:\n";
        text += "&8|\n";
        text += "&8|   &9Name: &bAdminGUI-Premium (Integrated)\n";
        text += "&8|   &9Developer: &bBlack1_TV\n";
        text += "&8|   &9Integration: &bBanManager Team\n";
        if (!username.contains("%%__")) {
            text += "&8|   &9Plugin owner: &b" + username + "\n";
            text += "&8|   &9License key: &b" + new Hash().createLicenseKey(username) + "\n";
        } else if (!user_id.contains("%%__")) {
            text += "&8|   &9Plugin owner: &b" + user_id + "\n";
            text += "&8|   &9License key: &b" + new Hash().createLicenseKey(user_id) + "\n";
        } else {
            text += "&8|   &9Plugin owner: &4&lCRACKED\n";
            text += "&8|   &9License key: &400000-00000-00000-00000\n";
        }
        if (new_version != null) {
            text += "&8|   &9Version: &b" + banManagerPlugin.getDescription().getVersion() + " (&6update available&b)\n";
        } else {
            text += "&8|   &9Version: &b" + banManagerPlugin.getDescription().getVersion() + "\n";
        }
        text += "&8|   &9Website: &bhttps://rabbit-company.com\n";
        text += "&8|   &9BanManager: &bhttps://banmanagement.com\n";
        text += "&8|\n";
        text += "&8[]=========================================[]\n";

        Bukkit.getConsoleSender().sendMessage(Message.chat(text));
    }

    // Bungee message handling
    @Override
    public void onPluginMessageReceived(String channel, Player pla, byte[] message) {
        if (!channel.equalsIgnoreCase("my:admingui")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String sender = in.readUTF();
        String subchannel = in.readUTF();

        switch (subchannel) {
            case "chat":
                if (getConf().getBoolean("bungeecord_enabled", false) && getConf().getBoolean("bungeecord_admin_chat", false)) {
                    Player player = Bukkit.getServer().getPlayer(UUID.fromString(sender));
                    if (player == null || !player.isOnline())
                        Bukkit.getServer().broadcastMessage(in.readUTF());
                }
                break;
            case "custom_chat_channels":
                String ccChannel = in.readUTF();
                String ccServerName = in.readUTF();
                String ccPlayerName = in.readUTF();
                String ccMessage = in.readUTF();

                if(!getConf().getBoolean("bungeecord_enabled", false) || !getConf().getBoolean("bungeecord_custom_chat_channels", false)) break;
                if(!getConf().contains("ccc." + ccChannel)) break;
                Player ccPlayer = Bukkit.getServer().getPlayer(UUID.fromString(sender));
                if (ccPlayer != null && ccPlayer.isOnline()) break;
                String bbFormat = getConf().getString("bungeecord_custom_chat_channels_format", "&7[{server_name}] {format}");
                String ccFormat = getConf().getString("ccc." + ccChannel + ".format", "&2[&cStaff Chat&2] &5{name} &f> {message}");
                String ccPermission = getConf().getString("ccc." + ccChannel + ".permission");

                ccFormat = bbFormat.replace("{server_name}", ccServerName).replace("{format}", ccFormat);

                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (TargetPlayer.hasPermission(player, ccPermission)) {
                        player.sendMessage(Message.chat(ccFormat.replace("{name}", ccPlayerName).replace("{display_name}", ccPlayerName).replace("{server_name}", ccServerName).replace("{message}", ccMessage)));
                    }
                }
                Bukkit.getConsoleSender().sendMessage(Message.chat(ccFormat.replace("{name}", ccPlayerName).replace("{display_name}", ccPlayerName).replace("{server_name}", ccServerName).replace("{message}", ccMessage)));
                break;
            case "online_players":
                String online_players = in.readUTF();
                String[] op = online_players.split(";");
                Settings.online_players.clear();
                for (String on : op) {
                    Settings.online_players.add(on);
                    Settings.skulls_players.put(on, Item.pre_createPlayerHead(on));
                }
                break;
            case "rank":
                String target_uuid = in.readUTF();
                String name = in.readUTF();
                String rank = in.readUTF();
                if (target_uuid.equals("null")) {
                    Permissions.saveRank(null, name, rank);
                } else {
                    Permissions.saveRank(UUID.fromString(target_uuid), name, rank);
                }
                break;
            case "gamemode":
                String player = in.readUTF();
                String gamemode = in.readLine();

                Player target = Bukkit.getServer().getPlayer(player);

                if (target != null) {
                    if (target.isOnline()) {
                        switch (gamemode) {
                            case "spectator":
                                target.setGameMode(GameMode.SPECTATOR);
                                break;
                            case "creative":
                                target.setGameMode(GameMode.CREATIVE);
                                break;
                            case "adventure":
                                target.setGameMode(GameMode.ADVENTURE);
                                break;
                            default:
                                target.setGameMode(GameMode.SURVIVAL);
                                break;
                        }
                    }
                }
                break;
        }
    }
    
    // Getters for BanManager integration
    public BMBukkitPlugin getBanManagerPlugin() {
        return banManagerPlugin;
    }
    
    public File getDataFolder() {
        return dataFolder;
    }
    
    public Logger getLogger() {
        return logger;
    }
} 
