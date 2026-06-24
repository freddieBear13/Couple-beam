package com.example.coupleapp.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST ("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("rooms/create")
    suspend fun createRoom(@Body request: CreateRoomRequest): Response<RoomResponse>

    @POST("rooms/join")
    suspend fun joinRoom(@Body request: JoinRoomRequest): Response<RoomResponse>
}