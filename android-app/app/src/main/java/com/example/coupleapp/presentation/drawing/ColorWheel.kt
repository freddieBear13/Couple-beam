package com.example.coupleapp.presentation.drawing

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.sqrt

@Composable
fun ColorWheel(
    currentHue: Float,
    onHueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier.size(250.dp)
) {
    val sweepColors = listOf(
        Color.Red,
        Color.Magenta,
        Color.Blue,
        Color.Cyan,
        Color.Green,
        Color.Yellow,
        Color.Red
    )

    val indicatorAngleRadians = Math.toRadians(currentHue.toDouble())

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val dx = offset.x - centerX
                    val dy = offset.y - centerY
                    val distance = sqrt(dx * dx + dy * dy)
                    val radius = size.width / 2f

                    if (distance <= radius) {
                        val angle = (Math.toDegrees(atan2(-dy.toDouble(), dx.toDouble())) + 360) % 360
                        onHueChanged(angle.toFloat())
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val dx = change.position.x - centerX
                    val dy = change.position.y - centerY
                    val distance = sqrt(dx * dx + dy * dy)
                    val radius = size.width / 2f

                    if (distance <= radius) {
                        val angle = (Math.toDegrees(atan2(-dy.toDouble(), dx.toDouble())) + 360) % 360
                        onHueChanged(angle.toFloat())
                    }
                }
            }
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = size.width / 2f

        drawCircle(
            brush = Brush.sweepGradient(
                colors = sweepColors,
                center = Offset(centerX, centerY)
            ),
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 40f)
        )

        val indicatorX = centerX + (radius * kotlin.math.cos(indicatorAngleRadians)).toFloat()
        val indicatorY = centerY - (radius * kotlin.math.sin(indicatorAngleRadians)).toFloat()

        drawCircle(
            color = Color.White,
            radius = 18f,
            center = Offset(indicatorX, indicatorY)
        )

        val hsv = floatArrayOf(currentHue, 1f, 1f)
        val indicatorColor = AndroidColor.HSVToColor(hsv)

        drawCircle(
            color = Color(indicatorColor),
            radius = 14f,
            center = Offset(indicatorX, indicatorY)
        )
    }
}