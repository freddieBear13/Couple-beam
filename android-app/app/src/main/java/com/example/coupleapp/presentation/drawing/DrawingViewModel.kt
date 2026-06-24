package com.example.coupleapp.presentation.drawing

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

    private var currentRoomId: String? = null

    fun init(roomId: String) {
        currentRoomId = roomId
        socketManager.setOnDrawListener { remoteStroke ->
            onRemoteStroke(remoteStroke)
        }
        socketManager.connect(roomId)
    }

    fun onStrokeFinished(stroke: List<DrawPoint>) {
        _strokes.update { currentStrokes ->
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

    override fun onCleared() {
        super.onCleared()
        socketManager.disconnect()
    }
}
