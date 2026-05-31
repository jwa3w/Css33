package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {

    val allPlayers: Flow<List<PlayerEntity>> = gameDao.getAllPlayersFlow()
    val allSessions: Flow<List<GameSessionEntity>> = gameDao.getAllSessionsFlow()
    val wildcardRules: Flow<List<WildcardRuleEntity>> = gameDao.getWildcardRulesFlow()

    suspend fun getPlayersList(): List<PlayerEntity> = gameDao.getAllPlayers()

    suspend fun addPlayer(player: PlayerEntity): Long {
        return gameDao.insertPlayer(player)
    }

    suspend fun updatePlayer(player: PlayerEntity) {
        gameDao.updatePlayer(player)
    }

    suspend fun removePlayer(player: PlayerEntity) {
        gameDao.deletePlayer(player)
    }

    suspend fun resetPlayers() {
        gameDao.clearAllPlayers()
    }

    suspend fun createSession(session: GameSessionEntity): Long {
        return gameDao.insertSession(session)
    }

    suspend fun updateSession(session: GameSessionEntity) {
        gameDao.updateSession(session)
    }

    suspend fun addWildcardRule(rule: WildcardRuleEntity): Long {
        return gameDao.insertWildcardRule(rule)
    }

    suspend fun updateWildcardRule(rule: WildcardRuleEntity) {
        gameDao.updateWildcardRule(rule)
    }

    suspend fun ensureDefaultWildcardsExist() {
        if (gameDao.getRulesCount() == 0) {
            val defaults = listOf(
                WildcardRuleEntity(
                    name = "Light Chaser Storm",
                    description = "When played, LEDs flash rapidly. The next player must play a wildcard as well or draw 2 cards.",
                    actionType = "CHASER_LIGHTS",
                    voicePrompt = "Light storm warning! Next player, play a wildcard or draw two cards!"
                ),
                WildcardRuleEntity(
                    name = "Mute Mimic Challenge",
                    description = "Everyone must play in absolute silence. Speaking leads to a 2-card penalty, monitored via the ESP32 MIC.",
                    actionType = "SOUND_EFFECT",
                    voicePrompt = "Silence active! Anyone who speaks gets a two card load cell penalty!"
                ),
                WildcardRuleEntity(
                    name = "Reverse Roulette",
                    description = "Change play directions dynamically. LED arrows swap motion and a sound effect triggers.",
                    actionType = "REVERSE",
                    voicePrompt = "Direction reversed! The chaser lights are now rushing the opposite way."
                ),
                WildcardRuleEntity(
                    name = "Weight Watcher Alert",
                    description = "Load cell checks player hand weight. If weight error detected, skip turn.",
                    actionType = "SKIP_TURN",
                    voicePrompt = "Weight checkpoint triggered! Put down your target card or skip your entire turn."
                )
            )
            for (rule in defaults) {
                gameDao.insertWildcardRule(rule)
            }
        }
    }
}
