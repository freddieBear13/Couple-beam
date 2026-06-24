package com.example.coupleapp.presentation.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.coupleapp.data.remote.DrawPoint

@Composable
fun DrawingCanvas(
    onStrokeFinished: (List<DrawPoint>) -> Unit,
    allStrokes: List<List<DrawPoint>>
) {
    val currentPath = remember { Path() }
    val currentStroke = remember { mutableStateListOf<DrawPoint>() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        currentPath.moveTo(offset.x, offset.y)
                        currentStroke.clear()
                        currentStroke.add(DrawPoint(offset.x, offset.y))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val newPoint = DrawPoint(change.position.x, change.position.y)
                        currentPath.lineTo(change.position.x, change.position.y)
                        currentStroke.add(newPoint)
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
                    color = Color.Black,
                    style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
        if (currentStroke.size > 1) {
            drawPath(
                path = currentPath,
                color = Color.Black,
                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}