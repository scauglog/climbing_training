package com.alma.climbingtraining.ui.randomexercise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alma.climbingtraining.R
import com.alma.climbingtraining.model.Discipline
import com.alma.climbingtraining.model.EnergySystem
import com.alma.climbingtraining.model.ExerciseFilter
import com.alma.climbingtraining.model.ExerciseLevel
import com.alma.climbingtraining.model.TargetAudience
import com.alma.climbingtraining.model.TechniqueFocus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RandomExerciseFilterScreen(
    filter: ExerciseFilter,
    onToggleTargetAudience: (TargetAudience) -> Unit,
    onToggleEnergySystem: (EnergySystem) -> Unit,
    onToggleDiscipline: (Discipline) -> Unit,
    onToggleLevel: (ExerciseLevel) -> Unit,
    onToggleTechniqueFocus: (TechniqueFocus) -> Unit,
    onDraw: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Target Audience
            FilterSection(title = stringResource(R.string.random_exercise_filter_audience_title)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TargetAudience.entries.forEach { value ->
                        FilterChip(
                            selected = value in filter.targetAudience,
                            onClick = { onToggleTargetAudience(value) },
                            label = { Text(stringResource(value.labelRes())) }
                        )
                    }
                }
            }

            // Energy System
            FilterSection(title = stringResource(R.string.random_exercise_filter_energy_title)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EnergySystem.entries.forEach { value ->
                        FilterChip(
                            selected = value in filter.energySystems,
                            onClick = { onToggleEnergySystem(value) },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = stringResource(value.labelRes()),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = stringResource(value.descriptionRes()),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Discipline
            FilterSection(title = stringResource(R.string.random_exercise_filter_discipline_title)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Discipline.entries.forEach { value ->
                        FilterChip(
                            selected = value in filter.disciplines,
                            onClick = { onToggleDiscipline(value) },
                            label = { Text(stringResource(value.labelRes())) }
                        )
                    }
                }
            }

            // Level
            FilterSection(title = stringResource(R.string.random_exercise_filter_level_title)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExerciseLevel.entries.forEach { value ->
                        FilterChip(
                            selected = value in filter.levels,
                            onClick = { onToggleLevel(value) },
                            label = { Text(stringResource(value.labelRes())) }
                        )
                    }
                }
            }

            // Technique Focus
            FilterSection(title = stringResource(R.string.random_exercise_filter_technique_title)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TechniqueFocus.entries.forEach { value ->
                        FilterChip(
                            selected = value in filter.techniqueFocus,
                            onClick = { onToggleTechniqueFocus(value) },
                            label = { Text(stringResource(value.labelRes())) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onDraw,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag(TAG_RANDOM_EXERCISE_DRAW_BUTTON)
        ) {
            Text(stringResource(R.string.random_exercise_draw_button))
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        content()
        Divider()
    }
}

fun TargetAudience.labelRes() = when (this) {
    TargetAudience.CHILD -> R.string.random_exercise_audience_child
    TargetAudience.ADULT -> R.string.random_exercise_audience_adult
}

fun EnergySystem.labelRes() = when (this) {
    EnergySystem.PURE_STRENGTH -> R.string.random_exercise_energy_pure_strength
    EnergySystem.STRENGTH_ENDURANCE -> R.string.random_exercise_energy_strength_endurance
    EnergySystem.ENDURANCE -> R.string.random_exercise_energy_endurance
    EnergySystem.STAMINA -> R.string.random_exercise_energy_stamina
}

fun EnergySystem.descriptionRes() = when (this) {
    EnergySystem.PURE_STRENGTH -> R.string.random_exercise_energy_pure_strength_desc
    EnergySystem.STRENGTH_ENDURANCE -> R.string.random_exercise_energy_strength_endurance_desc
    EnergySystem.ENDURANCE -> R.string.random_exercise_energy_endurance_desc
    EnergySystem.STAMINA -> R.string.random_exercise_energy_stamina_desc
}

fun Discipline.labelRes() = when (this) {
    Discipline.BOULDER -> R.string.random_exercise_discipline_boulder
    Discipline.LEAD -> R.string.random_exercise_discipline_lead
    Discipline.TOP_ROPE -> R.string.random_exercise_discipline_top_rope
}

fun ExerciseLevel.labelRes() = when (this) {
    ExerciseLevel.BEGINNER -> R.string.random_exercise_level_beginner
    ExerciseLevel.INTERMEDIATE -> R.string.random_exercise_level_intermediate
    ExerciseLevel.ADVANCED -> R.string.random_exercise_level_advanced
    ExerciseLevel.EXPERT -> R.string.random_exercise_level_expert
}

fun TechniqueFocus.labelRes() = when (this) {
    TechniqueFocus.FOOTWORK -> R.string.random_exercise_technique_footwork
    TechniqueFocus.HANDWORK -> R.string.random_exercise_technique_handwork
    TechniqueFocus.BODY_MOVEMENT -> R.string.random_exercise_technique_body_movement
    TechniqueFocus.OTHER -> R.string.random_exercise_technique_other
}
