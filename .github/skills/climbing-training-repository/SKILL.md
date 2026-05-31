---
name: climbing-training-repository
user-invocable: true
description: "Explain how the Climbing Training Android repository works, including architecture, workflows, software craftsmanship principles, and mandatory testing practices. Use when onboarding contributors or implementing new features."
argument-hint: "Optionally provide the feature area (home, flying-loto, settings, data, navigation, CI) and whether you want implementation or review guidance."
---

# Climbing Training Repository Guide

## Purpose
This skill explains how this repository works and how to contribute safely.

The project is an Android application built with Kotlin and Jetpack Compose. It currently delivers climbing session tools, with Flying Loto as the first tool.

## Software Craftsmanship Principles
We follow software craftsmanship principles in day-to-day development:
- Keep code simple, readable, and intentional.
- Prefer small, focused functions with clear responsibilities.
- Make behavior explicit through tests before or alongside implementation.
- Refactor continuously to remove duplication and improve naming.
- Preserve quality gates: passing tests and reproducible builds are non-negotiable.
- Leave the codebase better than you found it.

## Core Stack
- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Architecture: ViewModel + StateFlow
- Persistence: SharedPreferences via repository abstraction
- Build: Gradle (Kotlin DSL)
- Min SDK: 26
- Compile/Target SDK: 34
- JDK: 17

## Repository Layout
- app/src/main/java/com/alma/climbingtraining/
- app/src/test/java/com/alma/climbingtraining/
- .github/workflows/ci.yml
- .github/workflows/release.yml
- README.md
- SPEC.md

Main code areas:
- ui/home/: home screen and tool list.
- ui/flyingloto/: Flying Loto screens and state handling.
- ui/settings/: language settings UI.
- navigation/: navigation graph.
- data/: persistence interfaces and implementations.
- model/: shared data models.

## How Features Work
Typical flow:
1. UI composables render from immutable state.
2. User actions call ViewModel intents (add player, validate, start game, next number, stop game).
3. ViewModel updates StateFlow and orchestrates rules.
4. Data layer persists long-lived values (for example player names, language preferences).
5. UI recomposes from updated state.

## Testing Standard (Required)
Testing is mandatory for behavior changes.

Rules:
- Write tests for each function or behavior you add/change in ViewModel or data code.
- Cover normal path, edge cases, and invalid input.
- Use Robolectric/JVM tests to avoid emulator dependence for unit-level logic.
- Add or update UI instrumentation tests for new user-facing functionality (navigation flows, screen state transitions, or critical interactions).
- Prefer deterministic tests with fake repositories and controlled dispatchers.
- Keep tests readable with clear scenario naming.

Current examples:
- FlyingLotoViewModelTest: game flow, transitions, assignment, draw behavior, persistence interactions.
- PlayerPreferencesTest: serialization/deserialization and storage edge cases.

## Development Workflow
1. Read SPEC.md and existing code in the targeted feature area.
2. Add or update tests first (or in the same change) to describe expected behavior.
3. Implement minimal code to satisfy tests.
4. Refactor for readability and maintainability.
5. Run local quality checks.

Recommended local commands:
- ./gradlew testDebugUnitTest
- ./gradlew connectedDebugAndroidTest
- ./gradlew assembleDebug

## CI and Release Guardrails
CI (.github/workflows/ci.yml) runs on push/PR to main and develop:
- Unit tests
- Debug APK build
- Artifact upload (test report and APK)

Release (.github/workflows/release.yml) runs on semver tags:
- Unit tests
- Release APK build with injected versionName/versionCode
- GitHub release publication

## Good Development Practices for This Repo
- Prefer composition over large monolithic classes.
- Keep business logic out of composables when possible; put it in ViewModel/use-case style functions.
- Use interfaces for storage dependencies to keep logic testable.
- Avoid hidden side effects; make state transitions explicit.
- Keep public behavior documented in tests and reflected in README/SPEC when needed.
- Do not merge code that weakens tests or bypasses CI guarantees.

## Definition of Done
A change is done when:
- New or modified functions are covered by meaningful tests.
- ./gradlew testDebugUnitTest passes.
- ./gradlew assembleDebug succeeds.
- Architecture boundaries remain clear (UI -> ViewModel -> data).
- Documentation is updated when behavior changes are user-visible.
