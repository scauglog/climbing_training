# Random Exercise

## Overview

A tool that randomly selects a climbing training exercise from a library stored in JSON files. The user can filter exercises by various criteria before drawing a random one.

---

## Home Screen Integration

The **Random Exercise** tool appears as a card on the Home Screen, after the Flying Loto card. Same card format: tool name, short description, and icon.

---

## Screens

### 1. Random Exercise - Filter Screen (`/random-exercise`)

Displays a set of optional filters the user can apply before drawing an exercise. All filters are optional — leaving all filters unset means the draw picks from the full library.

#### Filter: Target Audience
Multi-select chips or checkboxes:
- **Child**
- **Adult**

#### Filter: Energy System
Multi-select chips or checkboxes, each with a brief description shown below the label:

| Value | Label | Description |
|---|---|---|
| `pure_strength` | Pure Strength | Short, intense efforts requiring maximal force output. |
| `strength_endurance` | Strength-Endurance | Ability to sustain hard moves for 10–60 seconds. |
| `endurance` | Endurance | Ability to climb for minutes or hours at a moderate pace. |
| `stamina` | Stamina | Ability to recover between hard efforts during a session. |

#### Filter: Discipline
Multi-select chips or checkboxes:
- **Boulder**
- **Lead Climbing**
- **Top Rope**

#### Filter: Level
Multi-select chips or checkboxes:
- **Beginner**
- **Intermediate**
- **Advanced**
- **Expert**

#### Filter: Technique Focus
Multi-select chips or checkboxes:
- **Footwork** — exercises targeting foot placement and precision
- **Handwork** — exercises targeting grip, crimp, open-hand technique
- **Body Movement** — exercises targeting balance, hip position, coordination
- **Other**

#### Bottom Action
- **"Draw an Exercise"** button fixed at the bottom of the screen
- Enabled at all times (filters are optional)
- Tapping it selects a random exercise matching the active filters and navigates to the Result Screen
- If no exercises match the current filter combination, a snackbar/toast is shown: *"No exercises match these filters. Try removing some criteria."* and no navigation occurs.

---

### 2. Random Exercise - Result Screen (`/random-exercise/result`)

Displays the selected exercise in full detail.

**Exercise card content:**
- Exercise name (large, prominent)
- Target audience badge(s)
- Energy system badge with short description
- Discipline badge(s)
- Level badge
- Technique focus badge(s)
- Full description / instructions (scrollable)
- Optional image or illustration (if provided in the JSON)

**Action buttons at the bottom:**
- **"Draw Again"** — picks a new random exercise using the same active filters, replaces the current result in place (with a brief animation)
- **"Change Filters"** — navigates back to the Filter Screen, preserving the current filter state

---

## Exercise Library — Data Format

Exercises are stored as JSON files under `app/src/main/assets/exercises/`. The repository loads **all** JSON files in that folder and merges them into a single library — this is transparent to the user.

### File naming convention

Files are named with a language prefix so that locale-specific content can be provided:

| Prefix | Language |
|--------|----------|
| `en_`  | English  |
| `fr_`  | French   |

At runtime the app detects the current locale and loads **only the files that match the active language prefix**. If no file exists for the active language it falls back to English (`en_`). Files without a recognised prefix are ignored.

Examples of valid file names:
```
en_exercises_beginner.json
en_exercises_advanced.json
fr_exercises_debutant.json
fr_exercises_avance.json
```

Each file contains a JSON array of exercise objects. The repository scans the whole `exercises/` folder, filters by language prefix, and merges all matching arrays into one flat list. Adding a new file requires no code change.

### Exercise JSON schema

Each file can contain one or more exercises. The schema for a single exercise:

```json
{
  "id": "unique_string_id",
  "name": "Exercise name",
  "targetAudience": ["child", "adult"],
  "energySystems": ["pure_strength", "strength_endurance", "endurance", "stamina"],
  "disciplines": ["boulder", "lead", "top_rope"],
  "level": "beginner",
  "techniqueFocus": ["footwork", "handwork", "body_movement", "other"],
  "description": "Full description of the exercise, instructions, tips...",
  "imageAsset": "exercises/images/my_exercise.png"
}
```

**Field constraints:**
- `id`: unique, non-empty string
- `name`: non-empty string
- `targetAudience`: non-empty array, values in `["child", "adult"]`
- `energySystems`: non-empty array, values in `["pure_strength", "strength_endurance", "endurance", "stamina"]`
- `disciplines`: non-empty array, values in `["boulder", "lead", "top_rope"]`
- `level`: one of `["beginner", "intermediate", "advanced", "expert"]`
- `techniqueFocus`: non-empty array, values in `["footwork", "handwork", "body_movement", "other"]`
- `description`: non-empty string
- `imageAsset`: optional string (relative path in assets)

---

## Data Model

```kotlin
enum class TargetAudience { CHILD, ADULT }

enum class EnergySystem {
    PURE_STRENGTH,
    STRENGTH_ENDURANCE,
    ENDURANCE,
    STAMINA
}

enum class Discipline { BOULDER, LEAD, TOP_ROPE }

enum class ExerciseLevel { BEGINNER, INTERMEDIATE, ADVANCED, EXPERT }

enum class TechniqueFocus { FOOTWORK, HANDWORK, BODY_MOVEMENT, OTHER }

data class Exercise(
    val id: String,
    val name: String,
    val targetAudience: List<TargetAudience>,
    val energySystems: List<EnergySystem>,
    val disciplines: List<Discipline>,
    val level: ExerciseLevel,
    val techniqueFocus: List<TechniqueFocus>,
    val description: String,
    val imageAsset: String? = null
)

data class ExerciseFilter(
    val targetAudience: Set<TargetAudience> = emptySet(),
    val energySystems: Set<EnergySystem> = emptySet(),
    val disciplines: Set<Discipline> = emptySet(),
    val levels: Set<ExerciseLevel> = emptySet(),
    val techniqueFocus: Set<TechniqueFocus> = emptySet()
)

data class RandomExerciseState(
    val filter: ExerciseFilter = ExerciseFilter(),
    val currentExercise: Exercise? = null,
    val noMatchFound: Boolean = false
)
```

---

## ViewModel Logic

1. **Load exercises** → detect current locale, scan `assets/exercises/`, load all files whose name starts with the matching language prefix (`en_` / `fr_`), fall back to `en_` if no locale-specific files exist, parse and merge into a single list
2. **Apply filter** → an exercise matches the filter if, for each non-empty filter set, the exercise's corresponding field contains **at least one** value from the filter set (OR logic within a filter category, AND logic across categories)
3. **Draw** → filter the full library, pick one at random; if the filtered list is empty set `noMatchFound = true`
4. **Draw again** → same as Draw, using the same `filter`
5. **Change filters** → navigate back to Filter Screen, keep `filter` state intact

---

## Project Structure additions

```
app/
├── src/main/
│   ├── assets/
│   │   └── exercises/
│   │       ├── en_exercises.json
│   │       ├── en_exercises_advanced.json
│   │       ├── fr_exercises.json
│   │       └── ...
│   └── java/com/alma/climbingtraining/
│       ├── ui/
│       │   └── randomexercise/
│       │       ├── RandomExerciseFilterScreen.kt
│       │       ├── RandomExerciseResultScreen.kt
│       │       └── RandomExerciseViewModel.kt
│       ├── data/
│       │   └── ExerciseRepository.kt
│       └── model/
│           └── Exercise.kt
```

---

## UI/UX Notes

- Filter chips should clearly indicate selected vs. unselected state (filled vs. outlined)
- Energy system filter items display their description as a subtitle below the label
- The Result Screen exercise card should be scrollable to accommodate long descriptions
- "Draw Again" replaces the card with a subtle slide or fade animation
- Back navigation from Result Screen always returns to the Filter Screen (not the home screen)
- No persistence required for filters between sessions (filters reset on each entry to the tool)
