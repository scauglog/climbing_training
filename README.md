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

UI instrumentation tests run on an Android emulator:

```bash
# Run all connected instrumentation tests
./gradlew connectedDebugAndroidTest

# Run only the Flying Loto end-to-end instrumentation test
./gradlew :app:connectedDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=com.alma.climbingtraining.ui.flyingloto.FlyingLotoFlowInstrumentationTest
```

Instrumentation reports are generated under:
- `app/build/reports/androidTests/connected/`
- `app/build/outputs/androidTest-results/connected/`

### Running UI tests in CI only when needed

This repository has a dedicated workflow for emulator-based UI tests:
- `.github/workflows/ui-tests.yml`

To trigger it for a pull request, add the label configured in that workflow condition:
- currently: `test:UI`

If you prefer the label name `UI:test`, update the `if:` condition in `.github/workflows/ui-tests.yml` to match.

**Test coverage**

| Class | Tests |
|-------|-------|
| `FlyingLotoViewModel` | 22 unit tests — player management, number assignment, game flow, persistence |
| `PlayerPreferences` | 11 Robolectric tests — JSON round-trip, ordering, special characters, edge cases |

### Emulator Testing Without Android Studio

You can set up and run an Android 15 emulator entirely from the command line.

1. Ensure JDK 17 is installed.
2. From the repository root, run:

```bash
# Print shell env exports (ANDROID_SDK_ROOT + PATH)
./scripts/android-emulator-cli.sh env

# Install Android command-line tools, SDK packages, and create AVD (Android 15)
./scripts/android-emulator-cli.sh setup

# Start the emulator and wait until it is fully booted
./scripts/android-emulator-cli.sh start

# Build and install the debug app on the running emulator
./scripts/android-emulator-cli.sh install-app

# Optional: run connected Android tests on emulator
./scripts/android-emulator-cli.sh connected-tests
```

Default AVD name is `Pixel_Android15` and default API level is `35`.

You can override defaults with env vars, for example:

```bash
ANDROID_API_LEVEL=35 AVD_NAME=MyAndroid15 ./scripts/android-emulator-cli.sh setup
```

Script location: `scripts/android-emulator-cli.sh`

---

## Contributor Guide

For onboarding and contribution standards, see the repository skill:

- [climbing-training-repository skill](.github/skills/climbing-training-repository/SKILL.md)

It documents architecture, workflow, software craftsmanship principles, and mandatory testing expectations (tests for each function/behavior change).

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
