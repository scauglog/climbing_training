# Flying loto
## 2. Flying Loto - Setup Screen (`/flying-loto`)

**Phase 1: Player Entry**
- Text input field to enter a player name
- "Add" button to add the name to the list
- List of entered players displayed below (with delete option per player)
- Max 30 players (since numbers are 1–30, unique assignment)
- "Validate" button (enabled when at least 1 player is added)

**Phase 2: Number Assignment (after validation)**
- Each player is randomly assigned a unique number between 1 and 30
- Display the list: `Name → Number`
- "Start Game" button

## 3. Flying Loto - Game Screen (`/flying-loto/game`)

- Large number displayed prominently in the center of the screen
- If the number is assigned to a player, the player's name appears below the number (highlighted)
- If not assigned to anyone, no name shown (or "—")
- Two action buttons:
  - **"Next Number"** — draws a new random number between 1 and 30 and displays it
  - **"Stop"** — ends the game and navigates back to the player entry screen (Phase 1)
- Numbers can repeat during the game (random draw each press)
- No timer — the game master controls pacing manually via the "Next Number" button
- On stop: player names are preserved and pre-filled on the setup screen for the next round

---

# Data Model

```kotlin
data class Player(
    val name: String,
    val assignedNumber: Int? = null
)

data class FlyingLotoState(
    val playerNames: List<String> = emptyList(),   // persisted names
    val players: List<Player> = emptyList(),        // names + assigned numbers
    val phase: GamePhase = GamePhase.PLAYER_ENTRY,
    val currentNumber: Int? = null
)

enum class GamePhase {
    PLAYER_ENTRY,
    CONFIGURATION,
    PLAYING
}
```

---

## Game Logic (ViewModel)

1. **Add players** → maintain a mutable list of names
2. **Validate** → shuffle numbers 1–30, assign first N to N players
3. **Next number** → pick a random number 1–30, update `currentNumber` in state
4. **Lookup** → check if `currentNumber` matches any player's `assignedNumber`
5. **Stop** → reset phase to PLAYER_ENTRY, keep `playerNames` list intact, clear assignments and `currentNumber`

---

## Persistence

- Player names are saved to **SharedPreferences** so they survive:
  - Stopping the game (going back to setup)
  - App restarts
- On app launch / entering Flying Loto, previously saved names are loaded and pre-filled
- Users can still add/remove names before validating a new round

---

## UI/UX Notes

- Numbers should appear with a brief scale/fade animation for visual impact
- Player name highlight when matched: use a distinct color (e.g., green)
- Keep the game screen simple and readable from a distance (large font for the number)
- Prevent screen from turning off during the game (keep screen on flag)
