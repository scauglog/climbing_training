package com.alma.climbingtraining.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alma.climbingtraining.data.ExerciseRepository
import com.alma.climbingtraining.data.WarmupExerciseDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryState(
    val hasCustomExercises: Boolean = false,
    val hasCustomWarmup: Boolean = false,
    /** Non-null while an exercises export JSON is ready to be written to a file chosen by the user. */
    val pendingExercisesExportJson: String? = null,
    /** Non-null while a warmup export JSON is ready to be written to a file chosen by the user. */
    val pendingWarmupExportJson: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class SettingsViewModel(
    application: Application,
    private val exerciseRepository: ExerciseRepository = ExerciseRepository(application),
    private val warmupDataSource: WarmupExerciseDataSource = WarmupExerciseDataSource(application),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    private val _libraryState = MutableStateFlow(LibraryState())
    val libraryState: StateFlow<LibraryState> = _libraryState.asStateFlow()

    init {
        refreshLibraryStatus()
    }

    fun refreshLibraryStatus() {
        viewModelScope.launch(ioDispatcher) {
            _libraryState.update {
                it.copy(
                    hasCustomExercises = exerciseRepository.hasCustomLibrary(),
                    hasCustomWarmup = warmupDataSource.hasCustomLibrary()
                )
            }
        }
    }

    // ── Exercise library ──────────────────────────────────────────────────────

    fun importExercises(json: String) {
        viewModelScope.launch(ioDispatcher) {
            val error = exerciseRepository.importCustomLibrary(json)
            if (error != null) {
                _libraryState.update { it.copy(errorMessage = error) }
            } else {
                _libraryState.update { it.copy(hasCustomExercises = true, successMessage = "Exercise library imported.") }
            }
        }
    }

    fun clearExercises() {
        viewModelScope.launch(ioDispatcher) {
            exerciseRepository.clearCustomLibrary()
            _libraryState.update { it.copy(hasCustomExercises = false, successMessage = "Exercise library reset to built-in.") }
        }
    }

    fun prepareExercisesExport() {
        viewModelScope.launch(ioDispatcher) {
            val json = exerciseRepository.exportCurrentLibraryAsJson()
            _libraryState.update { it.copy(pendingExercisesExportJson = json) }
        }
    }

    // ── Warmup library ────────────────────────────────────────────────────────

    fun importWarmup(json: String) {
        viewModelScope.launch(ioDispatcher) {
            val error = warmupDataSource.importCustomLibrary(json)
            if (error != null) {
                _libraryState.update { it.copy(errorMessage = error) }
            } else {
                _libraryState.update { it.copy(hasCustomWarmup = true, successMessage = "Warmup library imported.") }
            }
        }
    }

    fun clearWarmup() {
        viewModelScope.launch(ioDispatcher) {
            warmupDataSource.clearCustomLibrary()
            _libraryState.update { it.copy(hasCustomWarmup = false, successMessage = "Warmup library reset to built-in.") }
        }
    }

    fun prepareWarmupExport() {
        viewModelScope.launch(ioDispatcher) {
            val json = warmupDataSource.exportCurrentLibraryAsJson()
            _libraryState.update { it.copy(pendingWarmupExportJson = json) }
        }
    }

    // ── Feedback dismissal ────────────────────────────────────────────────────

    fun clearPendingExercisesExport() {
        _libraryState.update { it.copy(pendingExercisesExportJson = null) }
    }

    fun clearPendingWarmupExport() {
        _libraryState.update { it.copy(pendingWarmupExportJson = null) }
    }

    fun clearError() {
        _libraryState.update { it.copy(errorMessage = null) }
    }

    fun setImportError(message: String) {
        _libraryState.update { it.copy(errorMessage = message) }
    }

    fun clearSuccess() {
        _libraryState.update { it.copy(successMessage = null) }
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(application) as T
    }
}
