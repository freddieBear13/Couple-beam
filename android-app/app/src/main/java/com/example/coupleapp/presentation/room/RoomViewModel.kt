package com.example.coupleapp.presentation.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coupleapp.domain.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val generatedCode: String? = null,
    val isJoined: Boolean = false,
    val roomId: String? = null,
    val isPartnerJoined: Boolean = false,
    val hasActiveRoom: Boolean = false,
    val activeRoomCode: String? = null
)

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val repository: RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomState())
    val uiState: StateFlow<RoomState> = _uiState.asStateFlow()

    fun createRoom(userId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.createRoom(userId).fold(
                onSuccess = { code ->
                    _uiState.update { it.copy(isLoading = false, generatedCode = code, roomId = code) }
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message ?: "Network error")
                    }
                }
            )
        }
    }

    fun joinRoom(inviteCode: String, userId: String) {
        if (inviteCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter invitation code") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.joinRoom(inviteCode, userId).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isJoined = true, roomId = inviteCode) }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Network error")
                    }
                }
            )
        }
    }

    fun checkActiveRoom(userId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.getActiveRoom(userId).fold(
                onSuccess = { response ->
                    if (response.hasRoom && response.code != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                hasActiveRoom = true,
                                activeRoomCode = response.code,
                                roomId = response.code,
                                isJoined = true
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Network error")
                    }
                }
            )
        }
    }

    fun clearActiveRoom() {
        _uiState.update {
            it.copy(
                hasActiveRoom = false,
                activeRoomCode = null,
                roomId = null,
                isJoined = true,
                generatedCode = null
            )
        }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }
}