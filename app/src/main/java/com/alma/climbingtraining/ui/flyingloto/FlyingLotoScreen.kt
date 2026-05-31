package com.alma.climbingtraining.ui.flyingloto

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alma.climbingtraining.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlyingLotoScreen(
    onNavigateBack: () -> Unit,
    viewModel: FlyingLotoViewModel = viewModel(
        factory = FlyingLotoViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.flying_loto_title)) },
                navigationIcon = {
                    if (state.phase != GamePhase.PLAYING) {
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
                GamePhase.PLAYER_ENTRY -> PlayerEntryContent(
                    playerNames = state.playerNames,
                    onAddPlayer = viewModel::addPlayer,
                    onRemovePlayer = viewModel::removePlayer,
                    onValidate = viewModel::validate
                )
                GamePhase.CONFIGURATION -> ConfigurationContent(
                    players = state.players,
                    onStartGame = viewModel::startGame
                )
                GamePhase.PLAYING -> GameContent(
                    currentNumber = state.currentNumber,
                    currentPlayerName = state.currentPlayerName,
                    onNextNumber = viewModel::nextNumber,
                    onStop = viewModel::stopGame
                )
            }
        }
    }
}

@Composable
fun PlayerEntryContent(
    playerNames: List<String>,
    onAddPlayer: (String) -> Unit,
    onRemovePlayer: (String) -> Unit,
    onValidate: () -> Unit
) {
    var inputName by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputName,
                onValueChange = { inputName = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag(TAG_PLAYER_NAME_INPUT),
                label = { Text(stringResource(R.string.flying_loto_player_name_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (inputName.isNotBlank()) {
                            onAddPlayer(inputName)
                            inputName = ""
                        }
                        focusManager.clearFocus()
                    }
                )
            )
            IconButton(
                onClick = {
                    if (inputName.isNotBlank()) {
                        onAddPlayer(inputName)
                        inputName = ""
                    }
                },
                modifier = Modifier.testTag(TAG_ADD_PLAYER_BUTTON)
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = stringResource(R.string.flying_loto_add_player_content_description)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.flying_loto_player_count, playerNames.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(playerNames, key = { it }) { name ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { onRemovePlayer(name) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(
                                    R.string.flying_loto_remove_player_content_description, name
                                ),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onValidate,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TAG_VALIDATE_BUTTON),
            enabled = playerNames.isNotEmpty()
        ) {
            Text(stringResource(R.string.flying_loto_validate))
        }
    }
}

@Composable
fun ConfigurationContent(
    players: List<com.alma.climbingtraining.model.Player>,
    onStartGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.flying_loto_assignments_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(players, key = { _, player -> player.name }) { index, player ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.testTag("$TAG_ASSIGNMENT_NAME_PREFIX$index")
                        )
                        Text(
                            text = "#${player.assignedNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("$TAG_ASSIGNMENT_NUMBER_PREFIX$index")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onStartGame,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TAG_START_GAME_BUTTON),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(stringResource(R.string.flying_loto_start_game), fontSize = 18.sp)
        }
    }
}

@Composable
fun GameContent(
    currentNumber: Int?,
    currentPlayerName: String?,
    onNextNumber: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        AnimatedContent(
            targetState = currentNumber,
            transitionSpec = {
                (scaleIn(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)))
                    .togetherWith(scaleOut(animationSpec = tween(100)) + fadeOut(animationSpec = tween(100)))
            },
            label = "number_animation"
        ) { number ->
            Text(
                text = number?.toString() ?: stringResource(R.string.flying_loto_no_match),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 120.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag(TAG_CURRENT_NUMBER_TEXT),
                color = if (currentPlayerName != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(
            targetState = currentPlayerName,
            transitionSpec = {
                fadeIn(animationSpec = tween(300))
                    .togetherWith(fadeOut(animationSpec = tween(150)))
            },
            label = "name_animation"
        ) { name ->
            Text(
                text = name ?: stringResource(R.string.flying_loto_no_match),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag(TAG_CURRENT_PLAYER_NAME_TEXT),
                color = if (name != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.flying_loto_stop), fontSize = 18.sp)
            }

            Button(
                onClick = onNextNumber,
                modifier = Modifier
                    .weight(1f)
                    .testTag(TAG_NEXT_NUMBER_BUTTON)
            ) {
                Text(stringResource(R.string.flying_loto_next_number), fontSize = 18.sp)
            }
        }
    }
}
