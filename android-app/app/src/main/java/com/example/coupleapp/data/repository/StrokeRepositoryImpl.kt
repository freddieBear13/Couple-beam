package com.example.coupleapp.data.repository

import com.example.coupleapp.data.remote.ApiService
import com.example.coupleapp.data.remote.DrawPoint
import com.example.coupleapp.data.remote.PointDTO
import com.example.coupleapp.data.remote.StrokeRequest
import com.example.coupleapp.domain.StrokeRepository
import javax.inject.Inject

class StrokeRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : StrokeRepository {

    override suspend fun saveStroke(roomId: String, points: List<DrawPoint>): Result<Unit> {
        return try {
            if (points.isEmpty()) {
                return Result.success(Unit)
            }

            val pointDtos = points.map { point ->
                PointDTO(
                    x = point.x,
                    y = point.y,
                    color = point.color,
                    strokeWidth = point.strokeWidth
                )
            }

            val request = StrokeRequest(
                points = pointDtos,
                color = points[0].color,
                strokeWidth = points[0].strokeWidth
            )

            apiService.saveStroke(roomId, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loadHistory(roomId: String): Result<List<List<DrawPoint>>> {
        return try {
            val responses = apiService.getStrokes(roomId)

            val strokes = responses.map { response ->
                response.points.map { pointDto ->
                    DrawPoint(
                        x = pointDto.x,
                        y = pointDto.y,
                        color = pointDto.color,
                        strokeWidth = pointDto.strokeWidth
                    )
                }
            }

            Result.success(strokes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteLastStroke(roomId: String): Result<Unit> {
        return try {
            apiService.deleteLastStroke(roomId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllStrokes(roomId: String): Result<Unit> {
        return try {
            apiService.deleteAllStrokes(roomId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}