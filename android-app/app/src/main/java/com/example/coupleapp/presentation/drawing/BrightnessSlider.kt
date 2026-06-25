package com.example.coupleapp.presentation.drawing

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun BrightnessSlider(
    currentHue: Float,
    currentValue: Float,
    onValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
) {
    val hsv = floatArrayOf(currentHue, 1f, 1f)
    val fullColor = AndroidColor.HSVToColor(hsv)

    val indicatorPosition = currentValue * 100f

    Canvas(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newValue = (offset.x / size.width).coerceIn(0f, 1f)
                    onValueChanged(newValue)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val newValue = (change.position.x / size.width).coerceIn(0f, 1f)
                    onValueChanged(newValue)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val cornerRadius = height / 2f

        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Black,
                    Color(fullColor)
                ),
                startX = 0f,
                endX = width
            ),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
        )

        val indicatorX = (indicatorPosition / 100f) * width

        drawCircle(
            color = Color.White,
            radius = 16f,
            center = Offset(indicatorX, height / 2f)
        )

        val currentHsv = floatArrayOf(currentHue, 1f, currentValue)
        val currentColor = AndroidColor.HSVToColor(currentHsv)

        drawCircle(
            color = Color(currentColor),
            radius = 12f,
            center = Offset(indicatorX, height / 2f)
        )
    }

}