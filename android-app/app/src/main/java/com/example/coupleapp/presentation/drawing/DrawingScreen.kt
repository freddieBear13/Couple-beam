package com.example.coupleapp.presentation.drawing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DrawingScreen(
    roomId: String,
    viewModel: DrawingViewModel = hiltViewModel()
) {
    val strokes by viewModel.strokes.collectAsState()

    LaunchedEffect(roomId) {
        viewModel.init(roomId)
    }

    DrawingCanvas(
        onStrokeFinished = { stroke ->
            viewModel.onStrokeFinished(stroke)
        },
        allStrokes = strokes
    )
}