package com.example.coupleapp.data.repository

import android.util.Log
import com.example.coupleapp.data.local.TokenManager
import com.example.coupleapp.data.remote.ApiService
import com.example.coupleapp.data.remote.LoginRequest
import com.example.coupleapp.data.remote.RegisterRequest
import com.example.coupleapp.domain.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<String> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            Log.d("AuthDebug", "Response code: ${response.code()}")
            Log.d("AuthDebug", "Response body: ${response.body()}")
            if (!response.isSuccessful) {
                Log.d("AuthDebug", "Error body: ${response.errorBody()?.string()}")
            }
            if (response.isSuccessful && response.body() != null) {
                tokenManager.saveToken(response.body()!!.accessToken)
                Result.success(response.body()!!.userId)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Authorization error"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<String> {
        return try {
            val response = apiService.register(RegisterRequest(email, password))
            Log.d("AuthDebug", "Response code: ${response.code()}")
            Log.d("AuthDebug", "Response body: ${response.body()}")
            if (!response.isSuccessful) {
                Log.d("AuthDebug", "Error body: ${response.errorBody()?.string()}")
            }
            if (response.isSuccessful) {
                Result.success(response.body()!!.userId)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Registration error"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}