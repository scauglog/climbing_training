package com.alma.climbingtraining.ui.randompartner

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alma.climbingtraining.R
import com.alma.climbingtraining.model.Participant
import com.alma.climbingtraining.model.PartnerGroup
import com.alma.climbingtraining.model.RandomPartnerPhase
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomPartnerScreen(
    onNavigateBack: () -> Unit,
    viewModel: RandomPartnerViewModel = viewModel(
        factory = RandomPartnerViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.random_partner_title)) },
                navigationIcon = {
                    if (state.phase != RandomPartnerPhase.PRIVATE_INPUT) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
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
                RandomPartnerPhase.ENTRY -> EntryContent(
                    participants = state.participants,
                    toleranceKg = state.toleranceKg,
                    onAddParticipant = viewModel::startPrivateInput,
                    onRemoveParticipant = viewModel::removeParticipant,
                    onSetTolerance = viewModel::setTolerance,
                    onFindPartners = viewModel::findPartners
                )
                RandomPartnerPhase.PRIVATE_INPUT -> PrivateInputContent(
                    onConfirm = viewModel::confirmParticipant,
                    onCancel = viewModel::cancelPrivateInput
                )
                RandomPartnerPhase.RESULT -> ResultContent(
                    groups = state.groups,
                    onTryAgain = viewModel::tryAgain,
                    onRestart = viewModel::restart
                )
            }
        }
    }
}

// ── Entry screen ──────────────────────────────────────────────────────────────

@Composable
fun EntryContent(
    participants: List<Participant>,
    toleranceKg: Float,
    onAddParticipant: () -> Unit,
    onRemoveParticipant: (Participant) -> Unit,
    onSetTolerance: (Float) -> Unit,
    onFindPartners: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tolerance setting
        ToleranceRow(toleranceKg = toleranceKg, onSetTolerance = onSetTolerance)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.random_partner_participant_count, participants.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(participants, key = { it.id }) { participant ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = participant.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        IconButton(onClick = { onRemoveParticipant(participant) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(
                                    R.string.random_partner_remove_participant_content_description,
                                    participant.name
                                ),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onAddParticipant,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TAG_ADD_PARTICIPANT_BUTTON)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.random_partner_add_participant))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onFindPartners,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TAG_FIND_PARTNERS_BUTTON),
            enabled = participants.size >= 2
        ) {
            Text(stringResource(R.string.random_partner_find_partners))
        }
    }
}

@Composable
fun ToleranceRow(toleranceKg: Float, onSetTolerance: (Float) -> Unit) {
    var text by remember(toleranceKg) { mutableStateOf(toleranceKg.roundToInt().toString()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.random_partner_tolerance_label),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = text,
            onValueChange = { input ->
                text = input
                input.toFloatOrNull()?.let { v -> if (v >= 1f) onSetTolerance(v) }
            },
            modifier = Modifier
                .width(80.dp)
                .testTag(TAG_TOLERANCE_INPUT),
            singleLine = true,
            suffix = { Text("kg") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

// ── Private input screen ──────────────────────────────────────────────────────

@Composable
fun PrivateInputContent(
    onConfirm: (name: String, weightKg: Float) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }

    val weightValid = weightText.toFloatOrNull()?.let { it > 0f } ?: false
    val canConfirm = name.isNotBlank() && weightValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.random_partner_private_input_instruction),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TAG_PARTICIPANT_NAME_INPUT),
            label = { Text(stringResource(R.string.random_partner_name_label)) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TAG_PARTICIPANT_WEIGHT_INPUT),
            label = { Text(stringResource(R.string.random_partner_weight_label)) },
            singleLine = true,
            suffix = { Text("kg") },
            // Obscure weight so bystanders cannot read it
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onConfirm(name, weightText.toFloat()) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TAG_CONFIRM_PARTICIPANT_BUTTON),
            enabled = canConfirm
        ) {
            Text(stringResource(R.string.random_partner_confirm), fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TAG_CANCEL_PRIVATE_INPUT_BUTTON)
        ) {
            Text(stringResource(R.string.random_partner_cancel))
        }
    }
}

// ── Result screen ─────────────────────────────────────────────────────────────

@Composable
fun ResultContent(
    groups: List<PartnerGroup>,
    onTryAgain: () -> Unit,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.random_partner_result_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(groups, key = { index, _ -> index }) { index, group ->
                GroupCard(
                    group = group,
                    modifier = Modifier.testTag("$TAG_RESULT_GROUP_PREFIX$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier
                    .weight(1f)
                    .testTag(TAG_RESTART_BUTTON)
            ) {
                Text(stringResource(R.string.random_partner_restart))
            }
            Button(
                onClick = onTryAgain,
                modifier = Modifier
                    .weight(1f)
                    .testTag(TAG_TRY_AGAIN_BUTTON)
            ) {
                Text(stringResource(R.string.random_partner_try_again))
            }
        }
    }
}

@Composable
fun GroupCard(group: PartnerGroup, modifier: Modifier = Modifier) {
    val isTrio = group.members.size == 3
    val containerColor = when {
        group.toleranceExceeded -> MaterialTheme.colorScheme.errorContainer
        isTrio -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val onContainerColor = when {
        group.toleranceExceeded -> MaterialTheme.colorScheme.onErrorContainer
        isTrio -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    group.members.forEachIndexed { index, participant ->
                        Text(
                            text = participant.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = onContainerColor
                        )
                        if (index < group.members.size - 1) {
                            Text(
                                text = "×",
                                style = MaterialTheme.typography.bodySmall,
                                color = onContainerColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(
                            R.string.random_partner_weight_delta,
                            group.weightDeltaKg.roundToInt()
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = onContainerColor
                    )
                    if (group.toleranceExceeded) {
                        Text(
                            text = stringResource(R.string.random_partner_tolerance_exceeded),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (isTrio) {
                        Text(
                            text = stringResource(R.string.random_partner_trio_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = onContainerColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
