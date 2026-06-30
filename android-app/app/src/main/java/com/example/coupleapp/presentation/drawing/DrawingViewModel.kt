package com.example.coupleapp.presentation.drawing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coupleapp.data.remote.DrawPoint
import com.example.coupleapp.data.remote.SocketManager
import com.example.coupleapp.domain.StrokeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrawingViewModel @Inject constructor(
    private val socketManager: SocketManager,
    private val repository: StrokeRepository
) : ViewModel() {

    companion object {
        private const val TAG = "DrawingViewModel"
    }

    private val _strokes = MutableStateFlow<List<List<DrawPoint>>>(emptyList())
    val strokes: StateFlow<List<List<DrawPoint>>> = _strokes.asStateFlow()

    private val _currentColor = MutableStateFlow(0xFF000000.toInt())
    val currentColor: StateFlow<Int> = _currentColor.asStateFlow()

    private val _currentStrokeWidth = MutableStateFlow(8f)
    val currentStrokeWidth: StateFlow<Float> = _currentStrokeWidth.asStateFlow()

    private var currentRoomId: String? = null

    private fun logState(event: String) {
        val current = _strokes.value
        val lastStrokePoints = current.lastOrNull()?.size ?: 0
        Log.d(TAG, "[$event] strokes.size=${current.size}, lastStroke.points=$lastStrokePoints")
    }

    fun init(roomId: String) {
        currentRoomId = roomId
        Log.d(TAG, "init() roomId=$roomId")
        viewModelScope.launch {
            repository.loadHistory(roomId).fold(
                onSuccess = { history ->
                    _strokes.value = history
                    Log.d(TAG, "History loaded: ${history.size} strokes")
                    logState("afterLoad")
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to load history", exception)
                }
            )
        }

        socketManager.setOnDrawListener { remoteStroke ->
            Log.d(TAG, "WebSocket DRAW received, points=${remoteStroke.size}")
            onRemoteStroke(remoteStroke)
        }
        socketManager.setOnUndoListener {
            Log.d(TAG, "WebSocket UNDO received")
            onRemoteUndo()
        }
        socketManager.setOnClearListener {
            Log.d(TAG, "WebSocket CLEAR received")
            onRemoteClear()
        }
        socketManager.connect(roomId)
    }

    fun onStrokeFinished(stroke: List<DrawPoint>) {
        Log.d(TAG, "onStrokeFinished() points=${stroke.size}, color=${stroke.firstOrNull()?.color}")
        _strokes.update { currentStrokes ->
            currentStrokes.toMutableList().apply { add(stroke) }
        }
        logState("afterLocalAdd")
        currentRoomId?.let { roomId ->
            socketManager.emitDraw(roomId, stroke)

            viewModelScope.launch {
                repository.saveStroke(roomId, stroke).fold(
                    onSuccess = {
                        Log.d(TAG, "Stroke saved to server")
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to save stroke", exception)
                    }
                )
            }
        }
    }

    fun onRemoteStroke(stroke: List<DrawPoint>) {
        Log.d(TAG, "onRemoteStroke() points=${stroke.size}")
        _strokes.update { currentStrokes ->
            currentStrokes.toMutableList().apply { add(stroke) }
        }
        logState("afterRemoteAdd")
    }

    fun undoLastStroke() {
        Log.d(TAG, "undoLastStroke() CALLED")
        val before = _strokes.value.size
        Log.d(TAG, "Before undo: strokes.size=$before")

        val currentStrokes = _strokes.value
        if (currentStrokes.isNotEmpty()) {
            _strokes.update {
                it.toMutableList().apply {
                    val removed = removeAt(lastIndex)
                    Log.d(TAG, "Removed stroke with ${removed.size} points")
                }
            }
            logState("afterLocalUndo")

            currentRoomId?.let { roomId ->
                Log.d(TAG, "Emitting UNDO to WebSocket")
                socketManager.emitUndo(roomId)

                viewModelScope.launch {
                    Log.d(TAG, "Sending DELETE /strokes/last")
                    repository.deleteLastStroke(roomId).fold(
                        onSuccess = {
                            Log.d(TAG, "Server confirmed deletion")
                        },
                        onFailure = { exception ->
                            Log.e(TAG, "Server delete failed", exception)
                        }
                    )
                }
            }
        } else {
            Log.d(TAG, "undoLastStroke() skipped - strokes is empty")
        }
    }

    fun onRemoteUndo() {
        Log.d(TAG, "onRemoteUndo() CALLED")
        val before = _strokes.value.size
        _strokes.update { currentStrokes ->
            if (currentStrokes.isNotEmpty()) {
                currentStrokes.toMutableList().apply {
                    removeAt(lastIndex)
                    Log.d(TAG, "Remote undo removed stroke, new size=$size")
                }
            } else {
                Log.d(TAG, "Remote undo skipped - strokes is empty")
                currentStrokes
            }
        }
        logState("afterRemoteUndo (before=$before)")
    }

    fun clearAll() {
        Log.d(TAG, "clearAll() CALLED")
        _strokes.value = emptyList()
        logState("afterLocalClear")

        currentRoomId?.let { roomId ->
            socketManager.emitClear(roomId)

            viewModelScope.launch {
                repository.deleteAllStrokes(roomId).fold(
                    onSuccess = {
                        Log.d(TAG, "Server confirmed clear")
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Server clear failed", exception)
                    }
                )
            }
        }
    }

    fun onRemoteClear() {
        Log.d(TAG, "onRemoteClear() CALLED")
        _strokes.value = emptyList()
        logState("afterRemoteClear")
    }

    fun updateColor(color: Int) {
        _currentColor.value = color
    }

    fun updateStrokeWidth(width: Float) {
        _currentStrokeWidth.value = width
    }

    fun exitRoom() {
        socketManager.disconnect()
    }
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared() - disconnecting socket")
        socketManager.disconnect()
    }
}
