# Skyblock Coin Tracker

A lightweight Fabric mod that tracks your coin gains in Hypixel Skyblock. See exactly how much you're earning and your coins-per-hour rate in a clean, customizable overlay.

## Features

- **Real-time coin tracking** - Monitors your Purse and Piggy Bank from the scoreboard
- **Coins/hour calculation** - Know exactly how efficient your farming method is
- **Draggable overlay** - Position it anywhere on screen with `/ct move`
- **Auto-pause on inactivity** - Timer automatically pauses when you're AFK (configurable timeout)
- **Pause/resume tracking** - Take breaks without resetting your session
- **Persistent configuration** - Settings, position, and session progress saved across game restarts

## Commands

All commands use `/ct` (or `/cointracker`):

| Command | Description |
|---------|-------------|
| `/ct` | Show command help |
| `/ct on` | Enable the tracker and start a new session |
| `/ct off` | Disable the tracker and reset |
| `/ct toggle` | Toggle tracking on/off |
| `/ct pause` | Pause the timer (keeps gains frozen) |
| `/ct unpause` | Resume tracking |
| `/ct togglepause` | Toggle pause state |
| `/ct reset` | Reset gains and timer without disabling |
| `/ct move` | Open the drag-and-drop overlay editor |
| `/ct pos <x> <y>` | Set overlay position precisely |
| `/ct scale <0.1-5.0>` | Adjust overlay size |
| `/ct timeout <minutes>` | Set inactivity timeout (1-60 min, default: 5) |
| `/ct settime <minutes>` | Manually set elapsed time |
| `/ct setgain <amount>` | Manually set coin gain amount |

## Keybinds

Keybinds can be configured in **Options > Controls > Miscellaneous**:

| Keybind | Action | Default |
|---------|--------|---------|
| Toggle Tracker | Start/stop tracking (same as `/ct toggle`) | Unbound |
| Toggle Pause | Pause/unpause tracking (same as `/ct togglepause`) | Unbound |

## Overlay Display

The overlay shows three lines:
- **Gained:** Total coins earned this session
- **Time:** Session duration (shows "Inactive" or "Paused" when applicable)
- **Rate:** Your coins per hour

## Usage Tips

1. Run `/ct on` when you start farming
2. Use `/ct move` to drag the overlay to your preferred location
3. The tracker auto-pauses after 5 minutes of no purse changes (adjustable with `/ct timeout`)
4. Use `/ct pause` when taking a short break to freeze your stats
5. Use `/ct reset` to start fresh without repositioning the overlay

## Requirements

- Minecraft 1.21.11+
- Fabric Loader 0.19.2+
- Fabric API
