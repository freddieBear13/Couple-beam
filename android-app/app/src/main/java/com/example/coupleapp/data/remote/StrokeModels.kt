package com.example.coupleapp.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PointDTO(
    val x: Float,
    val y: Float,
    val color: Int,
    val strokeWidth: Float
)

@JsonClass(generateAdapter = true)
data class StrokeRequest(
    val points: List<PointDTO>,
    val color: Int,
    val strokeWidth: Float
)

@JsonClass(generateAdapter = true)
data class StrokeResponse(
    val id: String,
    val points: List<PointDTO>,
    val color: Int,
    val strokeWidth: Float,
    val roomId: String? = null
)