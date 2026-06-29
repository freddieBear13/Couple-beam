package com.example.coupleapp.domain

import com.example.coupleapp.data.remote.ActiveRoomResponse

interface RoomRepository {
    suspend fun createRoom(userId: String): Result<String>
    suspend fun joinRoom(inviteCode: String, userId: String): Result<Unit>

    suspend fun getActiveRoom(userId: String): Result<ActiveRoomResponse>
}