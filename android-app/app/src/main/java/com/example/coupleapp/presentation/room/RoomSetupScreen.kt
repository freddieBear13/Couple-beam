package com.example.coupleapp.presentation.room

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun RoomSetupScreen(
    viewModel: RoomViewModel,
    userId: String,
    onNavigateToDrawing: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var inputCode by remember { mutableStateOf("") }

    LaunchedEffect(state.isJoined, state.roomId) {
        val currentRoomId = state.roomId
        if (state.isJoined && currentRoomId != null) {
            onNavigateToDrawing(currentRoomId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Pair setup",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else if (state.generatedCode != null) {
            Text(
                text = "Your code:",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = state.generatedCode!!,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Button(
                onClick = { state.roomId?.let { onNavigateToDrawing(it) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Partner is connected, start drawing")
            }
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (state.generatedCode == null) {
            Button(
                onClick = { viewModel.createRoom(userId) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Text("Create new room")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth())

        Text(
            text = "Or enter partner code",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = inputCode,
            onValueChange = { inputCode = it.uppercase() },
            label = { Text("Parnter code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (inputCode != state.generatedCode) {
                    viewModel.joinRoom(inputCode, userId)
                } else {
                    viewModel.setError("Can't input your own code")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && inputCode.length == 6
        ) {
            Text("Join room")
        }
    }
}