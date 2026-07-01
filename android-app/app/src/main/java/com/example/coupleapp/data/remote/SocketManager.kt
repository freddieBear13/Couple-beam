package com.example.coupleapp.data.remote

import android.util.Log
import androidx.compose.runtime.currentRecomposeScope
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

sealed class QueuedEvent {
    data class Draw(val roomId: String, val points: List<DrawPoint>) : QueuedEvent()
    data object Undo : QueuedEvent()
    data object Clear : QueuedEvent()
}

@Singleton
class SocketManager @Inject constructor() {
    companion object {
        private const val TAG = "SocketManager"
    }
    private var socket: Socket? = null
    private var isDisconnected = false
    private var currentRoomId : String? = null

    private val eventQueue = mutableListOf<QueuedEvent>()

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var onDrawListener: ((List<DrawPoint>) -> Unit)? = null
    private var onUndoListener: (() -> Unit)? = null
    private var onClearListener: (() -> Unit)? = null

    fun setOnDrawListener(listener: (List<DrawPoint>) -> Unit) {
        this.onDrawListener = listener
    }

    fun setOnUndoListener(listener: () -> Unit) {
        this.onUndoListener = listener
    }

    fun setOnClearListener(listener: () -> Unit) {
        this.onClearListener = listener
    }

    fun connect(roomId: String) {
        if (socket?.connected() == true) return
        isDisconnected = false
        currentRoomId = roomId
        Log.d(TAG, "connect() roomId=$roomId")

        val options = IO.Options().apply {
            reconnection = true
            reconnectionAttempts = Int.MAX_VALUE
            reconnectionDelay = 1000
            reconnectionDelayMax = 5000
        }

        try {
            socket = IO.socket("http://192.168.10.9:3000", options)
            socket?.connect()

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "EVENT_CONNECT")
                _isOnline.value = true
                socket?.emit("joinRoom", roomId)
                flushQueue()
            }

            socket?.on("draw") { args ->
                Log.d(TAG, "EVENT draw received, args.size=${args.size}")
                if (args.isNotEmpty()) {
                    val jsonArray = args[0] as JSONArray
                    Log.d(TAG, "draw payload length=${jsonArray.length()}")
                    val points = mutableListOf<DrawPoint>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)

                        val colorValue = obj.opt("color")
                        val color = when (colorValue) {
                            is Number -> colorValue.toInt()
                            is String -> colorValue.toIntOrNull() ?: 0xFF000000.toInt()
                            else -> {
                                Log.w("SocketManager", "Unknown color type: ${colorValue?.javaClass}")
                                0xFF000000.toInt()
                            }
                        }

                        val widthValue = obj.opt("strokeWidth")
                        val strokeWidth = when (widthValue) {
                            is Number -> widthValue.toFloat()
                            is String -> widthValue.toFloatOrNull() ?: 8f
                            else -> {
                                Log.w("SocketManager", "Unknown width type: ${widthValue?.javaClass}")
                                8f
                            }
                        }

                        Log.d("SocketManager", "Parsed point: color=$color, width=$strokeWidth")

                        points.add(
                            DrawPoint(
                                x = obj.getDouble("x").toFloat(),
                                y = obj.getDouble("y").toFloat(),
                                color = color,
                                strokeWidth = strokeWidth
                            )
                        )
                    }
                    Log.d(TAG, "Parsed ${points.size} points, invoking listener")
                    onDrawListener?.invoke(points)
                }
            }

            socket?.on("undo") { args ->
                Log.d(TAG, "EVENT undo received, args.size=${args.size}")
                onUndoListener?.invoke()
            }

            socket?.on("clear") {args ->
                Log.d(TAG, "EVENT clear received, args.size=${args.size}")
                onClearListener?.invoke()
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "EVENT_DISCONNECT")
                _isOnline.value = false
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.w(TAG, "EVENT_CONNECT_ERROR: ${args.firstOrNull()}")
                _isOnline.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Socket connection error", e)
        }
    }

    private fun flushQueue() {
        if (eventQueue.isEmpty()) return
        Log.d(TAG, "flushQueue() sending ${eventQueue.size} queued events")

        val eventsToSend = eventQueue.toList()
        eventQueue.clear()

        for (event in eventsToSend) {
            when (event) {
                is QueuedEvent.Draw -> sendDrawInterval(event.roomId, event.points)
                is QueuedEvent.Undo -> sendUndoInterval()
                is QueuedEvent.Clear -> sendClearInterval()
            }
        }
    }

    fun emitDraw(roomId: String, points: List<DrawPoint>) {
        Log.d(TAG, "emitDraw() roomId=$roomId, points=${points.size}")
        if (socket?.connected() != true) {
            Log.w(TAG, "emitDraw() skipped - socket not connected")
            eventQueue.add(QueuedEvent.Draw(roomId, points))
            return
        }
        sendDrawInterval(roomId, points)
    }

    private fun sendDrawInterval(roomId: String, points: List<DrawPoint>) {
        val jsonArray = JSONArray()
        for (point in points) {
            val obj = JSONObject().apply {
                put("x", point.x.toDouble())
                put("y", point.y.toDouble())
                put("color", point.color.toLong())
                put("strokeWidth", point.strokeWidth.toDouble())
            }
            jsonArray.put(obj)
        }

        val payload = JSONObject().apply {
            put("roomId", roomId)
            put("points", jsonArray)
        }

        socket?.emit("draw", payload)
        Log.d(TAG, "emitDraw() sent successfully")
    }

    fun emitUndo(roomId: String) {
        Log.d(TAG, "emitUndo() roomId=$roomId")
        if (socket?.connected() != true) {
            Log.w(TAG, "emitUndo() skipped - socket not connected")
            eventQueue.add(QueuedEvent.Undo)
            return
        }
        sendUndoInterval()
    }

    private fun sendUndoInterval() {
        val roomId = currentRoomId ?: return
        val payload = JSONObject().apply {
            put("roomId", roomId)
        }
        socket?.emit("undo", payload)
        Log.d(TAG, "emitUndo() sent successfully")
    }

    fun emitClear(roomId: String) {
        Log.d(TAG, "emitClear() roomId=$roomId")
        if (socket?.connected() != true) {
            Log.w(TAG, "emitClear() skipped - socket not connected")
            eventQueue.add(QueuedEvent.Clear)
            return
        }
        sendClearInterval()
    }

    private fun sendClearInterval() {
        val roomId = currentRoomId
        val payload = JSONObject().apply {
            put("roomId", roomId)
        }
        socket?.emit("clear", payload)
        Log.d(TAG, "emitClear() sent successfully")
    }
    
    fun disconnect() {
        if (isDisconnected) {
            Log.d(TAG, "disconnect() skipped - already disconnected")
            return
        }
        isDisconnected = true
        Log.d(TAG, "disconnect()")
        _isOnline.value = false
        eventQueue.clear()
        socket?.disconnect()
        socket = null
    }
}