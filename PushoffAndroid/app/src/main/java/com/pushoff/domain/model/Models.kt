package com.pushoff.domain.model

/**
 * User model for authentication and profile
 */
data class User(
    val id: String,
    val username: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val xp: Long = 0L,
    val level: Int = 1,
    val elo: Int = 1000, // Starting ELO rating
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getXpForNextLevel(): Long = (level * 100).toLong()
    fun getXpProgress(): Float = (xp % 100).toFloat() / 100f
}

/**
 * Workout session data
 */
data class Workout(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String,
    val type: WorkoutType,
    val duration: Long, // milliseconds
    val reps: Int,
    val validReps: Int,
    val averageFormScore: Float,
    val calories: Int = 0,
    val damageDealt: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class WorkoutType {
    FREE_WORKOUT,
    CAMPAIGN,
    SPEED_CHALLENGE,
    SURVIVAL,
    RANKED_MATCH
}

/**
 * Match result for ranked mode
 */
data class RankedMatch(
    val id: String = java.util.UUID.randomUUID().toString(),
    val player1Id: String,
    val player2Id: String,
    val player1Score: Int, // Total damage dealt
    val player2Score: Int,
    val winnerId: String?,
    val player1Reps: Int,
    val player2Reps: Int,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Leaderboard entry
 */
data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val elo: Int,
    val rank: Int,
    val wins: Int = 0,
    val losses: Int = 0
)

/**
 * Achievement unlocked by user
 */
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconResId: Int,
    val xpReward: Int = 50
)

data class UserAchievement(
    val achievement: Achievement,
    val unlockedAt: Long
)

/**
 * Campaign level/boss data
 */
data class CampaignLevel(
    val id: Int,
    val name: String,
    val bossName: String,
    val bossHp: Int,
    val requiredReps: Int,
    val difficulty: Difficulty
)

enum class Difficulty {
    EASY,
    NORMAL,
    HARD,
    LEGENDARY
}

/**
 * Speed challenge mode settings
 */
data class SpeedChallenge(
    val durationSeconds: Int = 60,
    val targetReps: Int = 30
)

/**
 * Survival mode settings
 */
data class SurvivalMode(
    val initialBossHp: Int = 5000,
    val bossDamagePerSecond: Int = 10,
    val playerMaxHp: Int = 100
)

/**
 * Compact workout signature for anti-cheat validation
 */
data class WorkoutSignature(
    val sessionId: String,
    val timestamp: Long,
    val repTimestamps: List<Long>,
    val repDurations: List<Long>,
    val landmarkConfidence: List<Float>,
    val formScores: List<Float>,
    val deviceAttestation: String? = null,
    val cryptographicHash: String
) {
    companion object {
        fun generateHash(workout: Workout, repTimestamps: List<Long>): String {
            val data = "${workout.id}${workout.reps}${workout.duration}${repTimestamps.joinToString()}"
            return java.security.MessageDigest.getInstance("SHA-256")
                .digest(data.toByteArray())
                .joinToString("") { "%02x".format(it) }
        }
    }
}
