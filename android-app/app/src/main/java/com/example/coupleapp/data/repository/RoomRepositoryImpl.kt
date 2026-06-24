package com.example.coupleapp.data.repository

import com.example.coupleapp.data.remote.ApiService
import com.example.coupleapp.data.remote.CreateRoomRequest
import com.example.coupleapp.data.remote.JoinRoomRequest
import com.example.coupleapp.domain.RoomRepository
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : RoomRepository {
    override suspend fun createRoom(userId: String): Result<String> {
        return try {
            val response = apiService.createRoom(CreateRoomRequest(userId))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.inviteCode)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Creating room error"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinRoom(inviteCode: String, userId: String): Result<Unit> {
        return try {
            val response = apiService.joinRoom(JoinRoomRequest(inviteCode, userId))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Joining room error"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}