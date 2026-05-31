package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // --- Players ---
    @Query("SELECT * FROM players ORDER BY playOrder ASC")
    fun getAllPlayersFlow(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players ORDER BY playOrder ASC")
    suspend fun getAllPlayers(): List<PlayerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Delete
    suspend fun deletePlayer(player: PlayerEntity)

    @Query("DELETE FROM players")
    suspend fun clearAllPlayers()

    // --- Game Sessions ---
    @Query("SELECT * FROM game_sessions ORDER BY startTime DESC")
    fun getAllSessionsFlow(): Flow<List<GameSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSessionEntity): Long

    @Update
    suspend fun updateSession(session: GameSessionEntity)

    // --- Wildcard Rules ---
    @Query("SELECT * FROM wildcard_rules")
    fun getWildcardRulesFlow(): Flow<List<WildcardRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWildcardRule(rule: WildcardRuleEntity): Long

    @Update
    suspend fun updateWildcardRule(rule: WildcardRuleEntity)

    @Query("SELECT COUNT(*) FROM wildcard_rules")
    suspend fun getRulesCount(): Int
}
