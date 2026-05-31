package com.alma.climbingtraining.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.alma.climbingtraining.R
import com.alma.climbingtraining.model.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    // Derive the current language from the AppCompat-managed locale list.
    // Falls back to ENGLISH if no per-app locale has been set yet.
    val currentLocale = AppCompatDelegate.getApplicationLocales()[0]
    val initialLanguage = if (currentLocale != null) {
        AppLanguage.fromTag(currentLocale.language)
    } else {
        AppLanguage.ENGLISH
    }
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_language_label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

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
                                        // AppCompat persists the locale and recreates the Activity.
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
                        RadioButton(
                            selected = selectedLanguage == language,
                            onClick = null
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
