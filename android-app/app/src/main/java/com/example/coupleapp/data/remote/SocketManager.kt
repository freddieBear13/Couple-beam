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

    fun setOnDrawListener(listener: (List<DrawPoint>) -> Unit) {
        this.onDrawListener = listener
    }

    fun connect(roomId: String) {
        if (socket?.connected() == true) return

        val options = IO.Options().apply {
            reconnection = true
            reconnectionAttempts = 5
            reconnectionDelay = 1000
        }

        try {
            socket = IO.socket("http://10.0.2.2:3000", options)
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
                        points.add(DrawPoint(obj.getDouble("x").toFloat(), obj.getDouble("y").toFloat()))
                    }
                    onDrawListener?.invoke(points)
                }
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
                put("x", point.x)
                put("y", point.y)
            }
            jsonArray.put(obj)
        }

        val payload = JSONObject().apply {
            put("roomId", roomId)
            put("points", jsonArray)
        }

        socket?.emit("draw", payload)
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }
}