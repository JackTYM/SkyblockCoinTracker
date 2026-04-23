# Coin Tracker Mod Design

A Hypixel Skyblock Fabric mod that tracks coin profits/losses over time.

## Features

- Track coins from scoreboard "Purse:" line
- Movable/scalable HUD overlay showing Gained, Time, Rate
- Configurable timeout (auto-reset after no activity)
- Pause/unpause to exclude time and coin changes
- Reset on profile switch
- Commands and keybinds for control

## Project Structure

```
src/client/java/dev/jacktym/skyblockcointracker/client/
├── SkyblockcointrackerClient.java    # Entry point, registers everything
├── CoinTracker.java                  # Core tracking logic
├── CoinTrackerState.java             # Holds current state
├── CoinTrackerConfig.java            # Loads/saves JSON config
├── CoinTrackerKeybinds.java          # Registers keybinds
├── commands/
│   └── CoinTrackerCommand.java       # All /ct subcommands
├── mixin/
│   └── ScoreboardMixin.java          # Intercepts scoreboard updates
├── overlay/
│   ├── CoinOverlay.java              # Renders the HUD text
│   └── OverlayEditScreen.java        # Edit mode screen (drag + arrow keys)
└── util/
    └── FormatUtil.java               # Number/time formatting
```

## Commands

| Command | Description |
|---------|-------------|
| `/ct on` | Enable overlay |
| `/ct off` | Disable overlay |
| `/ct toggle` | Toggle on/off |
| `/ct pause` | Pause tracking |
| `/ct unpause` | Resume tracking |
| `/ct togglepause` | Toggle pause state |
| `/ct reset` | Reset tracker to zero |
| `/ct move` | Enter edit mode (drag + arrow keys) |
| `/ct pos <x> <y>` | Set overlay position |
| `/ct scale <value>` | Set overlay scale |
| `/ct timeout <minutes>` | Set inactive timeout |
| `/ct settime <minutes>` | Manually set elapsed time |
| `/ct setgain <amount>` | Manually set profit amount |

Alias: `/cointracker` works the same as `/ct`

## Keybinds

Registered in Minecraft's keybind system (Options > Controls > Coin Tracker):

- **Toggle Tracker** — calls toggle on/off (default: unbound)
- **Toggle Pause** — calls togglepause (default: unbound)

## State Management

### CoinTrackerState

```java
long startPurse;        // Purse value when tracking started
long currentPurse;      // Most recent purse value
long pausedPurse;       // Purse value when paused (for ignoring changes)
int elapsedSeconds;     // Time tracked (excludes paused time)
int secondsSinceChange; // For timeout detection
boolean paused;
boolean enabled;
```

### Calculations

- `gained = currentPurse - startPurse`
- `rate = gained / elapsedSeconds * 3600` (coins per hour)

### Pause Behavior

- On pause: store `pausedPurse = currentPurse`, stop time
- On unpause: adjust `startPurse += (currentPurse - pausedPurse)` to ignore changes during pause
- Result: both time and coin changes during pause are excluded

### Timeout Behavior

- Track `secondsSinceChange` — resets to 0 when purse changes
- When `secondsSinceChange >= timeoutMinutes * 60`:
  - Display shows red "Inactive" instead of time
  - Next purse change triggers auto-reset (new tracking session)

## Scoreboard Reading

Using a mixin to intercept `ScoreboardPlayerUpdateS2CPacket`:

1. When score update comes in, check if it's the "Purse:" line
2. Parse coin value (strip formatting, commas, symbols)
3. Update `currentPurse` in state
4. Reset `secondsSinceChange` to 0

A separate tick handler (once per second) increments `elapsedSeconds` and `secondsSinceChange`.

## Profile Switch Detection

Listen for chat messages via `ClientReceiveMessageEvents.GAME`:

- If message contains "Switching to profile" → call reset

## Overlay Rendering

### Display Format

```
Gained: 1,234,567 ¢
Time: 45:30
Rate: 2,469,134 ¢/hr
```

- Labels: Gold + Bold
- Values: White
- Time when inactive: Red "Inactive"

### Edit Mode (OverlayEditScreen)

- Semi-transparent background
- Overlay rendered with visible border
- **Drag**: Click and drag to reposition
- **Arrow keys**: Nudge 1px (10px with Shift)
- **Escape/Enter**: Save and exit

## Config File

Location: `.minecraft/config/cointracker.json`

```json
{
  "enabled": true,
  "overlayX": 10,
  "overlayY": 10,
  "scale": 1.0,
  "timeoutMinutes": 5
}
```

- Loaded on mod init, creates default if missing
- Saved immediately when changed via command
- Uses Gson (bundled with Minecraft)

State (startPurse, elapsedSeconds, etc.) is NOT persisted — resets on game restart.

## Dependencies

- Fabric API only (no external libraries)
- Minecraft 1.21.11
- Java 21
