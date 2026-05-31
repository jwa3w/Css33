package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerEntity
import com.example.data.WildcardRuleEntity
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// --- Screen 1: Welcome & Setup ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeScreen(vm: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // UNO App Branding Header
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE00010))
                    .border(4.dp, Color(0xFFFFC107), RoundedCornerShape(24.dp))
                    .padding(horizontal = 40.dp, vertical = 20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "UNO",
                        color = Color.White,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "WHOSE TURN?",
                        color = Color(0xFFFFC107),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Select Autonomous Play Mode",
                color = Color.LightGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Cards Grid
            val modes = listOf(
                Triple("Classic", "Original rules, turn prompts & smart load cell checks.", Color(0xFF4CAF50)),
                Triple("Chaotic", "Injects random Gemini triggers & custom wildcard alerts.", Color(0xFFFF5722)),
                Triple("Dual", "Heated 1v1 battle layout with fast turnaround pacing.", Color(0xFF2196F3)),
                Triple("Speed", "Timer active! Players get 5s to play card or draw.", Color(0xFFFF9800))
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                modes.forEach { (mode, desc, accent) ->
                    val isSelected = vm.gameMode == mode
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .height(115.dp)
                            .clickable { vm.gameMode = mode },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1A1D24) else Color(0xFF1E2129)
                        ),
                        border = if (isSelected) BorderStroke(2.dp, accent) else BorderStroke(1.dp, Color.Transparent),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mode,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(accent, CircleShape)
                                )
                            }
                            Text(
                                text = desc,
                                color = Color.Gray,
                                fontSize = 10.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Score Limit configuration
            Text(
                text = "Target Score Limit: ${vm.targetScoreLimit} pts",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = vm.targetScoreLimit.toFloat(),
                onValueChange = { vm.targetScoreLimit = it.toInt() },
                valueRange = 100f..1000f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFE00010),
                    activeTrackColor = Color(0xFFFFC107),
                    inactiveTrackColor = Color.DarkGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { vm.navigateTo(2) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("initiate_match_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "INITIATE DEVICE SCAN",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Filled.Camera, contentDescription = "Scan", tint = Color.Black)
            }
        }
    }
}

// --- Screen 2: Wi-Fi Setup & QR Scanner ---
@Composable
fun WifiConnectionScreen(vm: GameViewModel) {
    val infiniteTransition = rememberInfiniteTransition()
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "PAIR WITH ESP32-S3 COMPANION",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Scan the QR code on the back of the device board to connect instantly.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Simulated QR Scanner Frame with sliding neon line
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .border(4.dp, Color(0xFF2196F3), RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        // Drawing corner focus lines
                        val focusStroke = 12f
                        val focusLen = 60f
                        val focusColor = Color(0xFF2196F3)

                        // Top-Left
                        drawLine(focusColor, Offset(0f, 0f), Offset(focusLen, 0f), focusStroke)
                        drawLine(focusColor, Offset(0f, 0f), Offset(0f, focusLen), focusStroke)

                        // Top-Right
                        drawLine(focusColor, Offset(size.width, 0f), Offset(size.width - focusLen, 0f), focusStroke)
                        drawLine(focusColor, Offset(size.width, 0f), Offset(size.width, focusLen), focusStroke)

                        // Bottom-Left
                        drawLine(focusColor, Offset(0f, size.height), Offset(focusLen, size.height), focusStroke)
                        drawLine(focusColor, Offset(0f, size.height), Offset(0f, size.height - focusLen), focusStroke)

                        // Bottom-Right
                        drawLine(focusColor, Offset(size.width, size.height), Offset(size.width - focusLen, size.height), focusStroke)
                        drawLine(focusColor, Offset(size.width, size.height), Offset(size.width, size.height - focusLen), focusStroke)

                        // QR elements
                        drawRect(Color.White, Offset(40f, 40f), Size(50f, 50f))
                        drawRect(Color.Black, Offset(50f, 50f), Size(30f, 30f))

                        drawRect(Color.White, Offset(size.width - 90f, 40f), Size(50f, 50f))
                        drawRect(Color.Black, Offset(size.width - 80f, 50f), Size(30f, 30f))

                        drawRect(Color.White, Offset(40f, size.height - 90f), Size(50f, 50f))
                        drawRect(Color.Black, Offset(50f, size.height - 80f), Size(30f, 30f))

                        // Scattered QR noise
                        drawRect(Color.White, Offset(110f, 100f), Size(40f, 20f))
                        drawRect(Color.LightGray, Offset(150f, 130f), Size(20f, 40f))
                        drawRect(Color.DarkGray, Offset(100f, 150f), Size(30f, 30f))
                        drawRect(Color.White, Offset(170f, 80f), Size(25f, 25f))

                        // Hologram scan beam
                        drawLine(
                            color = Color(0xFF00E676),
                            start = Offset(0f, scanOffset),
                            end = Offset(size.width, scanOffset),
                            strokeWidth = 6f
                        )
                    }

                    if (vm.isEsp32Connected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.CheckCircle, "Connected", tint = Color(0xFF00E676), modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("CONNECTED!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("IP: ${vm.esp32IpAddress}", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // AP change status tracker text
                Text(
                    text = "Status: ${vm.ssidChangeStatus}",
                    color = when (vm.ssidChangeStatus) {
                        "Successfully Connected to ESP32!" -> Color(0xFF00E676)
                        "Idle" -> Color.Gray
                        else -> Color(0xFFFFC107)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Router Mode toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E2129))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ESP32 Acts as Wifi Router AP", color = Color.LightGray, fontSize = 12.sp)
                    Switch(
                        checked = vm.routerModeActive,
                        onCheckedChange = { vm.routerModeActive = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2196F3))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input fields if required OR simulated trigger
                Button(
                    onClick = { vm.scanQrCodeSsidAndConnect("WIFI:S:ESP32_S3_UNO_AP;T:WPA;P:UNOWhoTurn123;;") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    modifier = Modifier.width(220.dp).testTag("sim_qr_trigger")
                ) {
                    Text("SIMULATE QR SCAN (3s)")
                }
            }

            Button(
                onClick = { vm.navigateTo(3) },
                enabled = vm.isEsp32Connected,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("go_register_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("PROCEED REGISTER PLAYERS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Filled.ArrowForward, "Next", tint = Color.Black)
            }
        }
    }
}

// --- Screen 3: Player Registration Screen ---
@Composable
fun PlayerRegistrationScreen(vm: GameViewModel, playersList: List<PlayerEntity>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.navigateTo(2) }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        text = "PLAYER REGISTRATION",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = {
                        // Reset player helper
                        kotlinx.coroutines.GlobalScope.launch { vm.repository.resetPlayers() }
                    }) {
                        Icon(Icons.Filled.DeleteSweep, "Clear database", tint = Color(0xFFFF5722))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input forms
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add New Challenger", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = vm.newPlayerName,
                            onValueChange = { vm.newPlayerName = it },
                            label = { Text("Player Name", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFC107),
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("player_name_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom color indicator
                        Text("Select Player Color Halo", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        val colors = listOf("#FF3B30", "#34C759", "#007AFF", "#FFCC00", "#AF52DE", "#FF9500")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            colors.forEach { hex ->
                                val isSelected = vm.newPlayerColorHex == hex
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { vm.newPlayerColorHex = hex }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Microphone capture simulator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { vm.startRecordingVoiceName() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (vm.isRecordingName) Color(0xFFFF5722) else Color(0xFF4CAF50)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    if (vm.isRecordingName) Icons.Filled.Mic else Icons.Filled.MicNone,
                                    "Mic",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (vm.isRecordingName) "Recording ${vm.recordingTimer}s..." else "Record Voice Profile",
                                    color = Color.White
                                )
                            }
                            if (vm.isRecordingName) {
                                // Simple mic line wave animation
                                LinearProgressIndicator(
                                    color = Color(0xFFFF5722),
                                    trackColor = Color.DarkGray,
                                    modifier = Modifier.width(80.dp).height(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { vm.registerPlayer() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("add_player_btn")
                        ) {
                            Icon(Icons.Filled.Add, "Add player", tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ADD REGISTERED PLAYER", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Registered list
                Text(
                    text = "Player Room List (First joins is Admin)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(playersList) { p ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(p.colorHex)))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = p.name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (p.isAdmin) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFFFC107))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Admin", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }

                                Row {
                                    IconButton(onClick = { vm.playRecordedVoiceLog(p.name) }) {
                                        Icon(Icons.Filled.VolumeUp, "Voice Profile", tint = Color(0xFF2196F3))
                                    }
                                    IconButton(onClick = {
                                        kotlinx.coroutines.GlobalScope.launch { vm.repository.removePlayer(p) }
                                    }) {
                                        Icon(Icons.Filled.Delete, "Remove", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    vm.activePlayers.clear()
                    vm.activePlayers.addAll(playersList)
                    vm.navigateTo(4)
                },
                enabled = playersList.size >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("go_dealer_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("DETERMINE DEALER", color = Color.Black, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Filled.Casino, "Dealer selection", tint = Color.Black)
            }
        }
    }
}

// --- Screen 4: Dealer Selection Screen ---
@Composable
fun DealerSelectionScreen(vm: GameViewModel) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (vm.isSpinningDealer) 600 else 6000, easing = LinearEasing)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "DETERMINING DEALER",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "The system will automatically rotate and stop on the initial dealer.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Spinning Dial Canvas representation
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2129))
                    .border(4.dp, Color(0xFFFFC107), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotationAngle)
                ) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    // Draw segments for active players
                    val playersCount = if (vm.activePlayers.isEmpty()) 1 else vm.activePlayers.size
                    val sweep = 360f / playersCount
                    val sectorColors = listOf(Color(0xFFE00010), Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFF9C27B0))

                    for (i in 0 until playersCount) {
                        val color = sectorColors[i % sectorColors.size]
                        drawArc(
                            color = color.copy(alpha = 0.85f),
                            startAngle = i * sweep,
                            sweepAngle = sweep,
                            useCenter = true,
                            size = Size(size.width, size.height)
                        )
                    }

                    // Sector separators
                    for (i in 0 until playersCount) {
                        val angleRad = Math.toRadians((i * sweep).toDouble())
                        val endX = center.x + radius * cos(angleRad).toFloat()
                        val endY = center.y + radius * sin(angleRad).toFloat()
                        drawLine(Color(0xFF121214), center, Offset(endX, endY), 6f)
                    }
                }

                // Foreground pointer / center holder
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF121214))
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Navigation,
                        "Indicator pointer",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier
                            .size(42.dp)
                            .rotate(180f) // Facing down
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                val selectedDealer = vm.activePlayers.firstOrNull { it.id == vm.selectedDealerId }
                if (selectedDealer != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(android.graphics.Color.parseColor(selectedDealer.colorHex))),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .shadow(8.dp, RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("DESIGNATED DEALER", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.sp)
                            Text(selectedDealer.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }
                    }
                } else {
                    Text("No dealer designated yet", color = Color.Gray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { vm.triggerSpinDealer() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("spin_dealer_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SPIN WHEEL OF DEALERS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// --- Screen 5: Rules & Wildcard Screen ---
@Composable
fun WildcardConfigScreen(vm: GameViewModel, rules: List<WildcardRuleEntity>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.navigateTo(3) }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        text = "WILDCARD GAME SETUP",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = { vm.navigateTo(6) }) {
                        Icon(Icons.Filled.SettingsInputHdmi, "Calibration screen", tint = Color(0xFFFFCC00))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Configure custom rule modes or triggers that the ESP32 speaks out loud during play actions.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Gemini button selector to design a rule
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D24)),
                    border = BorderStroke(1.dp, Color(0xFFFFC107)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gemini Rules Generator", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Dynamically forge a random rule utilizing your matching Gemini API key.", color = Color.Gray, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { vm.generateFunAiWildcardIdea() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                            enabled = !vm.isGeneratingSpeech
                        ) {
                            Icon(Icons.Filled.AutoAwesome, "Gemini Spark", tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (vm.isGeneratingSpeech) "Forging..." else "Forge", color = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Custom Rules Configured: (Select 4)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(8.dp))

                // Scroll list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(rules) { rule ->
                        val isChecked = vm.selectedRulesIds.value.contains(rule.id)
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChecked) Color(0xFF1F2430) else Color(0xFF171921)
                            ),
                            border = if (isChecked) BorderStroke(1.5.dp, Color(0xFF00E676)) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val currentSet = vm.selectedRulesIds.value.toMutableSet()
                                    if (currentSet.contains(rule.id)) {
                                        currentSet.remove(rule.id)
                                    } else {
                                        currentSet.add(rule.id)
                                    }
                                    vm.selectedRulesIds.value = currentSet
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            when (rule.actionType) {
                                                "CHASER_LIGHTS" -> Icons.Filled.Lightbulb
                                                "SOUND_EFFECT" -> Icons.Filled.VolumeUp
                                                "REVERSE" -> Icons.Filled.Autorenew
                                                else -> Icons.Filled.Layers
                                            },
                                            "Rule action",
                                            tint = Color(0xFFFFC107)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(rule.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            val currentSet = vm.selectedRulesIds.value.toMutableSet()
                                            if (it) currentSet.add(rule.id) else currentSet.remove(rule.id)
                                            vm.selectedRulesIds.value = currentSet
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00E676))
                                    )
                                }
                                Text(rule.description, color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                                Text(
                                    "Host voice prompt: \"${rule.voicePrompt}\"",
                                    color = Color(0xFF2196F3),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    vm.startGamePlay()
                    vm.navigateTo(7) // Navigate to Active play board!
                },
                enabled = vm.selectedRulesIds.value.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_game_play_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("LAUNCH THE ACTIVE BATTLE", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}

// --- Screen 6: Calibration Screen ---
@Composable
fun CalibrationScreen(vm: GameViewModel) {
    var cameraAngleSelection by remember { mutableStateOf("Center (180°)") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.navigateTo(5) }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        text = "CV & SCALE CALIBRATION",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Align device physical sensors (Load scale baseline, camera angle, range mapping).", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(20.dp))

                // Camera Direction alignment card
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Servo & Lens Direction Control", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Point ESP32 lens to match player cards physical orientation.", color = Color.Gray, fontSize = 11.sp)
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("Left (90°)", "Center (180°)", "Right (270°)").forEach { d ->
                                val sel = cameraAngleSelection == d
                                FilterChip(
                                    selected = sel,
                                    onClick = {
                                        cameraAngleSelection = d
                                        vm.logAction("Camera adjusted to physically look at: $d")
                                    },
                                    label = { Text(d) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFFC107),
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Load-cell scale tare card
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Load-Cell Calibration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Set cumulative scale threshold. Make sure no cards are on the platform.", color = Color.Gray, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Current Mass Weight", color = Color.Gray, fontSize = 11.sp)
                                Text("${vm.loadCellWeight} grams", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            }
                            Button(
                                onClick = {
                                    vm.tareWeight = vm.loadCellWeight
                                    vm.loadCellWeight = 0f
                                    vm.logAction("Scale platform TARE reset applied!")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                            ) {
                                Text("ZERO / TARE")
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { vm.navigateTo(5) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SAVE CALIBRATION OPTIONS", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Screen 7: Active Play Dashboard ---
@Composable
fun GameDashboardScreen(vm: GameViewModel) {
    val activePlayer = vm.currentTurnPlayer ?: return
    val infiniteTransition = rememberInfiniteTransition()
    
    // Rotating flow indicator animation
    val arrowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (vm.gameDirectionClockwise) 360f else -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.navigateTo(12) }) {
                    Icon(Icons.Filled.Settings, "Config", tint = Color.White)
                }
                
                // Active Card styling representation
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (vm.activeCardColor) {
                                "Red" -> Color(0xFFE00010)
                                "Yellow" -> Color(0xFFFFC107)
                                "Blue" -> Color(0xFF2196F3)
                                "Green" -> Color(0xFF4CAF50)
                                else -> Color.DarkGray
                            }
                        )
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${vm.activeCardColor} ${vm.activeCardValue}",
                        color = if (vm.activeCardColor == "Yellow") Color.Black else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                IconButton(onClick = { vm.navigateTo(11) }) {
                    Icon(Icons.Filled.BarChart, "Statistics", tint = Color.White)
                }
            }

            // Direction arrow chasers
            Spacer(modifier = Modifier.height(10.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().rotate(arrowRotation)) {
                        drawCircle(
                            color = Color(0xFFFFC107).copy(alpha = 0.15f),
                            radius = size.minDimension / 2,
                            style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f))
                        )
                    }
                    Icon(
                        if (vm.gameDirectionClockwise) Icons.Filled.Autorenew else Icons.Filled.History,
                        "Direction",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(54.dp)
                    )
                }
                Text(
                    text = if (vm.gameDirectionClockwise) "CLOCKWISE PLAY" else "COUNTER-CLOCKWISE PLAY",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Player Turn details halo banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)),
                border = BorderStroke(3.dp, Color(android.graphics.Color.parseColor(activePlayer.colorHex))),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CURRENT PLAYING TURN", color = Color.Gray, fontSize = 11.sp, letterSpacing = 2.sp)
                    Text(
                        text = activePlayer.name,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Holding Handcards count: ${activePlayer.activeHandSize} cards",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
            }

            // Alert banner if rule violation
            if (vm.detectedRuleViolation) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE00010))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, "Danger Alert", tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(vm.detectedRuleViolationMsg, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid of trigger gameplay simulation actions
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { vm.advanceTurn() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("play_card_btn")
                    ) {
                        Icon(Icons.Filled.CheckCircle, "Play")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PLAY CARD", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { vm.triggerPlayerDrawCard(vm.currentTurnIndex) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("draw_card_btn")
                    ) {
                        Icon(Icons.Filled.Layers, "Draw")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DRAW CARD", fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { vm.changePlayDirection() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.CompareArrows, "Swap")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("REVERSE", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            vm.handleRuleViolation(activePlayer, "Played Out of Order")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.Gavel, "Trigger Penalty")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("FALSE PLAY ILLEGAL", fontSize = 12.sp)
                    }
                }

                // Rapid links to cameras/sensors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { vm.navigateTo(8) }) {
                        Icon(Icons.Filled.Videocam, "Camera Live", tint = Color.LightGray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Live Cam Monitor", color = Color.LightGray, fontSize = 12.sp)
                    }
                    TextButton(onClick = { vm.navigateTo(9) }) {
                        Icon(Icons.Filled.NetworkCheck, "Sensors board", tint = Color.LightGray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scale Force Logs", color = Color.LightGray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// --- Screen 8: Camera Computer Vision Monitor ---
@Composable
fun CameraMonitorScreen(vm: GameViewModel) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.navigateTo(7) }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        text = "ESP32-S3 LIVE CV FEED",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Monitoring the card draw center with hardware edge contours.", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated live camera viewport bounding box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, Color(0xFFFFC107), RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        // Bounding Box CV contours simulation
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                        // Top pile zone
                        drawRect(
                            color = Color(0xFF00E676),
                            style = Stroke(width = 3f, pathEffect = dashEffect),
                            size = Size(180.dp.toPx(), 220.dp.toPx()),
                            topLeft = Offset(size.width / 2 - 90.dp.toPx(), size.height / 2 - 110.dp.toPx())
                        )

                        // Detected Card outline highlight
                        drawRect(
                            color = Color(0xFFFF5722),
                            style = Stroke(width = 5f),
                            size = Size(100.dp.toPx(), 150.dp.toPx()),
                            topLeft = Offset(size.width / 2 - 50.dp.toPx(), size.height / 2 - 75.dp.toPx())
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Red)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("LIVE FEED", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("ESP32 CAM: On", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // CV properties overlay indicators
                        Column {
                            Text("Center Stack status: Centered", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Card Count Weight correlation: Correct", color = Color.LightGray, fontSize = 11.sp)
                            Text("Detected Card: ${vm.lastScannedCard}", color = Color(0xFFFFC107), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CV Calibration values customization
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("CV Card Detect Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(
                                onClick = {
                                    vm.lastScannedCard = "Yellow Draw 2"
                                    vm.logAction("Scanned new item: Yellow Draw 2 via cam.")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2129)),
                                border = BorderStroke(1.dp, Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Scan Yellow Draw 2", color = Color.White)
                            }
                            Button(
                                onClick = {
                                    vm.lastScannedCard = "Wild Draw 4"
                                    vm.logAction("Scanned new item: Wild Draw 4 via cam.")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2129)),
                                border = BorderStroke(1.dp, Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Scan Wild 4", color = Color.White)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { vm.navigateTo(7) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("RETURN GAME DASHBOARD", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Screen 9: Sensors & Log Board ---
@Composable
fun SensorLoggerScreen(vm: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.navigateTo(7) }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        text = "LOAD FORCE SENSOR LOGS",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = { vm.navigateTo(10) }) {
                        Icon(Icons.Filled.VoiceChat, "Microphone settings", tint = Color(0xFFFFC107))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive load cell scale simulator
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Cumulated Pile Mass Scale", color = Color.Gray, fontSize = 12.sp)
                            Text("${vm.loadCellWeight} g", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                            Text("Correlation: ~${(vm.loadCellWeight / 1.5f).toInt()} cards on table", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Simulator add/remove buttons
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    vm.loadCellWeight += 1.5f
                                    vm.logAction("Simulated placing card on scale platform. Weight is ${vm.loadCellWeight}g")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("+1 Card (1.5g)", fontSize = 10.sp)
                            }
                            Button(
                                onClick = {
                                    vm.loadCellWeight = Math.max(0f, vm.loadCellWeight - 1.5f)
                                    vm.logAction("Simulated removing card from scale platform.")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("-1 Card (1.5g)", fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("System Event Streaming Logs:", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(10.dp))

                // Scroll Console View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .border(1.5.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    LazyColumn(
                        reverseLayout = false,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(vm.actionLogs) { log ->
                            Text(
                                text = log,
                                color = if (log.contains("Violation") || log.contains("Alert")) Color.Red else Color(0xFF00FF66),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { vm.navigateTo(7) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("RETURN GAME DASHBOARD", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Screen 10: Microphone & Voice Configuration ---
@Composable
fun VoiceSettingsScreen(vm: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.navigateTo(7) }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        text = "VOICE HOST PERSONALIZATION",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Configure your speaker voice profile, Gemini announcers tone, and mic levels.", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(20.dp))

                // Speaker Tone selection card
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Companion Announcer Tone Type", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Determines the speaking style of the automated gameshow prompts.", color = Color.Gray, fontSize = 11.sp)
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        val tones = listOf("Classic announcer", "Sassy robot", "Deep Wizard", "Chaotic Host")
                        tones.forEach { tone ->
                            val isSelected = vm.voiceToneSelection == tone
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        vm.voiceToneSelection = tone
                                        vm.logAction("Host speech profile changed to: $tone")
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tone, color = Color.White, fontSize = 13.sp)
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        vm.voiceToneSelection = tone
                                        vm.logAction("Host speech profile changed to: $tone")
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFC107))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Voice text announcer simulator
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Trigger Custom Audio Announcement", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { vm.speakAppText("Double Draw rule applied on player three.") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Test Announce!", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { vm.speakAppText("Challenge successful! Play order reversed.") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Test Chaser Reversed", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { vm.navigateTo(7) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("RETURN GAME DASHBOARD", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Screen 11: Game History Stats & Charts ---
@Composable
fun StatsScreen(vm: GameViewModel, stats: List<com.example.data.GameSessionEntity>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.navigateTo(7) }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        text = "HISTORIC MATCH ANALYTICS",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Canvas drawing for the bar statistics
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Average Hand Length per Game Mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Autonomous diagnostic tracking scores.", color = Color.Gray, fontSize = 11.sp)
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            // Draw nice clean charts columns
                            val barWidth = 40.dp.toPx()
                            val spacing = 24.dp.toPx()
                            val data = listOf(80f, 110f, 75f, 95f) // Mock heights/values
                            val mapColors = listOf(Color(0xFFE00010), Color(0xFFFFC107), Color(0xFF2196F3), Color(0xFF4CAF50))
                            val labels = listOf("Classic", "Chaotic", "Dual", "Speed")

                            data.forEachIndexed { idx, heightVal ->
                                val x = idx * (barWidth + spaceToPx(spacing)) + 30f
                                val y = size.height - heightVal
                                drawRect(
                                    color = mapColors[idx],
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, heightVal)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Historic Game Rounds Database:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(10.dp))

                // List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (stats.isEmpty()) {
                        item {
                            Text("No game records persistent yet database. Complete active match on setup 13.", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        items(stats) { item ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129))) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Match Mode: ${item.gameMode}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Timestamp: ${item.startTime}", color = Color.Gray, fontSize = 10.sp)
                                    }
                                    Text("Winner: ${item.winnerName ?: "Open Play"}", color = Color(0xFFFFC107), fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { vm.navigateTo(7) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("RETURN GAME DASHBOARD", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Helpers for the simple spacing math inside canvas
fun spaceToPx(spacing: Float): Float = spacing

// --- Screen 12: Admin Custom General Configurations Screen ---
@Composable
fun SettingsScreen(vm: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.navigateTo(7) }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        text = "ADMIN HARDWARE CONTROL",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Custom system configurations for LED lights speed, microphone levels & hardware sound outputs.", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(20.dp))

                // Brightness configuration slider
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Edge Chaser LED Brightness: ${vm.ledBrightness.toInt()}/255", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Slider(
                            value = vm.ledBrightness,
                            onValueChange = { vm.ledBrightness = it },
                            valueRange = 0f..255f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFFFC107), activeTrackColor = Color(0xFFFFC107))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Microphone sensitivity slider
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Rules Mic Threshold Detection: ${vm.micSensitivity.toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Slider(
                            value = vm.micSensitivity,
                            onValueChange = { vm.micSensitivity = it },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF00FF66), activeTrackColor = Color(0xFF00FF66))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Speaker volume levels
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Hardware Board Speaker Volume: ${vm.appSpeakerVolume.toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Slider(
                            value = vm.appSpeakerVolume,
                            onValueChange = { vm.appSpeakerVolume = it },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF2196F3), activeTrackColor = Color(0xFF2196F3))
                        )
                    }
                }
            }

            Button(
                onClick = { vm.navigateTo(7) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("APPLY SYSTEM SETTINGS", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Screen 13: Win Screen & Star Dust Celebrate ---
@Composable
fun WinLeaderboardScreen(vm: GameViewModel) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Star dust celebration particle vectors animation
    val particlesX by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C1B)) // Deep cosmic space blue
    ) {
        // Draw the celebrated Star Dust animation overlay
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val starColors = listOf(Color(0xFFFFC107), Color(0xFFFF5722), Color(0xFF00E676), Color(0xFF03A9F4), Color(0xFF9C27B0))
            repeat(15) { idx ->
                val sizeVal = (5..15).random().toFloat()
                val offsetAngle = idx * 24.0
                val distance = particlesX + (idx * 15)
                val starX = size.width / 2 + distance * cos(Math.toRadians(offsetAngle)).toFloat() / 2f
                val starY = size.height / 2 + distance * sin(Math.toRadians(offsetAngle)).toFloat() / 2f
                
                drawCircle(
                    color = starColors[idx % starColors.size].copy(alpha = 0.8f),
                    radius = sizeVal,
                    center = Offset(starX, starY)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                Icon(
                    Icons.Filled.EmojiEvents,
                    "VICTORY GOLDEN CUP",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(80.dp)
                )

                Text(
                    text = "SUPREME UNO CHAMPION",
                    color = Color(0xFFFFC107),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = vm.finalWinnerName ?: "Champion",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gemini dynamic comment representation
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, "Gemini Spark", tint = Color(0xFFFFCC00))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gemini Host commentary details:", color = Color(0xFFFFCC00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = vm.lastAiGeneratedRemark,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Scores leaderboard tally list
            Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 14.dp)) {
                Text(
                    text = "Player Score Penalties (Lower wins):",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vm.finalScoresSummary) { (name, penalty) ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(name, color = Color.White, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$penalty pts", color = Color.White, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (penalty == 0) {
                                        Icon(Icons.Filled.Star, "Winner", tint = Color(0xFFFFC107))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    vm.navigateTo(1) // Return back to first Screen setup!
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("play_again_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("PLAY AGAIN / RETURN HOME", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}
