# Warmup Timer

## Overview

A guided Tabata-style warmup tool adapted for climbing. The user selects a duration, then the app runs a countdown timer cycling through randomly selected bodyweight exercises. The selection algorithm ensures a **full-body warmup** — every body part in the library is covered at least once before any repeats, provided enough time is available. A sound plays each time an exercise changes. Music support is planned for a future iteration.

---

## Home Screen Integration

The **Warmup Timer** tool appears as a card on the Home Screen, after the Random Exercise card. Same card format: tool name, short description, icon.

---

## Tabata Format

Each warmup round consists of alternating **Work** and **Rest** intervals:

| Interval | Duration |
|----------|----------|
| Work     | 20 s     |
| Rest     | 10 s     |

One full cycle (Work + Rest) = **30 seconds**.

Given a total duration of N minutes, the warmup contains **N × 2** full cycles. Each Work interval presents one exercise drawn from the exercise sequence (see **Exercise Sequencing** below).

---

## Screens

### 1. Warmup Setup Screen (`/warmup`)

- Title: "Warmup Timer"
- Large duration display showing the selected number of minutes (e.g. **10 min**)
- Two buttons to adjust duration:
  - **−** decreases by 1 minute (minimum: 1 minute)
  - **+** increases by 1 minute (maximum: 60 minutes)
- Default duration: **10 minutes**
- Summary line below the duration: "X rounds · covers all body parts" when the selected duration is long enough to cover all body parts at least once; otherwise "X rounds · partial warmup (Y/Z body parts)" where Y = distinct body parts covered and Z = total body parts in the library.
- **"Start"** button at the bottom — navigates to the Timer Screen

---

### 2. Warmup Timer Screen (`/warmup/timer`)

Displayed during the active warmup.

**Top section:**
- Progress bar (linear) showing overall warmup progress (intervals completed / total intervals)
- Total elapsed time or remaining time (e.g. "8:30 remaining")

**Center section:**
- Large label: **"WORK"** or **"REST"** (colored distinctly — e.g. green for Work, amber for Rest)
- Large countdown number (seconds remaining in current interval, e.g. **18**)
- Exercise name (large, prominent) — shown during Work intervals; "Get ready…" shown during Rest intervals
- Exercise description (short, 1–2 lines) — shown during Work intervals only
- Body part tag (e.g. "Wrists") — shown as a chip/badge below the exercise name during Work intervals

**Bottom section:**
- **"Pause / Resume"** button — pauses/resumes the countdown
- **"Stop"** button — ends the session early and navigates back to the Setup Screen

**End of warmup:**
- When all intervals are complete the screen transitions to a "Done!" state:
  - "Warmup complete! 🎉" message (text only, no emoji in code — use string resource)
  - Total duration displayed
  - List of body parts covered during the session
  - **"Back to setup"** button

---

## Sound

A short audio tone is played at the following moments:

| Event | Sound |
|-------|-------|
| Work interval starts (new exercise) | High-pitched short beep (800 Hz, 200 ms) |
| Rest interval starts | Low-pitched short beep (400 Hz, 200 ms) |
| Warmup complete | Three ascending beeps |

Sound is produced via Android's `ToneGenerator` — no audio asset files required.
Sound respects the device's current media volume. If the device is muted, no sound is played (no vibration fallback).

Future iteration: add optional background music (MediaPlayer, user-selectable track).

---

## Exercise Library

Exercises are stored in **JSON asset files**, one per language, following the same locale-fallback convention as the Random Exercise library:

```
app/src/main/assets/warmup/
├── en_warmup_exercises.json
└── fr_warmup_exercises.json
```

If no file matches the device locale, `en_warmup_exercises.json` is used as fallback.

### JSON Schema

```json
[
  {
    "id": "wrist_rotation",
    "name": "Wrist Rotation",
    "description": "Extend arms forward, make fists and rotate both wrists in full circles — inward then outward.",
    "bodyPart": "wrists"
  }
]
```

Fields:

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | Unique stable identifier |
| `name` | `String` | Display name (localised per file) |
| `description` | `String` | 1–2 sentence instruction shown on screen (localised per file) |
| `bodyPart` | `String` | Slug identifying the targeted body part (see canonical list below) |

### Canonical Body Parts

| Slug | Display label (EN) |
|------|--------------------|
| `wrists` | Wrists |
| `shoulders` | Shoulders |
| `core` | Core |
| `legs` | Legs |
| `hips` | Hips |
| `back` | Back |
| `cardio` | Cardio |
| `full_body` | Full Body |

A single exercise may target only **one** body part (the primary focus).

### Default English Exercise Library

Minimum 3 exercises per body part.

| ID | Name | Description | Body Part |
|----|------|-------------|-----------|
| `wrist_rotation` | Wrist Rotation | Extend arms forward, make fists and rotate both wrists in full circles — inward then outward. | `wrists` |
| `wrist_flexion` | Wrist Flexion & Extension | Place palms together in front of your chest, then reverse (backs of hands together) and push gently. | `wrists` |
| `wrist_figure_eight` | Wrist Figure-Eight | Interlace fingers and draw continuous figure-eight patterns with both hands. Keep wrists loose. | `wrists` |
| `arm_circle` | Arm Circle | Extend arms to the side. Draw large forward circles for 10 s then reverse for 10 s. | `shoulders` |
| `shoulder_roll` | Shoulder Roll | Roll both shoulders forward in large circles for 10 s, then backward for 10 s. | `shoulders` |
| `cross_body_arm_swing` | Cross-Body Arm Swing | Swing both arms across your chest simultaneously, then open them wide. Increase range each rep. | `shoulders` |
| `plank` | Plank | Hold a forearm plank position. Keep your core tight and back flat. | `core` |
| `mountain_climber` | Mountain Climber | Start in a high plank. Drive alternating knees toward your chest rapidly. | `core` |
| `dead_bug` | Dead Bug | Lie on your back, arms up and knees at 90°. Slowly lower opposite arm and leg toward the floor, then return. | `core` |
| `squat` | Squat | Feet shoulder-width apart, squat until thighs are parallel to the floor, drive through heels to stand. | `legs` |
| `lunge` | Lunge | Step forward with one leg, lower your back knee toward the floor, return to standing. Alternate legs. | `legs` |
| `lateral_shuffle` | Lateral Shuffle | Wide stance, stay low. Shuffle 3 steps left then 3 steps right in an athletic position. | `legs` |
| `hip_circle` | Hip Circle | Feet shoulder-width apart, hands on hips. Draw large circles with your hips, alternating directions. | `hips` |
| `hip_flexor_stretch` | Hip Flexor Stretch | Step into a lunge, drop the back knee to the floor, and push hips forward gently. Hold 10 s each side. | `hips` |
| `leg_swing` | Leg Swing | Hold a wall for balance. Swing one leg forward and back in a controlled arc, then switch sides. | `hips` |
| `cat_cow` | Cat-Cow | On all fours, alternate between arching your back toward the ceiling (cat) and dropping your belly toward the floor (cow). | `back` |
| `thoracic_rotation` | Thoracic Rotation | Sit cross-legged, hands behind your head. Rotate your upper body left then right, keeping hips still. | `back` |
| `standing_side_bend` | Standing Side Bend | Feet hip-width apart, raise one arm overhead and bend to the opposite side. Hold 5 s, then switch. | `back` |
| `jumping_jack` | Jumping Jack | Start with feet together and arms at sides. Jump feet apart while raising arms overhead, then return. | `cardio` |
| `high_knee` | High Knees | Jog in place, driving knees as high as possible. Pump arms in opposition. | `cardio` |
| `butt_kick` | Butt Kicks | Jog in place, kicking your heels up to touch your glutes with each step. Keep a quick pace. | `cardio` |
| `burpee` | Burpee | From standing: squat down, jump feet back to plank, do a push-up, jump feet in, jump up with arms overhead. | `full_body` |
| `bear_crawl` | Bear Crawl | Start on all fours with knees hovering 2 cm off the floor. Crawl forward 3 steps then backward 3 steps. | `full_body` |
| `inchworm` | Inchworm | Stand and hinge at the hips to place hands on the floor. Walk hands out to a plank, then walk them back and stand. | `full_body` |

---

## Exercise Sequencing

The goal is to guarantee **at least one exercise per body part** before any repeats, as long as the selected duration provides enough Work intervals.

### Algorithm

1. **On Start**, build an ordered sequence of exercises for the session:
   a. Shuffle all exercises randomly.
   b. Group by body part. From each group pick one exercise randomly → this forms the **coverage round** (one exercise per body part, in random order). Shuffle the coverage round.
   c. If `totalWorkIntervals > numberOfBodyParts`, fill the remaining slots by randomly sampling from all exercises (uniform random, with replacement allowed) until the sequence reaches `totalWorkIntervals` entries.
2. The sequence is fixed for the entire session (no re-randomisation mid-session).
3. `totalWorkIntervals = durationMinutes * 2 / 2 = durationMinutes` (half of all intervals are Work intervals).

> **Example** — 3 min session (6 intervals = 3 Work):  
> 8 body parts, 3 Work slots < 8 → only a partial coverage round is used (3 random body parts, one exercise each).

> **Example** — 10 min session (20 intervals = 10 Work):  
> 8 body parts → coverage round fills slots 1–8 (one per body part, shuffled), slots 9–10 are filled randomly.

---

## Data Model

```kotlin
data class WarmupExercise(
    val id: String,
    val name: String,           // display name
    val description: String,    // 1–2 sentence instruction shown on screen
    val bodyPart: String        // slug, e.g. "wrists"
)

enum class IntervalType { WORK, REST }

enum class WarmupPhase { SETUP, RUNNING, PAUSED, FINISHED }

data class WarmupState(
    val phase: WarmupPhase = WarmupPhase.SETUP,
    val durationMinutes: Int = 10,
    val totalIntervals: Int = 20,               // durationMinutes * 2
    val completedIntervals: Int = 0,
    val intervalType: IntervalType = IntervalType.WORK,
    val intervalSecondsRemaining: Int = 20,      // 20 for WORK, 10 for REST
    val currentExercise: WarmupExercise? = null,
    val nextExercise: WarmupExercise? = null,    // shown during REST interval
    val coveredBodyParts: Set<String> = emptySet() // accumulated during session
)
```

---

## ViewModel Logic

1. **Setup phase** — user adjusts `durationMinutes`; `totalIntervals = durationMinutes * 2`
2. **Start** — build the exercise sequence (see **Exercise Sequencing**), transition to `RUNNING`, load first exercise, start first WORK interval (20 s)
3. **Tick** — decrement `intervalSecondsRemaining` every second via a coroutine
4. **Interval end** — toggle `intervalType` (WORK → REST or REST → WORK):
   - If transitioning to **WORK**: increment `completedIntervals`, advance to next exercise in the pre-built sequence, add its `bodyPart` to `coveredBodyParts`, play Work beep
   - If transitioning to **REST**: play Rest beep
5. **Last interval ends** — transition to `FINISHED`, play completion sound
6. **Pause** — suspend the coroutine tick; phase → `PAUSED`
7. **Resume** — restart the coroutine tick; phase → `RUNNING`
8. **Stop** — cancel timer, phase → `SETUP`, reset all timer state
9. **Back to setup** (from FINISHED) — same as Stop

---

## Project Structure Additions

```
app/
├── src/main/assets/warmup/
│   ├── en_warmup_exercises.json
│   └── fr_warmup_exercises.json
└── src/main/java/com/alma/climbingtraining/
    ├── model/
    │   └── WarmupExercise.kt
    └── ui/
        └── warmup/
            ├── WarmupExerciseDataSource.kt   — JSON loader (replaces WarmupExerciseLibrary.kt)
            ├── WarmupViewModel.kt
            ├── WarmupScreen.kt
            └── WarmupTestTags.kt
```

---

## UI/UX Notes

- The countdown number should animate (fade or scale) on each second tick for visual clarity
- Work interval background tint: green (`primaryContainer`)
- Rest interval background tint: amber/orange (`tertiaryContainer`)
- Done state: neutral background with a completion message and body part summary
- The Setup screen "−" button is disabled when duration is already 1 minute
- The Setup screen "+" button is disabled when duration is already 60 minutes
- Prevent the screen from sleeping during an active or paused session (keep screen on flag)
- Back navigation during a running session should trigger a confirmation or behave the same as "Stop"
- The body part tag on the Timer screen should use a `SuggestionChip` or similar small pill component
