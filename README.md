# Climbing Training

An Android app hosting tools for climbing exercise groups.

## Tools

### Flying Loto
A randomised number game for climbing sessions. The game master enters the names of all participants — each is secretly assigned a unique number between 1 and 30. During the game, the master taps **Next Number** to reveal a random draw. When a drawn number matches a player's assignment, their name lights up on screen. Tap **Stop** at any time to end the round; player names are saved for the next session.

---

## Requirements

| Tool | Version |
|------|---------|
| Android Studio | Hedgehog 2023.1+ |
| JDK | 17 |
| Android SDK | API 34 |
| Gradle | 8.5 (wrapper included) |

Minimum supported Android version: **8.0 (API 26)**

---

## Build

Clone the repo, then open the project in Android Studio or build from the command line:

```bash
# Debug build
./gradlew assembleDebug

# Release build (version injected by CI — see below)
./gradlew assembleRelease
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

---

## Tests

Unit tests run on the JVM via Robolectric — no emulator required:

```bash
./gradlew testDebugUnitTest
```

HTML reports are generated at `app/build/reports/tests/testDebugUnitTest/index.html`.

**Test coverage**

| Class | Tests |
|-------|-------|
| `FlyingLotoViewModel` | 22 unit tests — player management, number assignment, game flow, persistence |
| `PlayerPreferences` | 11 Robolectric tests — JSON round-trip, ordering, special characters, edge cases |

---

## CI / CD

### Continuous Integration

Runs on every push and pull request to `main` or `develop`:

- Executes unit tests
- Builds a debug APK
- Uploads the APK and test report as workflow artifacts (7-day retention)

### Releases

Triggered by pushing a semver tag (`v0.1.0`, `v1.2.3`, …):

```bash
git tag v0.1.0
git push origin v0.1.0
```

The workflow will:
1. Run unit tests
2. Build a release APK with the version baked in
3. Create a **GitHub Release** with the APK attached as a downloadable asset

Versions starting with `0.` are automatically marked as **pre-release**.

**`versionCode`** is derived from the tag: `MAJOR × 10000 + MINOR × 100 + PATCH`.

> The release APK is currently unsigned. To enable signed builds, add a keystore as a GitHub secret and configure a `signingConfigs` block in `app/build.gradle.kts`.

---

## Project Structure

```
app/src/main/java/com/alma/climbingtraining/
├── MainActivity.kt               # AppCompatActivity — applies saved locale on start
├── navigation/
│   └── AppNavGraph.kt            # Jetpack Navigation routes
├── ui/
│   ├── home/
│   │   └── HomeScreen.kt         # Tool list + settings icon
│   ├── flyingloto/
│   │   ├── FlyingLotoScreen.kt   # All game phases (entry / assignment / play)
│   │   └── FlyingLotoViewModel.kt
│   ├── settings/
│   │   └── SettingsScreen.kt     # Language picker
│   └── theme/
│       └── Theme.kt              # Material 3 green palette
├── data/
│   ├── PlayerNamesRepository.kt  # Interface for player persistence
│   ├── PlayerPreferences.kt      # SharedPreferences + JSON implementation
│   └── LanguagePreference.kt     # Persists chosen language tag
└── model/
    └── Player.kt
```

---

## Localisation

The app ships in **English** (default) and **French**. The language can be changed at any time from the Settings screen (gear icon in the top bar).

| Resource | Path |
|----------|------|
| English strings | `app/src/main/res/values/strings.xml` |
| French strings | `app/src/main/res/values-fr/strings.xml` |
| Supported locales | `app/src/main/res/xml/locale_config.xml` |

Switching language uses `AppCompatDelegate.setApplicationLocales()` which recreates the activity automatically. The selected language is persisted in SharedPreferences and restored on next launch.

---

## Versioning

This project follows [Semantic Versioning](https://semver.org/).  
Current version: **0.1.0** (pre-release — API and features may change).
