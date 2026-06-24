package com.example.coupleapp.domain

interface AuthRepository {
    suspend fun login(email: String, password: String) : Result<String>

    suspend fun register(email: String, password: String) : Result<String>
}