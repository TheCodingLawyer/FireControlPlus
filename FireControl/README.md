# 🔥 FireControl

> A lightweight, high-performance Spigot plugin to control fire spread on your Minecraft server.

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spigot](https://img.shields.io/badge/Spigot-1.18%2B-orange.svg)](https://www.spigotmc.org/)
[![Java](https://img.shields.io/badge/Java-11%2B-red.svg)](https://www.java.com/)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](#)

---

## ✨ Features

- **🎛️ Fire Spread Control** - Completely disable or enable fire spread with a single command
- **⚡ Lightweight** - Minimal resource usage with optimized event handling
- **🔧 Easy Configuration** - Simple YAML config for quick setup
- **🛡️ Permission System** - Built-in permission nodes for admin control
- **📊 Logging** - Optional event logging for audit trails
- **🔄 Hot Reload** - Reload configuration without restarting
- **📱 Aliases** - Multiple command aliases for convenience
- **🎮 Player Feedback** - Customizable messages for blocked fire spread

---

## 📦 Installation

### Quick Start

1. Download the latest `FireControl-*.jar` from [Releases](https://github.com/TheCodingLawyer/FireControlPlus/releases)
2. Place it in your server's `plugins/` folder
3. Restart your server or use `/reload confirm`
4. *(Optional)* Configure in `plugins/FireControl/config.yml`

### Requirements

- **Minecraft**: 1.18 - 1.21+ (all versions tested and supported)
- **Server Type**: Spigot, Paper, Purpur, and forks
- **Java**: 11 or higher

---

## 🎮 Commands

| Command | Description | Permission | Aliases |
|---------|-------------|-----------|---------|
| `/firecontrol` | Show plugin info and usage | `firecontrol.admin` | `/fc`, `/firespread` |
| `/firecontrol enable` | Enable fire spread | `firecontrol.admin` | — |
| `/firecontrol disable` | Disable fire spread | `firecontrol.admin` | — |
| `/firecontrol status` | Check current fire spread status | `firecontrol.admin` | — |
| `/firecontrol reload` | Reload configuration | `firecontrol.admin` | — |

### Usage Examples

```bash
# Check current status
/fc status

# Disable fire spread
/fc disable

# Enable fire spread
/fc enable

# Reload config (hot reload)
/firecontrol reload
```

---

## ⚙️ Configuration

### Default Config (`plugins/FireControl/config.yml`)

```yaml
# Fire Spread Control Settings
fire-spread:
  # Set to true to disable fire spread, false to allow normal fire spread
  disabled: true
  
  # Show messages when fire spread is blocked
  show-messages: true
  
  # Customizable message when fire spread is blocked
  # Use & for color codes (e.g., &c for red, &a for green)
  block-message: "&cFire spread is disabled on this server"

# Logging Configuration
logging:
  # Log fire spread blocks to console (useful for debugging)
  log-blocks: false
```

### Color Codes

Use these codes in your messages:
- `&c` - Red
- `&a` - Green
- `&e` - Yellow
- `&b` - Aqua
- `&d` - Light Purple
- `&f` - White
- `&0-9` - Other colors
- `&l` - Bold
- `&o` - Italic

---

## 🔐 Permissions

### Permission Nodes

| Permission | Description | Default |
|-----------|-------------|---------|
| `firecontrol.admin` | Access to all FireControl commands | `op` |

To grant permission to a player:
```bash
/op playername
```

Or use a permission plugin like LuckPerms:
```bash
/lp user playername permission set firecontrol.admin true
```

---

## 🔨 Building from Source

### Prerequisites

- Git
- Gradle (comes with gradlew)
- Java 11+

### Build Instructions

```bash
# Clone the repository
git clone https://github.com/TheCodingLawyer/FireControlPlus.git
cd FireControlPlus

# Build the plugin
./gradlew build

# Output JAR will be in: build/libs/FireControl-*.jar
```

---

## 📋 How It Works

FireControl intercepts the `BlockSpreadEvent` from Bukkit and cancels fire spread events when disabled. This approach:

✅ Has zero impact on server performance  
✅ Works seamlessly across all Spigot-compatible servers  
✅ Can be toggled on/off without restart  
✅ Preserves all other fire behavior (fire placement, damage, etc.)

---

## 🐛 Troubleshooting

### Fire spread isn't being disabled

1. Check `/firecontrol status` to confirm it's disabled
2. Verify your config file at `plugins/FireControl/config.yml`
3. Ensure `fire-spread.disabled: true` is set
4. Reload with `/firecontrol reload`
5. Check console for any error messages

### Plugin won't load

1. Ensure you're using **Java 11 or higher**
2. Check that your server version is **1.18 or higher**
3. Look for errors in the console on startup
4. Try deleting the config and letting it regenerate

### Configuration not applying

1. Use `/firecontrol reload` to hot-reload
2. Or restart the server: `/stop`

---

## 📊 Version Compatibility

| Version | Status | Notes |
|---------|--------|-------|
| 1.18.x - 1.20.x | ✅ Fully Supported | Recommended for stability |
| 1.21+ | ✅ Fully Supported | Latest versions |
| Below 1.18 | ❌ Not Supported | Requires Java 8 build |

---

## 🤝 Contributing

Found a bug? Have a feature request? Feel free to:
1. Open an [Issue](https://github.com/TheCodingLawyer/FireControlPlus/issues)
2. Submit a [Pull Request](https://github.com/TheCodingLawyer/FireControlPlus/pulls)
3. Join our Discord community

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

Developed by the **BanManager Integration Team**

---

## 🙏 Support

If you found this plugin useful, please:
- ⭐ Star this repository
- 🔔 Watch for updates
- 📢 Share with other server admins
- 💬 Provide feedback and suggestions

---

## 📞 Links

- 🌐 [GitHub Repository](https://github.com/TheCodingLawyer/FireControlPlus)
- 🎮 [Spigot Page](https://www.spigotmc.org/)
- 💬 [Discussions](https://github.com/TheCodingLawyer/FireControlPlus/discussions)
- 📧 **Questions?** Open an issue on GitHub

---

<div align="center">

**Made with ❤️ for the Minecraft community**

</div> 