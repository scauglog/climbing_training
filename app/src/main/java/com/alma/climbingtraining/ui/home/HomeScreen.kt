package com.alma.climbingtraining.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alma.climbingtraining.R

data class Tool(
    val nameRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToFlyingLoto: () -> Unit,
    onNavigateToRandomExercise: () -> Unit,
    onNavigateToWarmup: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val tools = remember {
        listOf(
            Tool(
                nameRes = R.string.tool_flying_loto_name,
                descriptionRes = R.string.tool_flying_loto_description,
                icon = Icons.Default.Casino
            ),
            Tool(
                nameRes = R.string.tool_random_exercise_name,
                descriptionRes = R.string.tool_random_exercise_description,
                icon = Icons.Default.FitnessCenter
            ),
            Tool(
                nameRes = R.string.tool_warmup_name,
                descriptionRes = R.string.tool_warmup_description,
                icon = Icons.Default.Timer
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.home_settings_content_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tools, key = { it.nameRes }) { tool ->
                ToolCard(
                    tool = tool,
                    modifier = when (tool.nameRes) {
                        R.string.tool_flying_loto_name -> Modifier.testTag(TAG_TOOL_CARD_FLYING_LOTO)
                        R.string.tool_random_exercise_name -> Modifier.testTag(TAG_TOOL_CARD_RANDOM_EXERCISE)
                        R.string.tool_warmup_name -> Modifier.testTag(TAG_TOOL_CARD_WARMUP)
                        else -> Modifier
                    },
                    onClick = {
                        when (tool.nameRes) {
                            R.string.tool_flying_loto_name -> onNavigateToFlyingLoto()
                            R.string.tool_random_exercise_name -> onNavigateToRandomExercise()
                            R.string.tool_warmup_name -> onNavigateToWarmup()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ToolCard(
    tool: Tool,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = stringResource(tool.nameRes),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(tool.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
