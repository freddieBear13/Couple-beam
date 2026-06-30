package com.example.coupleapp.presentation.drawing


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.coupleapp.data.remote.DrawPoint


@Composable
fun DrawingCanvas(
    onStrokeFinished: (List<DrawPoint>) -> Unit,
    allStrokes: List<List<DrawPoint>>,
    currentColor: Int,
    currentStrokeWidth: Float,
    isLoadingHistory: Boolean
) {
    val currentStroke = remember { mutableStateListOf<DrawPoint>() }
    val currentPath = remember { Path() }

    val latestColor by rememberUpdatedState(currentColor)
    val latestWidth by rememberUpdatedState(currentStrokeWidth)

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath.moveTo(offset.x, offset.y)
                            currentStroke.clear()
                            currentStroke.add(
                                DrawPoint(
                                    offset.x,
                                    offset.y,
                                    latestColor,
                                    latestWidth
                                )
                            )
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPath.lineTo(change.position.x, change.position.y)
                            currentStroke.add(
                                DrawPoint(
                                    change.position.x,
                                    change.position.y,
                                    latestColor,
                                    latestWidth
                                )
                            )
                        },
                        onDragEnd = {
                            if (currentStroke.size > 1) {
                                onStrokeFinished(currentStroke.toList())
                            }
                            currentPath.reset()
                            currentStroke.clear()
                        },
                        onDragCancel = {
                            currentPath.reset()
                            currentStroke.clear()
                        }
                    )
                }
        ) {
            for (stroke in allStrokes) {
                if (stroke.size > 1) {
                    val path = Path()
                    path.moveTo(stroke[0].x, stroke[0].y)
                    for (i in 1 until stroke.size) {
                        path.lineTo(stroke[i].x, stroke[i].y)
                    }
                    drawPath(
                        path = path,
                        color = Color(stroke[0].color),
                        style = Stroke(
                            width = stroke[0].strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            if (currentStroke.size > 1) {
                drawPath(
                    path = currentPath,
                    color = Color(latestColor),
                    style = Stroke(
                        width = latestWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        if (isLoadingHistory) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.7f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Downloading history...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}