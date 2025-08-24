# AdminGUI Premium - Enhanced BanManager Integration

This is AdminGUI Premium with **enhanced BanManager integration** that uses direct API calls instead of command dispatching for better performance and reliability.

## ✅ **BUILD STATUS: SUCCESSFUL**
- **0 compilation errors** 
- **100 deprecation warnings** (safe to ignore - compatibility warnings only)
- **Output**: `admingui/build/libs/AdminGUI-Premium-5.15.0-Enhanced.jar` (522 KB)

## 🚀 What's Enhanced?

### Before (Command-based Integration):
- Used `Bukkit.dispatchCommand()` to execute BanManager commands
- Slower performance due to command parsing overhead
- Less reliable error handling
- Limited access to punishment data

### After (Direct API Integration):
- **Direct BanManager API calls** for all punishment operations
- **Faster performance** - no command parsing overhead
- **Better error handling** with automatic fallback to commands
- **Full access** to punishment data and player information
- **Automatic player data management** - creates player records as needed
- **Real-time punishment status checking**

## 📋 Features

### Core AdminGUI Features:
- **Inventory-based GUI** for server administration
- **Player Management**: Ban, mute, kick, warn players
- **Server Administration**: Time, weather, maintenance mode
- **Player Utilities**: Heal, feed, teleport, gamemode changes
- **Economy Integration**: Vault support for money management
- **Permission Management**: Group and permission handling
- **Chat Management**: Moderation and filtering
- **Multi-platform**: Bukkit and BungeeCord support

### Enhanced BanManager Integration:
- **Direct API Punishment Actions**: Ban, mute, kick, warn with direct API calls
- **Punishment Status Checking**: Real-time ban/mute status verification
- **Player Data Retrieval**: Access to full punishment history
- **Automatic Fallback**: Falls back to commands if API fails
- **Silent Punishment Support**: All punishment types support silent mode
- **Temporary Punishment Support**: Time-based bans, mutes, and warns

## 🔧 Requirements

- **Java 8+**
- **Paper/Spigot 1.18+** (recommended: Paper 1.21+)
- **BanManager 7.8.0+** (recommended: 7.10.0+)
- **Maven** (for building from source)

### Optional Dependencies:
- **Vault** - For economy and permission features
- **PlaceholderAPI** - For placeholder support
- **SuperVanish/PremiumVanish** - For vanish integration
- **LiteBans** - Alternative punishment system support
- **DiscordSRV** - Discord integration

## 🏗️ Building

### Windows:
```cmd
cd admingui
build.bat
```

### Linux/Mac:
```bash
cd admingui
chmod +x build.sh
./build.sh
```

### Manual Build:
```bash
cd admingui
mvn clean package
```

The compiled jar will be in `target/AdminGUI-Premium.jar`

## 📦 Installation

1. **Install BanManager** on your server first
2. **Copy `AdminGUI-Premium.jar`** to your `plugins` folder
3. **Restart your server**
4. **Configure** the plugin in `plugins/AdminGUI-Premium/`

## ⚙️ Configuration

The plugin will automatically detect BanManager and use the enhanced integration. No additional configuration is required for the BanManager integration.

### Key Configuration Files:
- `config.yml` - Main plugin configuration
- `messages.yml` - Customizable messages
- `permissions.yml` - Permission settings
- Custom GUI configurations in `Custom GUIs/` folder

## 🎯 Usage

### Opening the Main GUI:
```
/admin
```

### Admin Chat:
```
/adminchat <message>
/achat <message>
/ac <message>
```

### Command Spy:
```
/admincommandspy
/acommandspy
/admincs
/acs
```

## 🔌 BanManager Integration Details

### Supported Operations:
- ✅ **Ban Player** (temporary/permanent, silent/public)
- ✅ **Unban Player**
- ✅ **Mute Player** (temporary/permanent, silent/public)
- ✅ **Unmute Player**
- ✅ **Kick Player**
- ✅ **Warn Player** (silent/public)
- ✅ **Check Ban Status**
- ✅ **Check Mute Status**
- ✅ **Get Punishment Lists**
- ✅ **Get Player Punishment History**

### API Integration Features:
- **Automatic Player Data Creation** - Creates BanManager player records automatically
- **Name Synchronization** - Updates player names in BanManager database
- **Async Operations** - All punishment operations are asynchronous
- **Error Recovery** - Automatic fallback to command-based operations
- **UUID Support** - Full UUID-based player identification

## 🐛 Troubleshooting

### Common Issues:

**"BanManager not found" errors:**
- Ensure BanManager is installed and enabled
- Check that BanManager loads before AdminGUI

**Permission errors:**
- Verify the admin has proper AdminGUI permissions
- Check BanManager permission configuration

**Database errors:**
- Ensure BanManager database is properly configured
- Check server logs for detailed error messages

**Build errors:**
- Ensure Maven is properly installed
- Check internet connection for dependency downloads
- Verify Java version compatibility

## 📝 Permissions

### Core Permissions:
- `admingui.admin` - Access to main GUI
- `admingui.ban` - Ban players
- `admingui.mute` - Mute players
- `admingui.kick` - Kick players
- `admingui.warn` - Warn players
- `admingui.unban` - Unban players
- `admingui.unmute` - Unmute players

### Bypass Permissions:
- `admingui.ban.bypass` - Immune to bans
- `admingui.mute.bypass` - Immune to mutes
- `admingui.kick.bypass` - Immune to kicks

See `plugin.yml` for the complete permission list.

## 🤝 Support

For support with:
- **AdminGUI features**: Contact the original AdminGUI developers
- **BanManager integration**: Check both AdminGUI and BanManager documentation
- **Build issues**: Ensure Maven and Java are properly configured

## 📄 License

This enhanced version maintains the original AdminGUI Premium license. Please respect the original licensing terms.

---

**Enhanced by**: BanManager Integration Team  
**Original Plugin**: AdminGUI Premium by Bytengine  
**BanManager**: by confuser 