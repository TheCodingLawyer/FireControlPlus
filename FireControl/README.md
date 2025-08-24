# FireControl Plugin

A simple, lightweight Spigot plugin to control fire spread on your server.

## Features

- **🔥 Fire Spread Control**: Completely disable or enable fire spread
- **⚙️ Configuration**: Easy config file setup
- **🎮 Commands**: In-game commands for instant control
- **📝 Logging**: Optional logging of blocked fire spread events
- **🚀 Lightweight**: Minimal resource usage, single event listener

## Installation

1. Download `FireControl-1.0.0.jar` from the releases
2. Place it in your server's `plugins/` folder
3. Restart your server
4. Configure in `plugins/FireControl/config.yml` if needed

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/firecontrol` | Show plugin info and usage | `firecontrol.admin` |
| `/firecontrol enable` | Enable fire spread | `firecontrol.admin` |
| `/firecontrol disable` | Disable fire spread | `firecontrol.admin` |
| `/firecontrol status` | Check current fire spread status | `firecontrol.admin` |
| `/firecontrol reload` | Reload configuration | `firecontrol.admin` |

**Aliases**: `/fc`, `/firespread`

## Configuration

```yaml
# Fire spread control settings
fire-spread:
  # Whether fire spread is disabled (true = no fire spread, false = normal fire spread)
  disabled: true
  
  # Whether to show messages when fire spread is blocked
  show-messages: true
  
  # Message shown when fire spread is blocked (set to empty string to disable)
  block-message: "&cFire spread is disabled on this server"

# Logging settings
logging:
  # Whether to log fire spread blocks to console
  log-blocks: false
```

## Permissions

- `firecontrol.admin` - Allows use of all FireControl commands (default: op)

## Building

```bash
./gradlew build
```

Output: `build/libs/FireControl-1.0.0.jar`

## Compatibility

- **Minecraft**: 1.21+ (works on latest versions)
- **Server**: Spigot, Paper, and forks
- **Java**: 21+ 