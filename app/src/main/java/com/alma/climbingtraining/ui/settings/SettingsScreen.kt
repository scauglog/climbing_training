package com.alma.climbingtraining.ui.settings

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alma.climbingtraining.R
import com.alma.climbingtraining.model.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val currentLocale = AppCompatDelegate.getApplicationLocales()[0]
    val initialLanguage = if (currentLocale != null) {
        AppLanguage.fromTag(currentLocale.language)
    } else {
        AppLanguage.ENGLISH
    }
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }

    val libraryState by viewModel.libraryState.collectAsState()
    val context = LocalContext.current

    // ── File import launchers ─────────────────────────────────────────────────

    val exerciseImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val json = runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
        }.getOrNull()
        if (json != null) {
            viewModel.importExercises(json)
        } else {
            viewModel.setImportError(context.getString(R.string.settings_import_read_error))
        }
    }

    val warmupImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val json = runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
        }.getOrNull()
        if (json != null) {
            viewModel.importWarmup(json)
        } else {
            viewModel.setImportError(context.getString(R.string.settings_import_read_error))
        }
    }

    // ── File export launchers ─────────────────────────────────────────────────

    val exerciseExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: run { viewModel.clearPendingExercisesExport(); return@rememberLauncherForActivityResult }
        libraryState.pendingExercisesExportJson?.let { json ->
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
            }
        }
        viewModel.clearPendingExercisesExport()
    }

    val warmupExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: run { viewModel.clearPendingWarmupExport(); return@rememberLauncherForActivityResult }
        libraryState.pendingWarmupExportJson?.let { json ->
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
            }
        }
        viewModel.clearPendingWarmupExport()
    }

    // Each launcher watches only its own dedicated state field — no shared slot, no routing by filename.
    LaunchedEffect(libraryState.pendingExercisesExportJson) {
        if (libraryState.pendingExercisesExportJson != null) {
            exerciseExportLauncher.launch("exercises.json")
        }
    }
    LaunchedEffect(libraryState.pendingWarmupExportJson) {
        if (libraryState.pendingWarmupExportJson != null) {
            warmupExportLauncher.launch("warmup.json")
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    libraryState.errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text(stringResource(R.string.settings_import_error_title)) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text(stringResource(R.string.settings_dialog_ok))
                }
            }
        )
    }

    libraryState.successMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::clearSuccess,
            title = { Text(stringResource(R.string.settings_import_success_title)) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = viewModel::clearSuccess) {
                    Text(stringResource(R.string.settings_dialog_ok))
                }
            }
        )
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Language section ──────────────────────────────────────────────
            SectionTitle(stringResource(R.string.settings_language_label))

            Column(modifier = Modifier.selectableGroup()) {
                AppLanguage.entries.forEach { language ->
                    val label = when (language) {
                        AppLanguage.ENGLISH -> stringResource(R.string.settings_language_english)
                        AppLanguage.FRENCH -> stringResource(R.string.settings_language_french)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedLanguage == language,
                                onClick = {
                                    if (selectedLanguage != language) {
                                        selectedLanguage = language
                                        AppCompatDelegate.setApplicationLocales(
                                            LocaleListCompat.forLanguageTags(language.tag)
                                        )
                                    }
                                },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(selected = selectedLanguage == language, onClick = null)
                        Text(text = label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Divider()

            // ── Exercise library section ──────────────────────────────────────
            SectionTitle(stringResource(R.string.settings_exercise_library_title))

            LibraryStatusBadge(
                isCustom = libraryState.hasCustomExercises,
                customLabel = stringResource(R.string.settings_library_custom),
                builtInLabel = stringResource(R.string.settings_library_builtin)
            )

            LibraryActionRow(
                onExport = viewModel::prepareExercisesExport,
                onImport = { exerciseImportLauncher.launch(arrayOf("application/json", "text/plain")) },
                onClear = viewModel::clearExercises,
                hasCustom = libraryState.hasCustomExercises
            )

            Divider()

            // ── Warmup library section ────────────────────────────────────────
            SectionTitle(stringResource(R.string.settings_warmup_library_title))

            LibraryStatusBadge(
                isCustom = libraryState.hasCustomWarmup,
                customLabel = stringResource(R.string.settings_library_custom),
                builtInLabel = stringResource(R.string.settings_library_builtin)
            )

            LibraryActionRow(
                onExport = viewModel::prepareWarmupExport,
                onImport = { warmupImportLauncher.launch(arrayOf("application/json", "text/plain")) },
                onClear = viewModel::clearWarmup,
                hasCustom = libraryState.hasCustomWarmup
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun LibraryStatusBadge(isCustom: Boolean, customLabel: String, builtInLabel: String) {
    val (label, color) = if (isCustom) {
        customLabel to MaterialTheme.colorScheme.tertiary
    } else {
        builtInLabel to MaterialTheme.colorScheme.outline
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun LibraryActionRow(
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClear: () -> Unit,
    hasCustom: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(onClick = onExport, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.settings_library_export))
        }
        OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.settings_library_import))
        }
        if (hasCustom) {
            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.settings_library_reset))
            }
        }
    }
}
