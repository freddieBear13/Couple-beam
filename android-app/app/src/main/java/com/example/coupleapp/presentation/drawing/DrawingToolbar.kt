package com.example.coupleapp.presentation.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun DrawingToolbar(
    modifier: Modifier = Modifier,
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    strokeWidth: Float,
    onStrokeWidthChanged: (Float) -> Unit,
    onUndoClick: () -> Unit
) {
    val presetColors = listOf(
        0xFF000000.toInt(),
        0xFFFF0000.toInt(),
        0xFF0000FF.toInt(),
        0xFF00FF00.toInt(),
        0xFFFFFF00.toInt()
    )

    var showColorPicker by remember { mutableStateOf(false) }
    var lastCustomColor by remember { mutableStateOf(0xFFFF0000.toInt()) }

    LaunchedEffect(selectedColor) {
        if (!presetColors.contains(selectedColor)) {
            lastCustomColor = selectedColor
        }
    }

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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (color in presetColors) {
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

                    // RGB кружок: показывает кастомный цвет или радужный градиент
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(lastCustomColor))
                            .border(
                                width = if (selectedColor == lastCustomColor) 3.dp else 1.dp,
                                color = Color.DarkGray,
                                shape = CircleShape
                            )
                            .clickable { showColorPicker = true }
                    ) {
                        Text(
                            text = "+",
                            color = Color.DarkGray,
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Button(
                    onClick = onUndoClick,
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("U")
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

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = lastCustomColor,
            onColorSelected = { newColor ->
                onColorSelected(newColor)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}