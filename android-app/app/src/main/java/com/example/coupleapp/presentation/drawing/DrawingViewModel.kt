package com.example.coupleapp.presentation.drawing

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import com.example.coupleapp.data.remote.DrawPoint
import com.example.coupleapp.data.remote.SocketManager
import com.example.coupleapp.domain.StrokeRepository
import com.example.coupleapp.presentation.widget.DrawingWidgetProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
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

    private val _isLoadingHistory = MutableStateFlow(false)
    val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory.asStateFlow()

    val isOnline: StateFlow<Boolean> = socketManager.isOnline

    private var currentRoomId: String? = null
    private var isInitialized = false

    fun init(roomId: String) {
        if (isInitialized) return
        isInitialized = true
        currentRoomId = roomId

        _isLoadingHistory.value = true

        viewModelScope.launch {
            repository.loadHistory(roomId).fold(
                onSuccess = { history ->
                    _strokes.value = history
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to load history", exception)
                }
            )
            _isLoadingHistory.value = false
        }

        socketManager.setOnDrawListener { remoteStroke ->
            onRemoteStroke(remoteStroke)
        }
        socketManager.setOnUndoListener {
            onRemoteUndo()
        }
        socketManager.setOnClearListener {
            onRemoteClear()
        }
        socketManager.connect(roomId)
    }

    fun reset() {
        isInitialized = false
        _strokes.value = emptyList()
        currentRoomId = null
    }

    fun onStrokeFinished(stroke: List<DrawPoint>) {
        _strokes.update { currentStrokes ->
            currentStrokes.toMutableList().apply { add(stroke) }
        }
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
        _strokes.update { currentStrokes ->
            currentStrokes.toMutableList().apply { add(stroke) }
        }
    }

    fun undoLastStroke() {
        val currentStrokes = _strokes.value
        if (currentStrokes.isNotEmpty()) {
            _strokes.update {
                it.toMutableList().apply {
                    val removed = removeAt(lastIndex)
                }
            }
            currentRoomId?.let { roomId ->
                socketManager.emitUndo(roomId)
                viewModelScope.launch {
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
        }
    }

    fun exportToPng(width: Int, height: Int): ByteArray? {
        if (width <= 0 || height <= 0) return null

        val bitmap = ImageBitmap(width, height, ImageBitmapConfig.Argb8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply {
            color = Color.White
            style = PaintingStyle.Fill
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val strokes = _strokes.value
        for (stroke in strokes) {
            if (stroke.size <= 1) continue

            val path = Path()
            path.moveTo(stroke[0].x, stroke[0].y)
            for (i in 1 until stroke.size) {
                path.lineTo(stroke[i].x, stroke[i].y)
            }

            val paint = Paint().apply {
                color = Color(stroke[0].color)
                style = PaintingStyle.Stroke
                strokeWidth = stroke[0].strokeWidth
                strokeCap = StrokeCap.Round
                strokeJoin = StrokeJoin.Round
            }
            canvas.drawPath(path, paint)
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    fun saveLastDrawing(context: Context): Boolean {
        val metrics = context.resources.displayMetrics
        val pngBytes = exportToPng(metrics.widthPixels, metrics.heightPixels)

        return try {
            val file = File(context.filesDir, DrawingWidgetProvider.LAST_DRAWING_FILE)
            file.writeBytes(pngBytes!!)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun onRemoteUndo() {
        _strokes.update { currentStrokes ->
            if (currentStrokes.isNotEmpty()) {
                currentStrokes.toMutableList().apply {
                    removeAt(lastIndex)
                }
            } else {
                currentStrokes
            }
        }
    }

    fun clearAll() {
        _strokes.value = emptyList()
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
        _strokes.value = emptyList()
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
