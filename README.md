# Skyblock Coin Tracker

A lightweight Fabric mod for Minecraft 1.21.11+ that tracks your coin earnings in Hypixel Skyblock. Monitor your purse changes in real-time with a clean, customizable HUD overlay.

## Features

- **Real-time coin tracking** - Automatically monitors Purse and Piggy Bank values from the scoreboard
- **Coins/hour calculation** - See exactly how efficient your farming method is
- **Draggable overlay** - Position anywhere on screen via `/ct move`
- **Auto-pause on inactivity** - Timer automatically pauses when no purse changes are detected (configurable timeout)
- **Session persistence** - Settings, position, and progress saved across game restarts
- **Keybind support** - Configure hotkeys for quick toggle and pause

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) (0.19.2+) for Minecraft 1.21.11
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download the latest release from [Modrinth](https://modrinth.com/mod/skyblockcointracker) or [GitHub Releases](https://github.com/jacktym/SkyblockCoinTracker/releases)
4. Place the `.jar` file in your `mods` folder

## Commands

All commands use `/ct` (or `/cointracker`):

| Command | Description |
|---------|-------------|
| `/ct` | Show command help |
| `/ct on` | Enable tracker and start a new session |
| `/ct off` | Disable tracker and reset |
| `/ct toggle` | Toggle tracking on/off |
| `/ct pause` | Pause the timer (freezes stats) |
| `/ct unpause` | Resume tracking |
| `/ct togglepause` | Toggle pause state |
| `/ct reset` | Reset gains and timer without disabling |
| `/ct move` | Open drag-and-drop overlay editor |
| `/ct pos <x> <y>` | Set overlay position precisely |
| `/ct scale <0.1-5.0>` | Adjust overlay size |
| `/ct timeout <minutes>` | Set inactivity timeout (1-60 min, default: 5) |
| `/ct settime <minutes>` | Manually set elapsed time |
| `/ct setgain <amount>` | Manually set coin gain amount |

## Keybinds

Configure in **Options > Controls > Key Binds**:

| Action | Description | Default |
|--------|-------------|---------|
| Toggle Tracker | Start/stop tracking | Unbound |
| Toggle Pause | Pause/unpause tracking | Unbound |

## Overlay Display

The HUD overlay shows three lines:
- **Gained:** Total coins earned this session
- **Time:** Session duration (shows "Inactive" or "Paused" when applicable)
- **Rate:** Your coins per hour

## Usage

1. Join Hypixel Skyblock
2. Run `/ct on` when you start farming
3. Use `/ct move` to drag the overlay to your preferred location
4. The tracker auto-pauses after 5 minutes of no purse changes (adjust with `/ct timeout`)
5. Use `/ct pause` when taking breaks to freeze your stats
6. Use `/ct reset` to start fresh without repositioning

## Tech Stack

- **Minecraft:** 1.21.11
- **Mod Loader:** Fabric (Fabric Loader 0.19.2+)
- **Language:** Java 21
- **Build System:** Gradle with Fabric Loom
- **Mappings:** Official Mojang mappings

### Project Structure

```
src/
├── main/java/                    # Server-side (empty - client-only mod)
└── client/java/                  # Client-side code
    └── dev/jacktym/skyblockcointracker/client/
        ├── SkyblockcointrackerClient.java  # Mod entrypoint
        ├── CoinTracker.java                # Core tracking logic
        ├── CoinTrackerState.java           # Session state management
        ├── CoinTrackerConfig.java          # Persistent configuration
        ├── CoinTrackerKeybinds.java        # Keybind registration
        ├── ChatListener.java               # Chat event handling
        ├── commands/
        │   └── CoinTrackerCommand.java     # Command registration
        ├── overlay/
        │   ├── CoinOverlay.java            # HUD rendering
        │   └── OverlayEditScreen.java      # Drag-to-position screen
        ├── util/
        │   └── FormatUtil.java             # Number/time formatting
        └── mixin/client/
            └── ScoreboardMixin.java        # Scoreboard data interception
```

### How It Works

1. **Scoreboard Mixin** intercepts scoreboard updates from Hypixel
2. **CoinTracker** parses "Purse:" and "Piggy:" lines to extract coin values
3. **CoinTrackerState** tracks the difference from the starting purse and elapsed time
4. **CoinOverlay** renders the HUD using Fabric's `HudRenderCallback`
5. **CoinTrackerConfig** persists settings to `config/cointracker.json`

## Building from Source

```bash
# Clone the repository
git clone https://github.com/jacktym/SkyblockCoinTracker.git
cd SkyblockCoinTracker

# Build the mod
./gradlew build

# Output JAR will be in build/libs/
```

## Requirements

- Minecraft 1.21.11+
- Fabric Loader 0.19.2+
- Fabric API

## License

All Rights Reserved

---

*This mod is not affiliated with Hypixel Inc. or Mojang Studios.*
