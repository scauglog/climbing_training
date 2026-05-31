package com.alma.climbingtraining.model

data class WarmupExercise(
    val id: String,
    val name: String,           // display name
    val description: String,    // 1–2 sentence instruction shown on screen
    val bodyPart: String        // slug, e.g. "wrists"
)

enum class IntervalType { WORK, REST }

enum class WarmupPhase { SETUP, RUNNING, PAUSED, FINISHED }
