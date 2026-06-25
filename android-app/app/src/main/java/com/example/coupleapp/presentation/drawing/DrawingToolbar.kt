package com.example.coupleapp.presentation.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DrawingToolbar(
    modifier: Modifier = Modifier,
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    strokeWidth: Float,
    onStrokeWidthChanged: (Float) -> Unit,
    onUndoClick: () -> Unit
) {
    val colors = listOf(
        0xFF000000.toInt(),
        0xFFFF0000.toInt(),
        0xFF0000FF.toInt(),
        0xFF00FF00.toInt(),
        0xFFFFFF00.toInt()
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (color in colors) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(
                                    width = if (color == selectedColor) 3.dp else 0.dp,
                                    color = Color.DarkGray,
                                    shape = CircleShape
                                )
                                .clickable { onColorSelected(color) }
                        )
                    }
                }

                Button(onClick = onUndoClick) {
                    Text("Undo")
                }
            }

            Slider(
                value = strokeWidth,
                onValueChange = onStrokeWidthChanged,
                valueRange = 2f..30f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}