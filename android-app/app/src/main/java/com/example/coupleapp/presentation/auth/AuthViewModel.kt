package com.example.coupleapp.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coupleapp.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val userId: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.login(email, password).fold(
                onSuccess = { userId ->
                    Log.d("AuthVM_Debug", "Login success, userId from repo: $userId")
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, userId = userId) }
                    Log.d("AuthVM_Debug", "State updated. New state: isSuccess=true, userId=$userId")

                },
                onFailure = { exception ->
                    Log.e("AuthVM_Debug", "Login FAILED", exception) // <-- добавь это
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Unknown network error"
                        )
                    }
                }
            )
        }
    }

    fun register(email: String, password: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.register(email, password).fold(
                onSuccess = { userId ->
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, userId = userId) }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Unknown network error"
                        )
                    }
                }
            )
        }
    }
}