package com.alma.climbingtraining.ui.randomexercise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alma.climbingtraining.data.ExerciseDataSource
import com.alma.climbingtraining.data.ExerciseRepository
import com.alma.climbingtraining.model.Discipline
import com.alma.climbingtraining.model.EnergySystem
import com.alma.climbingtraining.model.Exercise
import com.alma.climbingtraining.model.ExerciseFilter
import com.alma.climbingtraining.model.ExerciseLevel
import com.alma.climbingtraining.model.TargetAudience
import com.alma.climbingtraining.model.TechniqueFocus
import com.alma.climbingtraining.model.matchesFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class RandomExercisePhase { FILTER, RESULT }

data class RandomExerciseState(
    val phase: RandomExercisePhase = RandomExercisePhase.FILTER,
    val filter: ExerciseFilter = ExerciseFilter(),
    val currentExercise: Exercise? = null,
    val noMatchFound: Boolean = false
)

class RandomExerciseViewModel(
    application: Application,
    private val dataSource: ExerciseDataSource = ExerciseRepository(application)
) : AndroidViewModel(application) {
    private var allExercises: List<Exercise> = emptyList()

    private val _state = MutableStateFlow(RandomExerciseState())
    val state: StateFlow<RandomExerciseState> = _state.asStateFlow()

    init {
        allExercises = dataSource.loadExercises()
    }

    fun toggleTargetAudience(value: TargetAudience) = updateFilter { f ->
        f.copy(targetAudience = f.targetAudience.toggle(value))
    }

    fun toggleEnergySystem(value: EnergySystem) = updateFilter { f ->
        f.copy(energySystems = f.energySystems.toggle(value))
    }

    fun toggleDiscipline(value: Discipline) = updateFilter { f ->
        f.copy(disciplines = f.disciplines.toggle(value))
    }

    fun toggleLevel(value: ExerciseLevel) = updateFilter { f ->
        f.copy(levels = f.levels.toggle(value))
    }

    fun toggleTechniqueFocus(value: TechniqueFocus) = updateFilter { f ->
        f.copy(techniqueFocus = f.techniqueFocus.toggle(value))
    }

    fun drawExercise() {
        val filtered = allExercises.filter { it.matchesFilter(_state.value.filter) }
        if (filtered.isEmpty()) {
            _state.update { it.copy(noMatchFound = true) }
            return
        }
        val picked = filtered.random()
        _state.update { it.copy(currentExercise = picked, phase = RandomExercisePhase.RESULT, noMatchFound = false) }
    }

    fun drawAgain() {
        val filtered = allExercises.filter { it.matchesFilter(_state.value.filter) }
        if (filtered.isEmpty()) {
            _state.update { it.copy(noMatchFound = true, phase = RandomExercisePhase.FILTER) }
            return
        }
        val picked = filtered.random()
        _state.update { it.copy(currentExercise = picked, noMatchFound = false) }
    }

    fun clearNoMatchFound() {
        _state.update { it.copy(noMatchFound = false) }
    }

    fun changeFilters() {
        _state.update { it.copy(phase = RandomExercisePhase.FILTER, currentExercise = null) }
    }

    private fun updateFilter(transform: (ExerciseFilter) -> ExerciseFilter) {
        _state.update { it.copy(filter = transform(it.filter)) }
    }

    private fun <T> Set<T>.toggle(value: T): Set<T> =
        if (contains(value)) this - value else this + value

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RandomExerciseViewModel(application) as T
    }
}
