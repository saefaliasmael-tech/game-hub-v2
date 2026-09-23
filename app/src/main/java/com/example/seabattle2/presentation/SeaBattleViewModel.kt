package com.example.seabattle2.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seabattle2.engine.SeaBattleEngine
import com.example.seabattle2.model.BattlePhase
import com.example.seabattle2.model.SeaBattleState
import com.example.seabattle2.repository.SeaBattleRepository
import com.example.watersort.core.audio.GameSound
import com.example.watersort.core.audio.HapticManager
import com.example.watersort.core.audio.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SeaBattleViewModel(
    private val repository: SeaBattleRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {
    private val engine = SeaBattleEngine()

    private val _state = MutableStateFlow(engine.initGame())
    val state: StateFlow<SeaBattleState> = _state.asStateFlow()

    fun startBattle() {
        _state.value = _state.value.copy(phase = BattlePhase.PLAYER_TURN)
        soundManager.play(GameSound.BUTTON)
    }

    fun randomizePlacement() {
        _state.value = engine.initGame()
        soundManager.play(GameSound.SELECT)
    }

    fun fireAtEnemy(x: Int, y: Int) {
        val current = _state.value
        if (current.phase != BattlePhase.PLAYER_TURN) return

        val next = engine.playerFire(
            state = current,
            x = x,
            y = y,
            onHit = {
                soundManager.play(GameSound.HIT)
                hapticManager.medium()
            },
            onMiss = {
                soundManager.play(GameSound.INVALID)
                hapticManager.tap()
            },
            onShipSunk = {
                soundManager.play(GameSound.ACHIEVEMENT)
                hapticManager.strong()
            }
        )

        _state.value = next

        if (next.phase == BattlePhase.GAME_OVER) {
            if (next.isPlayerWinner) {
                soundManager.play(GameSound.WIN)
                hapticManager.success()
                repository.recordGame(true)
            }
            return
        }

        // Trigger AI turn after brief suspense delay
        if (next.phase == BattlePhase.AI_TURN) {
            viewModelScope.launch {
                delay(700L)
                executeAiTurn()
            }
        }
    }

    private fun executeAiTurn() {
        val current = _state.value
        if (current.phase != BattlePhase.AI_TURN) return

        val next = engine.aiFire(
            state = current,
            onHit = {
                soundManager.play(GameSound.HIT)
                hapticManager.medium()
            },
            onMiss = {
                soundManager.play(GameSound.SELECT)
            },
            onShipSunk = {
                soundManager.play(GameSound.GAME_OVER)
                hapticManager.strong()
            }
        )

        _state.value = next

        if (next.phase == BattlePhase.GAME_OVER && !next.isPlayerWinner) {
            soundManager.play(GameSound.GAME_OVER)
            hapticManager.error()
            repository.recordGame(false)
        }
    }

    fun restart() {
        _state.value = engine.initGame()
        soundManager.play(GameSound.BUTTON)
    }
}
