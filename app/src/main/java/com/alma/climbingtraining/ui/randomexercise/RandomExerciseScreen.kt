package com.alma.climbingtraining.ui.randomexercise

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alma.climbingtraining.R
import com.alma.climbingtraining.model.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomExerciseScreen(
    onNavigateBack: () -> Unit,
    viewModel: RandomExerciseViewModel = viewModel(
        factory = RandomExerciseViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val noMatchMessage = stringResource(R.string.random_exercise_no_match)

    LaunchedEffect(state.noMatchFound) {
        if (state.noMatchFound) {
            snackbarHostState.showSnackbar(noMatchMessage)
            viewModel.clearNoMatchFound()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.phase == RandomExercisePhase.FILTER)
                            stringResource(R.string.random_exercise_title)
                        else
                            stringResource(R.string.random_exercise_result_title)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.phase == RandomExercisePhase.RESULT) {
                                viewModel.changeFilters()
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AnimatedContent(
                targetState = state.phase,
                transitionSpec = {
                    if (targetState == RandomExercisePhase.RESULT) {
                        (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)))
                            .togetherWith(slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(150)))
                    } else {
                        (slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)))
                            .togetherWith(slideOutHorizontally(tween(300)) { it } + fadeOut(tween(150)))
                    }
                },
                label = "phase_transition"
            ) { phase ->
                when (phase) {
                    RandomExercisePhase.FILTER -> RandomExerciseFilterScreen(
                        filter = state.filter,
                        onToggleTargetAudience = viewModel::toggleTargetAudience,
                        onToggleEnergySystem = viewModel::toggleEnergySystem,
                        onToggleDiscipline = viewModel::toggleDiscipline,
                        onToggleLevel = viewModel::toggleLevel,
                        onToggleTechniqueFocus = viewModel::toggleTechniqueFocus,
                        onDraw = viewModel::drawExercise
                    )
                    RandomExercisePhase.RESULT -> {
                        val exercise = state.currentExercise
                        if (exercise != null) {
                            RandomExerciseResultScreen(
                                exercise = exercise,
                                onDrawAgain = viewModel::drawAgain,
                                onChangeFilters = viewModel::changeFilters
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RandomExerciseResultScreen(
    exercise: Exercise,
    onDrawAgain: () -> Unit,
    onChangeFilters: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = exercise.name, style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.testTag(TAG_RANDOM_EXERCISE_EXERCISE_NAME))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                exercise.targetAudience.forEach { BadgeChip(stringResource(it.labelRes())) }
                exercise.disciplines.forEach {
                    BadgeChip(stringResource(it.labelRes()), MaterialTheme.colorScheme.secondaryContainer)
                }
                BadgeChip(stringResource(exercise.level.labelRes()), MaterialTheme.colorScheme.tertiaryContainer)
                exercise.techniqueFocus.forEach { BadgeChip(stringResource(it.labelRes())) }
            }

            exercise.energySystems.forEach { energy ->
                EnergyBadgeCard(
                    label = stringResource(energy.labelRes()),
                    description = stringResource(energy.descriptionRes())
                )
            }

            Divider()

            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onDrawAgain,
                modifier = Modifier.fillMaxWidth().testTag(TAG_RANDOM_EXERCISE_DRAW_AGAIN_BUTTON)) {
                Text(stringResource(R.string.random_exercise_draw_again_button))
            }
            OutlinedButton(onClick = onChangeFilters,
                modifier = Modifier.fillMaxWidth().testTag(TAG_RANDOM_EXERCISE_CHANGE_FILTERS_BUTTON)) {
                Text(stringResource(R.string.random_exercise_change_filters_button))
            }
        }
    }
}

@Composable
private fun BadgeChip(
    label: String,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        tonalElevation = 2.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun EnergyBadgeCard(label: String, description: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
