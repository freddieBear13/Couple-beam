package com.example.coupleapp.presentation.drawing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coupleapp.presentation.room.RoomViewModel

@Composable
fun DrawingScreen(
    roomId: String,
    onNavigateBack: () -> Unit,
    viewModel: DrawingViewModel = hiltViewModel()
) {
    val strokes by viewModel.strokes.collectAsState()
    val color by viewModel.currentColor.collectAsState()
    val width by viewModel.currentStrokeWidth.collectAsState()
    val isLoadingHistory by viewModel.isLoadingHistory.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init(roomId)
    }

    BackHandler {
        viewModel.exitRoom()
        onNavigateBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DrawingCanvas(
            onStrokeFinished = viewModel::onStrokeFinished,
            allStrokes = strokes,
            currentColor = color,
            currentStrokeWidth = width,
            isLoadingHistory = isLoadingHistory
        )

        IconButton(
            onClick = {
                viewModel.exitRoom()
                onNavigateBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 35.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }

        IconButton(
            onClick = { viewModel.clearAll() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 35.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete all"
            )
        }

        DrawingToolbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            selectedColor = color,
            onColorSelected = viewModel::updateColor,
            strokeWidth = width,
            onStrokeWidthChanged = viewModel::updateStrokeWidth,
            onUndoClick = viewModel::undoLastStroke,
            isOnline = isOnline
        )
    }
}