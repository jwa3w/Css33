package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String,
    val score: Int = 0,
    val recordedVoicePath: String? = null,
    val isAdmin: Boolean = false,
    val playOrder: Int = 0,
    val activeHandSize: Int = 7
)

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val winnerName: String? = null,
    val gameMode: String = "Classic", // Classic, Chaotic, Dual, Speed
    val maxScore: Int = 500,
    val isCompleted: Boolean = false
)

@Entity(tableName = "wildcard_rules")
data class WildcardRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val actionType: String, // "CHASER_LIGHTS", "SOUND_EFFECT", "REVERSE", "SKIP_TURN"
    val voicePrompt: String,
    val isEnabled: Boolean = true
)
