package com.example.coupleapp.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateRoomRequest(
    val ownerId: String
)

@JsonClass(generateAdapter = true)
data class JoinRoomRequest(
    val inviteCode: String,
    val partnerId: String
)

@JsonClass(generateAdapter = true)
data class RoomResponse(
    @Json(name = "inviteCode")
    val inviteCode: String,
    @Json(name = "partnerId")
    val partnerId: String?
)