package com.example.coupleapp.data.di

import com.example.coupleapp.data.repository.AuthRepositoryImpl
import com.example.coupleapp.data.repository.RoomRepositoryImpl
import com.example.coupleapp.domain.AuthRepository
import com.example.coupleapp.domain.RoomRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ) : AuthRepository

    @Binds
    @Singleton
    abstract fun bindRoomRepository(
        impl: RoomRepositoryImpl
    ) : RoomRepository
}