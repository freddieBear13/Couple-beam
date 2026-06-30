package com.example.coupleapp.domain

import com.example.coupleapp.data.remote.DrawPoint

interface StrokeRepository {
    suspend fun saveStroke(roomId: String, points: List<DrawPoint>): Result<Unit>
    suspend fun loadHistory(roomId: String): Result<List<List<DrawPoint>>>
    suspend fun deleteLastStroke(roomId: String): Result<Unit>
    suspend fun deleteAllStrokes(roomId: String): Result<Unit>
}