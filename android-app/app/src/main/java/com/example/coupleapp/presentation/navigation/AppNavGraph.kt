package com.example.coupleapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import com.example.coupleapp.presentation.auth.AuthViewModel
import com.example.coupleapp.presentation.auth.LoginScreen
import com.example.coupleapp.presentation.auth.RegisterScreen
import com.example.coupleapp.presentation.drawing.DrawingScreen
import com.example.coupleapp.presentation.room.RoomSetupScreen
import com.example.coupleapp.presentation.room.RoomViewModel
import com.example.coupleapp.presentation.main.MainScreen

object Screen {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ROOM_SETUP = "room_setup"
    const val DRAWING = "drawing"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.LOGIN
    ) {
        composable(Screen.LOGIN) {
            val viewModel: AuthViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.isSuccess, state.userId) {
                if (state.isSuccess && state.userId != null) {
                    navController.navigate("${Screen.ROOM_SETUP}/${state.userId}") {
                        popUpTo(Screen.LOGIN) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                onLoginClick = { email, password ->
                    viewModel.login(email, password)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.REGISTER)
                }
            )
        }

        composable(Screen.REGISTER) {
            val viewModel : AuthViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.isSuccess, state.userId) {
                if (state.isSuccess && state.userId != null) {
                    navController.navigate("${Screen.ROOM_SETUP}/${state.userId}") {
                        popUpTo(Screen.REGISTER) { inclusive = true }
                    }
                }
            }

            RegisterScreen(
                onRegisterClick = { email, password ->
                    viewModel.register(email, password)
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "${Screen.ROOM_SETUP}/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val viewModel: RoomViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.isJoined, state.roomId) {
                if (state.isJoined && state.roomId != null) {
                    navController.navigate("${Screen.DRAWING}/${state.roomId}") {
                        popUpTo(Screen.ROOM_SETUP) { inclusive = true }
                    }
                }
            }

            RoomSetupScreen(
                viewModel = viewModel,
                userId = userId,
                onNavigateToDrawing = { roomId ->
                    navController.navigate("${Screen.DRAWING}/$roomId") {
                        popUpTo(Screen.ROOM_SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Screen.DRAWING}/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: return@composable
            DrawingScreen(
                roomId = roomId,
                onNavigateBack = {
                    navController.navigate(Screen.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}