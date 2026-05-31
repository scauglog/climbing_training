package com.alma.climbingtraining.model

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

fun Exercise.matchesFilter(filter: ExerciseFilter): Boolean {
    if (filter.targetAudience.isNotEmpty() && targetAudience.none { it in filter.targetAudience }) return false
    if (filter.energySystems.isNotEmpty() && energySystems.none { it in filter.energySystems }) return false
    if (filter.disciplines.isNotEmpty() && disciplines.none { it in filter.disciplines }) return false
    if (filter.levels.isNotEmpty() && level !in filter.levels) return false
    if (filter.techniqueFocus.isNotEmpty() && techniqueFocus.none { it in filter.techniqueFocus }) return false
    return true
}
