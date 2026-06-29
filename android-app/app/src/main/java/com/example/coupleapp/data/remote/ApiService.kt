package com.example.coupleapp.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST ("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("rooms/create")
    suspend fun createRoom(@Body request: CreateRoomRequest): Response<RoomResponse>

    @POST("rooms/join")
    suspend fun joinRoom(@Body request: JoinRoomRequest): Response<RoomResponse>

    @GET("rooms/active")
    suspend fun getActiveRoom(@Query("userId") userId: String): ActiveRoomResponse

    @POST("rooms/{roomId}/strokes")
    suspend fun saveStroke(
        @Path("roomId") roomId: String,
        @Body request: StrokeRequest
    ): StrokeSaveResponse

    @GET("rooms/{roomId}/strokes")
    suspend fun getStrokes(
        @Path("roomId") roomId: String
    ): List<StrokeResponse>
}

data class StrokeSaveResponse(
    val id: String
)