package com.example.coupleapp.presentation.drawing

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coupleapp.presentation.widget.DrawingWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val success = saveToUri(context, uri, viewModel)
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(context, "Image saved", Toast.LENGTH_SHORT).show()
                        val savedForWidget = viewModel.saveLastDrawing(context)
                        if (savedForWidget) {
                            DrawingWidgetProvider.requestUpdate(context)
                        }
                    } else {
                        Toast.makeText(context, "Error saving", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

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

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        val shared = shareImage(context, viewModel)
                        withContext(Dispatchers.Main) {
                            if (!shared) {
                                Toast.makeText(context, "Export error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.padding(top = 35.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share"
                )
            }

            IconButton(
                onClick = {
                    saveLauncher.launch("drawing_${System.currentTimeMillis()}.png")
                },
                modifier = Modifier
                    .padding(top = 35.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Save"
                )
            }

            IconButton(
                onClick = { viewModel.clearAll() },
                modifier = Modifier
                    .padding(top = 35.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete all"
                )
            }
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

private suspend fun saveToUri(context: Context, uri: Uri, viewModel: DrawingViewModel): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val metrics = context.resources.displayMetrics
            val pngBytes = viewModel.exportToPng(metrics.widthPixels, metrics.heightPixels)

            if (pngBytes == null) return@withContext false

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(pngBytes)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

private suspend fun shareImage(context: Context, viewModel: DrawingViewModel): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val metrics = context.resources.displayMetrics
            val pngBytes = viewModel.exportToPng(metrics.widthPixels, metrics.heightPixels)
                ?: return@withContext false

            val fileName = "drawing_${System.currentTimeMillis()}.png"
            val cacheFile = File(context.cacheDir, fileName)

            FileOutputStream(cacheFile).use { fos ->
                fos.write(pngBytes)
            }

            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, cacheFile)

            withContext(Dispatchers.Main) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, "Share image")
                context.startActivity(chooser)
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}