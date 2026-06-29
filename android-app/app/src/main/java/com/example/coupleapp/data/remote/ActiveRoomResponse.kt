package com.example.coupleapp.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ActiveRoomResponse(
    val hasRoom: Boolean,
    val roomId: String? = null,
    val code: String? = null
)