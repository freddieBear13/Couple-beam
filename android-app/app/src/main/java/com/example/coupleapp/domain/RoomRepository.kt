package com.example.coupleapp.domain

interface RoomRepository {
    suspend fun createRoom(userId: String): Result<String>
    suspend fun joinRoom(inviteCode: String, userId: String): Result<Unit>
}