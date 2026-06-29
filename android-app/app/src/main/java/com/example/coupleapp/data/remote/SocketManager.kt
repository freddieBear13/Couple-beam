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
    private var socket: Socket? = null
    private var onDrawListener: ((List<DrawPoint>) -> Unit)? = null
    private var onUndoListener: ((Int) -> Unit)? = null
    private var onClearListener: (() -> Unit)? = null

    fun setOnDrawListener(listener: (List<DrawPoint>) -> Unit) {
        this.onDrawListener = listener
    }

    fun setOnUndoListener(listener: (Int) -> Unit) {
        this.onUndoListener = listener
    }

    fun setOnClearListener(listener: () -> Unit) {
        this.onClearListener = listener
    }

    fun connect(roomId: String) {
        if (socket?.connected() == true) return

        val options = IO.Options().apply {
            reconnection = true
            reconnectionAttempts = 5
            reconnectionDelay = 1000
        }

        try {
            socket = IO.socket("http://192.168.10.14:3000", options)
            socket?.connect()

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("SocketManager", "Connected to server")
                socket?.emit("joinRoom", roomId)
            }

            socket?.on("draw") { args ->
                if (args.isNotEmpty()) {
                    val jsonArray = args[0] as JSONArray
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
                    onDrawListener?.invoke(points)
                }
            }

            socket?.on("undo") { args ->
                if (args.isNotEmpty()) {
                    val index = args[0] as Int
                    onUndoListener?.invoke(index)
                }
            }

            socket?.on("clear") {
                onClearListener?.invoke()
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("SocketManager", "Disconnected from server")
            }
        } catch (e: Exception) {
            Log.e("SocketManager", "Socket connection error", e)
        }
    }

    fun emitDraw(roomId: String, points: List<DrawPoint>) {
        if (socket?.connected() != true) return

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
    }

    fun emitUndo(roomId: String, strokeIndex: Int) {
        if (socket?.connected() != true) return
        val payload = JSONObject().apply {
            put("roomId", roomId)
            put("strokeIndex", strokeIndex)
        }
        socket?.emit("undo", payload)
    }

    fun emitClear(roomId: String) {
        if (socket?.connected() != true) return
        val payload = JSONObject().apply {
            put("roomId", roomId)
        }
        socket?.emit("clear", payload)
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }
}