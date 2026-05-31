package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = GameRepository(db.gameDao())

    // --- State Navigation ---
    var currentScreen by mutableStateOf(1) // 1 to 13
        private set

    fun navigateTo(screenId: Int) {
        if (screenId in 1..13) {
            currentScreen = screenId
        }
    }

    // --- Database Streams ---
    val allPlayersStream = repository.allPlayers
    val allSessionsStream = repository.allSessions
    val wildcardRulesStream = repository.wildcardRules

    // --- Wi-Fi Connection State (Screen 2) ---
    var wifiSsid by mutableStateOf("")
    var wifiPassword by mutableStateOf("")
    var isWifiScanning by mutableStateOf(false)
    var isEsp32Connected by mutableStateOf(false)
    var esp32IpAddress by mutableStateOf("192.168.4.1")
    var ssidChangeStatus by mutableStateOf("Idle") // Idle, Scanning, Connecting, Success
    var routerModeActive by mutableStateOf(true)

    // --- Player Management UI State (Screen 3) ---
    var newPlayerName by mutableStateOf("")
    var newPlayerColorHex by mutableStateOf("#FF3B30") // Default iOS Red
    var isRecordingName by mutableStateOf(false)
    var recordingTimer by mutableStateOf(0)

    // --- Dealer Selection State (Screen 4) ---
    var selectedDealerId by mutableStateOf<Int?>(null)
    var isSpinningDealer by mutableStateOf(false)

    // --- Game Rules State (Screen 5 & 7) ---
    var gameMode by mutableStateOf("Classic") // Classic, Chaotic, Dual, Speed
    var targetScoreLimit by mutableStateOf(500)
    var selectedRulesIds = mutableStateOf<Set<Int>>(emptySet())

    // --- Active Play Core State (Screen 7, 8, 9, 13) ---
    var activePlayers = mutableListOf<PlayerEntity>()
    var currentTurnIndex by mutableStateOf(0)
    val currentTurnPlayer: PlayerEntity?
        get() = activePlayers.getOrNull(currentTurnIndex)

    var gameDirectionClockwise by mutableStateOf(true) // True = CW, False = CCW
    var activeCardColor by mutableStateOf("Red") // Red, Yellow, Blue, Green, Multi
    var activeCardValue by mutableStateOf("7") // numbers, Draw 2, Skip, Reverse, Wild, Wild Draw 4
    val actionLogs = mutableListOf<String>()

    // --- Camera Computer Vision State (Screen 8) ---
    var cameraActive by mutableStateOf(true)
    var cvStatusText by mutableStateOf("Detecting ready...")
    var lastScannedCard by mutableStateOf("Red 7")
    var detectedRuleViolation by mutableStateOf(false)
    var detectedRuleViolationMsg by mutableStateOf("")

    // --- Load Cell Sensor State (Screen 9) ---
    var loadCellWeight by mutableStateOf(45.5f) // Total weight of stack in grams (approx 1.5g per card)
    var tareWeight by mutableStateOf(0f)
    var sensorStateMsg by mutableStateOf("Sensors stable. 30 cards on stack.")

    // --- Voice, Mic & TTS State (Screen 10) ---
    var voiceToneSelection by mutableStateOf("Classic announcer") // Sassy, Chill, Chaotic, announcer
    var appSpeechText by mutableStateOf("Greeting! Let's get started.")
    var lastAiGeneratedRemark by mutableStateOf("Welcome to UNO Whose Turn! Let the battle begin.")
    var isGeneratingSpeech by mutableStateOf(false)

    // --- Hardware Controls State (Screen 12) ---
    var ledBrightness by mutableStateOf(150f) // 0-255
    var ledChaserSpeed by mutableStateOf(3f) // Speed factor
    var micSensitivity by mutableStateOf(70f) // Threshold
    var appSpeakerVolume by mutableStateOf(80f)

    // --- Winner Leaderboard State (Screen 13) ---
    var finalWinnerName by mutableStateOf<String?>(null)
    var finalScoresSummary by mutableStateOf<List<Pair<String, Int>>>(emptyList())

    // --- OkHttp Client for Direct Gemini API (Option B) ---
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    init {
        viewModelScope.launch {
            repository.ensureDefaultWildcardsExist()
            // Observe wildcard rules to auto-check them.
            wildcardRulesStream.collectLatest { rules ->
                if (selectedRulesIds.value.isEmpty()) {
                    selectedRulesIds.value = rules.take(4).map { it.id }.toSet()
                }
            }
        }
    }

    // --- Methods for WiFi setup (Screen 2) ---
    fun scanQrCodeSsidAndConnect(qrContent: String) {
        viewModelScope.launch {
            ssidChangeStatus = "Scanning QR Code..."
            delay(1500)
            if (qrContent.startsWith("WIFI:S:") || qrContent.contains(";")) {
                // Parse SSID and PW
                val ssidPart = qrContent.substringAfter("S:").substringBefore(";")
                val passPart = qrContent.substringAfter("P:").substringBefore(";")
                wifiSsid = ssidPart
                wifiPassword = passPart
            } else {
                wifiSsid = "ESP32_S3_UNO_AP"
                wifiPassword = "UNOWhoTurn123"
            }
            ssidChangeStatus = "SSID Auto-changing..."
            delay(2000)
            isEsp32Connected = true
            ssidChangeStatus = "Successfully Connected to ESP32!"
            logAction("Connected to Device router: $wifiSsid")
            speakAppText("System matched ESP32 WiFi Router")
        }
    }

    // --- Add/Remove player (Screen 3) ---
    fun registerPlayer() {
        if (newPlayerName.trim().isEmpty()) return
        viewModelScope.launch {
            val count = activePlayers.size
            val newPlayer = PlayerEntity(
                name = newPlayerName,
                colorHex = newPlayerColorHex,
                isAdmin = count == 0, // First joined player is designated as Admin
                playOrder = count,
                activeHandSize = 7
            )
            repository.addPlayer(newPlayer)
            logAction("Player ${newPlayer.name} entered as ${if (newPlayer.isAdmin) "Admin" else "Player"}")
            newPlayerName = ""
        }
    }

    fun startRecordingVoiceName() {
        if (isRecordingName) return
        isRecordingName = true
        recordingTimer = 0
        viewModelScope.launch {
            while (isRecordingName && recordingTimer < 3) {
                delay(1000)
                recordingTimer++
            }
            stopRecordingVoiceName()
        }
    }

    fun stopRecordingVoiceName() {
        if (!isRecordingName) return
        isRecordingName = false
        logAction("Voice capture recorded for local speech.")
    }

    // --- Dealer picker actions (Screen 4) ---
    fun triggerSpinDealer() {
        if (activePlayers.isEmpty()) return
        isSpinningDealer = true
        viewModelScope.launch {
            var highlightIndex = 0
            repeat(15) {
                highlightIndex = (activePlayers.indices).random()
                selectedDealerId = activePlayers[highlightIndex].id
                delay(150 + it * 20L)
            }
            isSpinningDealer = false
            val dealer = activePlayers.firstOrNull { it.id == selectedDealerId }
            logAction("Dealer designated: ${dealer?.name}")
            speakAppText("The designated dealer is ${dealer?.name}")
            delay(1500)
            navigateTo(5) // Move to page 5 custom settings
        }
    }

    // --- Active Play Core Loops ---
    fun startGamePlay() {
        if (activePlayers.isEmpty()) return
        currentTurnIndex = 0
        gameDirectionClockwise = true
        activeCardColor = "Red"
        activeCardValue = "7"
        logAction("Game started in $gameMode mode! First to play: ${currentTurnPlayer?.name}")
        speakAppText("Battle sequence initiated. Turn belongs to ${currentTurnPlayer?.name}")
    }

    fun advanceTurn() {
        if (activePlayers.isEmpty()) return
        val current = currentTurnPlayer ?: return
        
        // Simulating Card placement and weight checks
        viewModelScope.launch {
            // Decrease card count from player hand, increase weight of stack
            val oldWeight = loadCellWeight
            loadCellWeight += 1.5f // Each card is approx 1.5 grams
            
            val updatedPlayers = activePlayers.mapIndexed { index, p ->
                if (index == currentTurnIndex) {
                    val newHandSize = Math.max(0, p.activeHandSize - 1)
                    if (newHandSize == 0) {
                        triggerPlayerWin(p)
                    }
                    p.copy(activeHandSize = newHandSize)
                } else p
            }
            activePlayers.clear()
            activePlayers.addAll(updatedPlayers)

            // Dynamic announce
            logAction("${current.name} played card. Pile weight: ${loadCellWeight}g")
            
            // Advance counter depending on direction
            val nextIndex = if (gameDirectionClockwise) {
                (currentTurnIndex + 1) % activePlayers.size
            } else {
                var prev = currentTurnIndex - 1
                if (prev < 0) prev = activePlayers.size - 1
                prev
            }
            
            currentTurnIndex = nextIndex
            speakAppText("Now turn for ${currentTurnPlayer?.name}")
        }
    }

    fun triggerPlayerDrawCard(playerIndex: Int) {
        if (playerIndex !in activePlayers.indices) return
        val player = activePlayers[playerIndex]
        
        viewModelScope.launch {
            loadCellWeight -= 1.5f // Draw card decreases stack weight
            val updatedPlayers = activePlayers.mapIndexed { index, p ->
                if (index == playerIndex) {
                    p.copy(activeHandSize = p.activeHandSize + 1)
                } else p
            }
            activePlayers.clear()
            activePlayers.addAll(updatedPlayers)
            logAction("${player.name} drew a card. Stack weight: ${loadCellWeight}g")
            speakAppText("${player.name} draws one item.")
        }
    }

    fun handleRuleViolation(player: PlayerEntity, msg: String) {
        detectedRuleViolation = true
        detectedRuleViolationMsg = "$msg by ${player.name}!"
        logAction("Violation alert: ${player.name} $msg")
        speakAppText("Alert! Rule violation by ${player.name}. Double card penalty!")
        
        // Apply card penalties via weight decrease and hand increase
        viewModelScope.launch {
            loadCellWeight -= 3.0f
            val updatedPlayers = activePlayers.mapIndexed { index, p ->
                if (p.id == player.id) {
                    p.copy(activeHandSize = p.activeHandSize + 2)
                } else p
            }
            activePlayers.clear()
            activePlayers.addAll(updatedPlayers)
            delay(4000)
            detectedRuleViolation = false
        }
    }

    fun triggerPlayerWin(player: PlayerEntity) {
        finalWinnerName = player.name
        val summary = activePlayers.map { it.name to (it.activeHandSize * 15) }
        finalScoresSummary = summary.sortedBy { it.second }
        
        viewModelScope.launch {
            // Save Session in database
            val session = GameSessionEntity(
                endTime = System.currentTimeMillis(),
                winnerName = player.name,
                gameMode = gameMode,
                isCompleted = true
            )
            repository.createSession(session)
            logAction("Match won by ${player.name}!")
            speakAppText("All hail the supreme champion, ${player.name}!")
            
            // Generate customized speech congratulation using Gemini
            generatePersonalizedWinnerSpeech(player.name)
            
            navigateTo(13) // Nav to Winner leaderboards
        }
    }

    fun changePlayDirection() {
        gameDirectionClockwise = !gameDirectionClockwise
        logAction("Direction changed! Speed chaser running ${if (gameDirectionClockwise) "Clockwise" else "Counter-Clockwise"}")
        speakAppText("Warning, reverse flow active. Chaser reversed!")
    }

    fun triggerCustomWildcardEffect(ruleName: String, voiceText: String) {
        logAction("Wildcard triggered: $ruleName")
        speakAppText(voiceText)
    }

    // --- Gemini Interactive Voice Prompt ---
    fun speakAppText(text: String) {
        appSpeechText = text
        // Announce via log
        logAction("[App Announcement]: $text")
    }

    private suspend fun generatePersonalizedWinnerSpeech(winnerName: String) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
            lastAiGeneratedRemark = "Outstanding victory $winnerName! Your card-slinging wizardry has completely demolished your rivals. What a legendary performance."
            return
        }

        isGeneratingSpeech = true
        val prompt = "Write a short, highly fun, energetic, personalized 2-sentence trash-talk styled victory comment for an UNO game. The winner is named $winnerName. Match played in $gameMode mode. Frame this as a cool, animated voice assistant host."
        
        withContext(Dispatchers.IO) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val jsonRequest = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                }

                val body = jsonRequest.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val respBody = response.body?.string() ?: ""
                        val obj = JSONObject(respBody)
                        val textResult = obj.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")
                        
                        withContext(Dispatchers.Main) {
                            lastAiGeneratedRemark = textResult.trim()
                            speakAppText(lastAiGeneratedRemark)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            lastAiGeneratedRemark = "Incredible play $winnerName! You absolutely dominated this classic match of UNO. Incredible reflexes."
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    lastAiGeneratedRemark = "Perfect victory $winnerName! You outsmarted every other player with extreme ease."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isGeneratingSpeech = false
                }
            }
        }
    }

    fun generateFunAiWildcardIdea() {
        val apiKey = BuildConfig.GEMINI_API_KEY
        viewModelScope.launch {
            isGeneratingSpeech = true
            val prompt = "Generate exactly one creative, wacky UNO game wildcard rule title and description. It must be unique, funny, and sound like a rule a robotic gameshow host would announce. Format like this: Title: [Title Text] | Description: [Description Text]"
            
            withContext(Dispatchers.IO) {
                try {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                    val jsonRequest = JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", prompt)
                                    })
                                })
                            })
                        })
                    }

                    val body = jsonRequest.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url(url)
                        .post(body)
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val respBody = response.body?.string() ?: ""
                            val obj = JSONObject(respBody)
                            val textResult = obj.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text")

                            withContext(Dispatchers.Main) {
                                val cleanText = textResult.trim()
                                val newTitle = cleanText.substringBefore("|").replace("Title:", "").trim()
                                val newDesc = cleanText.substringAfter("|").replace("Description:", "").trim()
                                
                                val ruleEntity = WildcardRuleEntity(
                                    name = if(newTitle.isNotEmpty()) newTitle else "Universal Trade Session",
                                    description = if(newDesc.isNotEmpty()) newDesc else "All players swap hands with their neighbor.",
                                    actionType = listOf("CHASER_LIGHTS", "SOUND_EFFECT", "REVERSE", "SKIP_TURN").random(),
                                    voicePrompt = newDesc,
                                    isEnabled = true
                                )
                                repository.addWildcardRule(ruleEntity)
                                logAction("AI Generated Rule Added: ${ruleEntity.name}")
                                speakAppText("Generated Wildcard Rule: ${ruleEntity.name}")
                            }
                        } else {
                            fallbackRuleAddition()
                        }
                    }
                } catch (e: Exception) {
                    fallbackRuleAddition()
                } finally {
                    withContext(Dispatchers.Main) {
                        isGeneratingSpeech = false
                    }
                }
            }
        }
    }

    private suspend fun fallbackRuleAddition() {
        withContext(Dispatchers.Main) {
            val rule = WildcardRuleEntity(
                name = "Chaos Card Switch",
                description = "Everyone hands their cards to the left!",
                actionType = "SOUND_EFFECT",
                voicePrompt = "Hand swap triggered! Swap all cards with your neighbor immediately!",
                isEnabled = true
            )
            repository.addWildcardRule(rule)
            logAction("Added Fallback wild rule: ${rule.name}")
        }
    }

    fun logAction(msg: String) {
        actionLogs.add(0, "[${System.currentTimeMillis() % 100000}] $msg")
        if (actionLogs.size > 50) {
            actionLogs.removeLast()
        }
    }

    fun playRecordedVoiceLog(name: String) {
        logAction("Playing custom audio playback template for speaker option: '$name'")
        speakAppText("This is the custom captured voice profile for $name.")
    }
}
