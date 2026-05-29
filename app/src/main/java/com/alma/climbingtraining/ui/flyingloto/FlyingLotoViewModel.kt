package com.alma.climbingtraining.ui.flyingloto

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alma.climbingtraining.data.PlayerNamesRepository
import com.alma.climbingtraining.data.PlayerPreferences
import com.alma.climbingtraining.model.Player
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GamePhase {
    PLAYER_ENTRY,
    CONFIGURATION,
    PLAYING
}

data class FlyingLotoState(
    val playerNames: List<String> = emptyList(),
    val players: List<Player> = emptyList(),
    val phase: GamePhase = GamePhase.PLAYER_ENTRY,
    val currentNumber: Int? = null,
    val currentPlayerName: String? = null
)

class FlyingLotoViewModel(
    application: Application,
    private val repository: PlayerNamesRepository = PlayerPreferences(application),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(FlyingLotoState())
    val state: StateFlow<FlyingLotoState> = _state.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            val savedNames = repository.loadPlayerNames()
            _state.update { it.copy(playerNames = savedNames) }
        }
    }

    fun addPlayer(name: String) {
        if (name.isBlank()) return
        val trimmed = name.trim()
        var added = false
        _state.update { s ->
            if (s.playerNames.size >= 30 || s.playerNames.contains(trimmed)) s
            else { added = true; s.copy(playerNames = s.playerNames + trimmed) }
        }
        if (added) persistNames(_state.value.playerNames)
    }

    fun removePlayer(name: String) {
        var removed = false
        _state.update { s ->
            val updated = s.playerNames - name
            if (updated.size != s.playerNames.size) { removed = true; s.copy(playerNames = updated) }
            else s
        }
        if (removed) persistNames(_state.value.playerNames)
    }

    private fun persistNames(names: List<String>) {
        viewModelScope.launch(ioDispatcher) {
            repository.savePlayerNames(names)
        }
    }

    fun validate() {
        val names = _state.value.playerNames
        if (names.isEmpty()) return

        val shuffledNumbers = (1..30).shuffled()
        val players = names.mapIndexed { index, name ->
            Player(name = name, assignedNumber = shuffledNumbers[index])
        }

        _state.update {
            it.copy(
                players = players,
                phase = GamePhase.CONFIGURATION
            )
        }
    }

    fun startGame() {
        _state.update {
            it.copy(phase = GamePhase.PLAYING, currentNumber = null, currentPlayerName = null)
        }
    }

    fun nextNumber() {
        val randomNumber = (1..30).random()
        val matchedPlayer = _state.value.players.find { it.assignedNumber == randomNumber }

        _state.update {
            it.copy(
                currentNumber = randomNumber,
                currentPlayerName = matchedPlayer?.name
            )
        }
    }

    fun stopGame() {
        _state.update {
            it.copy(
                phase = GamePhase.PLAYER_ENTRY,
                players = emptyList(),
                currentNumber = null,
                currentPlayerName = null
            )
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FlyingLotoViewModel(application) as T
        }
    }
}
