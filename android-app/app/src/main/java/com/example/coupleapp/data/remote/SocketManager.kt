package com.example.coupleapp.data.remote

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor() {
    companion object {
        private const val TAG = "SocketManager"
    }
    private var socket: Socket? = null
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
        Log.d(TAG, "connect() roomId=$roomId")

        val options = IO.Options().apply {
            reconnection = true
            reconnectionAttempts = 5
            reconnectionDelay = 1000
        }

        try {
            socket = IO.socket("http://192.168.10.9:3000", options)
            socket?.connect()

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "EVENT_CONNECT")
                socket?.emit("joinRoom", roomId)
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
            }
        } catch (e: Exception) {
            Log.e(TAG, "Socket connection error", e)
        }
    }

    fun emitDraw(roomId: String, points: List<DrawPoint>) {
        Log.d(TAG, "emitDraw() roomId=$roomId, points=${points.size}")
        if (socket?.connected() != true) {
            Log.w(TAG, "emitDraw() skipped - socket not connected")
            return
        }

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
            return
        }
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
            return
        }
        val payload = JSONObject().apply {
            put("roomId", roomId)
        }
        socket?.emit("clear", payload)
        Log.d(TAG, "emitClear() sent successfully")
    }

    fun disconnect() {
        Log.d(TAG, "disconnect()")
        socket?.disconnect()
        socket = null
    }
}