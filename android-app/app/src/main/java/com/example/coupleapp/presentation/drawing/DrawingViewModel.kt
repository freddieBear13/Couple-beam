package com.example.coupleapp.presentation.drawing

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.coupleapp.data.remote.DrawPoint
import com.example.coupleapp.data.remote.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DrawingViewModel @Inject constructor(
    private val socketManager: SocketManager
) : ViewModel() {

    private val _strokes = MutableStateFlow<List<List<DrawPoint>>>(emptyList())
    val strokes: StateFlow<List<List<DrawPoint>>> = _strokes.asStateFlow()

    private val _currentColor = MutableStateFlow(0xFF000000.toInt())
    val currentColor: StateFlow<Int> = _currentColor.asStateFlow()

    private val _currentStrokeWidth = MutableStateFlow(8f)
    val currentStrokeWidth: StateFlow<Float> = _currentStrokeWidth.asStateFlow()

    private var currentRoomId: String? = null

    fun init(roomId: String) {
        currentRoomId = roomId
        socketManager.setOnDrawListener { remoteStroke ->
            onRemoteStroke(remoteStroke)
        }
        socketManager.setOnUndoListener { index ->
            onRemoteUndo(index)
        }
        socketManager.connect(roomId)
    }

    fun onStrokeFinished(stroke: List<DrawPoint>) {
        _strokes.update { currentStrokes ->
            if (stroke.isNotEmpty()) {
                Log.d("DrawingViewModel", "Added stroke: color=${stroke[0].color}, width=${stroke[0].strokeWidth}")
            }
            currentStrokes.toMutableList().apply { add(stroke) }
        }
        currentRoomId?.let { roomId ->
            socketManager.emitDraw(roomId, stroke)
        }
    }

    fun onRemoteStroke(stroke: List<DrawPoint>) {
        _strokes.update { currentStrokes ->
            currentStrokes.toMutableList().apply { add(stroke) }
        }
    }

    fun updateColor(color: Int) {
        _currentColor.value = color
    }

    fun updateStrokeWidth(width: Float) {
        _currentStrokeWidth.value = width
    }

    fun undoLastStroke() {
        val currentStrokes = _strokes.value
        if (currentStrokes.isNotEmpty()) {
            val indexToRemove = currentStrokes.size - 1
            _strokes.update {
                it.toMutableList().apply { removeAt(indexToRemove) }
            }
            currentRoomId?.let { roomId ->
                socketManager.emitUndo(roomId, indexToRemove)
            }
        }
    }

    fun onRemoteUndo(index: Int) {
        _strokes.update { currentStrokes ->
            if (index in currentStrokes.indices) {
                currentStrokes.toMutableList().apply { removeAt(index) }
            } else {
                currentStrokes
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.disconnect()
    }
}
