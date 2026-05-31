package com.alma.climbingtraining.ui.warmup

import android.app.Application
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alma.climbingtraining.R
import com.alma.climbingtraining.model.IntervalType
import com.alma.climbingtraining.model.WarmupPhase

// ── Body part label helper ────────────────────────────────────────────────────

@Composable
fun bodyPartLabel(slug: String): String = when (slug) {
    "wrists"     -> stringResource(R.string.warmup_body_part_wrists)
    "shoulders"  -> stringResource(R.string.warmup_body_part_shoulders)
    "core"       -> stringResource(R.string.warmup_body_part_core)
    "legs"       -> stringResource(R.string.warmup_body_part_legs)
    "hips"       -> stringResource(R.string.warmup_body_part_hips)
    "back"       -> stringResource(R.string.warmup_body_part_back)
    "cardio"     -> stringResource(R.string.warmup_body_part_cardio)
    "full_body"  -> stringResource(R.string.warmup_body_part_full_body)
    else         -> slug
}

// ── Root screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarmupScreen(
    onNavigateBack: () -> Unit,
    viewModel: WarmupViewModel = viewModel(
        factory = WarmupViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.warmup_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.phase == WarmupPhase.SETUP || state.phase == WarmupPhase.FINISHED) {
                            onNavigateBack()
                        } else {
                            viewModel.stop()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (state.phase) {
                WarmupPhase.SETUP -> WarmupSetupContent(
                    durationMinutes = state.durationMinutes,
                    totalIntervals = state.totalIntervals,
                    coveredCount = viewModel.coveredBodyPartsCount(state.durationMinutes),
                    totalBodyParts = viewModel.totalBodyPartsCount(),
                    onDecrement = viewModel::decrementDuration,
                    onIncrement = viewModel::incrementDuration,
                    onStart = viewModel::start
                )
                WarmupPhase.RUNNING, WarmupPhase.PAUSED -> WarmupTimerContent(
                    state = state,
                    onPauseResume = {
                        if (state.phase == WarmupPhase.RUNNING) viewModel.pause()
                        else viewModel.resume()
                    },
                    onStop = viewModel::stop
                )
                WarmupPhase.FINISHED -> WarmupDoneContent(
                    durationMinutes = state.durationMinutes,
                    coveredBodyParts = state.coveredBodyParts,
                    onBackToSetup = viewModel::stop
                )
            }
        }
    }
}

// ── Setup ─────────────────────────────────────────────────────────────────────

@Composable
fun WarmupSetupContent(
    durationMinutes: Int,
    totalIntervals: Int,
    coveredCount: Int,
    totalBodyParts: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onStart: () -> Unit
) {
    val rounds = totalIntervals / 2
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.warmup_duration_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                FilledIconButton(
                    onClick = onDecrement,
                    enabled = durationMinutes > 1,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag(TAG_WARMUP_DECREMENT_BUTTON)
                ) {
                    Text("−", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = stringResource(R.string.warmup_duration_value, durationMinutes),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag(TAG_WARMUP_DURATION_DISPLAY)
                )

                FilledIconButton(
                    onClick = onIncrement,
                    enabled = durationMinutes < 60,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag(TAG_WARMUP_INCREMENT_BUTTON)
                ) {
                    Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val summaryText = if (coveredCount >= totalBodyParts) {
                stringResource(R.string.warmup_rounds_summary_full, rounds)
            } else {
                stringResource(R.string.warmup_rounds_summary_partial, rounds, coveredCount, totalBodyParts)
            }
            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag(TAG_WARMUP_ROUNDS_SUMMARY)
            )
        }

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TAG_WARMUP_START_BUTTON)
        ) {
            Text(
                text = stringResource(R.string.warmup_start_button),
                fontSize = 18.sp
            )
        }
    }
}

// ── Timer ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WarmupTimerContent(
    state: WarmupState,
    onPauseResume: () -> Unit,
    onStop: () -> Unit
) {
    val isWork = state.intervalType == IntervalType.WORK
    val intervalColor = if (isWork)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.tertiaryContainer

    val progress = if (state.totalIntervals > 0)
        state.completedIntervals.toFloat() / state.totalIntervals.toFloat()
    else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress bar
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        val remainingMinutes = state.remainingTotalSeconds / 60
        val remainingSeconds = state.remainingTotalSeconds % 60
        Text(
            text = stringResource(
                R.string.warmup_remaining,
                remainingMinutes,
                remainingSeconds
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        // Interval type badge
        Card(
            colors = CardDefaults.cardColors(containerColor = intervalColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isWork) stringResource(R.string.warmup_work)
                    else stringResource(R.string.warmup_rest),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag(TAG_WARMUP_INTERVAL_LABEL)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Countdown
                AnimatedContent(
                    targetState = state.intervalSecondsRemaining,
                    transitionSpec = {
                        (scaleIn(tween(150)) + fadeIn(tween(150)))
                            .togetherWith(scaleOut(tween(100)) + fadeOut(tween(100)))
                    },
                    label = "countdown"
                ) { seconds ->
                    Text(
                        text = seconds.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag(TAG_WARMUP_COUNTDOWN)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isWork && state.currentExercise != null) {
                    Text(
                        text = state.currentExercise.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag(TAG_WARMUP_EXERCISE_NAME)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Body part chip
                    SuggestionChip(
                        onClick = {},
                        label = { Text(bodyPartLabel(state.currentExercise.bodyPart)) },
                        modifier = Modifier.testTag(TAG_WARMUP_BODY_PART_CHIP)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.currentExercise.description,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = stringResource(R.string.warmup_get_ready, state.nextExercise?.name ?: ""),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag(TAG_WARMUP_EXERCISE_NAME)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier
                    .weight(1f)
                    .testTag(TAG_WARMUP_STOP_BUTTON),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Text(
                    text = stringResource(R.string.warmup_stop_button),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Button(
                onClick = onPauseResume,
                modifier = Modifier
                    .weight(1f)
                    .testTag(TAG_WARMUP_PAUSE_RESUME_BUTTON)
            ) {
                Icon(
                    if (state.phase == WarmupPhase.RUNNING) Icons.Default.Pause
                    else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Text(
                    text = if (state.phase == WarmupPhase.RUNNING)
                        stringResource(R.string.warmup_pause_button)
                    else
                        stringResource(R.string.warmup_resume_button),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

// ── Done ──────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WarmupDoneContent(
    durationMinutes: Int,
    coveredBodyParts: Set<String>,
    onBackToSetup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.warmup_done_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(TAG_WARMUP_DONE_MESSAGE)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.warmup_done_duration, durationMinutes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (coveredBodyParts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.warmup_done_body_parts_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag(TAG_WARMUP_DONE_BODY_PARTS)
            ) {
                coveredBodyParts.sorted().forEach { slug ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text(bodyPartLabel(slug)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onBackToSetup,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TAG_WARMUP_BACK_TO_SETUP_BUTTON)
        ) {
            Text(stringResource(R.string.warmup_back_to_setup_button))
        }
    }
}
