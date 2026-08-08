package com.pushoff.domain.usecase

import com.pushoff.domain.model.*
import com.pushoff.domain.repository.*
import kotlinx.coroutines.flow.Flow

/**
 * Calculate ELO rating changes after a ranked match
 * Uses standard ELO formula: K-factor = 32 for competitive games
 */
class CalculateEloChange {
    private val kFactor = 32
    
    /**
     * Calculate expected score based on current ratings
     * EA = 1 / (1 + 10^((RB - RA)/400))
     */
    fun calculateExpectedScore(playerRating: Int, opponentRating: Int): Float {
        return 1f / (1f + kotlin.math.pow(10.0, (opponentRating - playerRating) / 400.0)).toFloat()
    }
    
    /**
     * Calculate new rating after match
     * New Rating = Old Rating + K × (Actual - Expected)
     */
    fun calculateNewRating(
        oldRating: Int,
        opponentRating: Int,
        won: Boolean
    ): Int {
        val expectedScore = calculateExpectedScore(oldRating, opponentRating)
        val actualScore = if (won) 1f else 0f
        
        val ratingChange = (kFactor * (actualScore - expectedScore)).toInt()
        return (oldRating + ratingChange).coerceAtLeast(0)
    }
    
    /**
     * Calculate rating changes for both players
     */
    fun calculateMatchRatings(
        player1Rating: Int,
        player2Rating: Int,
        player1Won: Boolean
    ): Pair<Int, Int> {
        val player1New = calculateNewRating(player1Rating, player2Rating, player1Won)
        val player2New = calculateNewRating(player2Rating, player1Rating, !player1Won)
        
        return Pair(player1New, player2New)
    }
}

/**
 * Validate workout data for anti-cheat
 */
class ValidateWorkoutSignature {
    
    /**
     * Check if workout data is suspicious
     */
    fun isSuspicious(workout: Workout, repTimestamps: List<Long>): ValidationResult {
        val issues = mutableListOf<String>()
        
        // Check rep count vs duration
        val avgRepTime = workout.duration.toFloat() / workout.reps.coerceAtLeast(1)
        if (avgRepTime < 800f) { // Less than 0.8 seconds per rep
            issues.add("Unrealistic rep speed: ${avgRepTime.toInt()}ms/rep")
        }
        
        // Check for perfect form on all reps (suspicious)
        if (workout.averageFormScore > 98f && workout.reps > 50) {
            issues.add("Suspiciously perfect form across many reps")
        }
        
        // Check rep timestamp consistency
        if (repTimestamps.size > 1) {
            val intervals = repTimestamps.zipWithNext { a, b -> b - a }
            val avgInterval = intervals.average().toLong()
            val variance = intervals.map { kotlin.math.abs(it - avgInterval) }.average()
            
            if (variance < 50L && workout.reps > 20) { // Too consistent timing
                issues.add("Mechanically consistent rep timing")
            }
        }
        
        // Check impossible calorie burn
        val maxCaloriesPerMinute = 15 // Upper limit for push-ups
        val calculatedMaxCalories = (workout.duration / 60000 * maxCaloriesPerMinute).toInt()
        if (workout.calories > calculatedMaxCalories * 1.5f) {
            issues.add("Impossible calorie burn rate")
        }
        
        return ValidationResult(
            isValid = issues.isEmpty(),
            issues = issues,
            riskLevel = when {
                issues.isEmpty() -> RiskLevel.LOW
                issues.size <= 2 -> RiskLevel.MEDIUM
                else -> RiskLevel.HIGH
            }
        )
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val issues: List<String>,
    val riskLevel: RiskLevel
)

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Calculate XP and level progression
 */
class CalculateXpProgress {
    
    fun getXpForLevel(level: Int): Long = (level * 100).toLong()
    
    fun getLevelFromXp(totalXp: Long): Int {
        var level = 1
        var xpRequired = getXpForLevel(level)
        
        while (totalXp >= xpRequired) {
            level++
            xpRequired = getXpForLevel(level)
        }
        
        return level
    }
    
    fun getXpRewards(workout: Workout): XpRewards {
        val baseXp = workout.validReps * 10L
        val formBonus = (workout.averageFormScore / 100f * 20L).toLong()
        val streakBonus = 0L // Would need streak data
        val firstWinBonus = 0L // Would need match data
        
        val totalXp = baseXp + formBonus + streakBonus + firstWinBonus
        
        return XpRewards(
            baseXp = baseXp,
            formBonus = formBonus,
            streakBonus = streakBonus,
            firstWinBonus = firstWinBonus,
            totalXp = totalXp
        )
    }
}

data class XpRewards(
    val baseXp: Long,
    val formBonus: Long,
    val streakBonus: Long,
    val firstWinBonus: Long,
    val totalXp: Long
)

/**
 * Matchmaking logic for finding opponents
 */
class FindOpponent {
    
    data class MatchmakingRange(
        val eloMin: Int,
        val eloMax: Int,
        val waitTimeMs: Long
    )
    
    /**
     * Progressive ELO range expansion for matchmaking
     */
    fun getSearchRanges(playerElo: Int): List<MatchmakingRange> {
        return listOf(
            MatchmakingRange(
                eloMin = playerElo - 100,
                eloMax = playerElo + 100,
                waitTimeMs = 5000
            ),
            MatchmakingRange(
                eloMin = playerElo - 150,
                eloMax = playerElo + 150,
                waitTimeMs = 10000
            ),
            MatchmakingRange(
                eloMin = playerElo - 250,
                eloMax = playerElo + 250,
                waitTimeMs = 15000
            )
        )
    }
    
    /**
     * Check if two players can be matched
     */
    fun canMatch(player1Elo: Int, player2Elo: Int, waitTimeMs: Long): Boolean {
        val ranges = getSearchRanges(player1Elo)
        val applicableRange = ranges.firstOrNull { it.waitTimeMs >= waitTimeMs } 
            ?: ranges.last()
        
        return player2Elo in applicableRange.eloMin..applicableRange.eloMax
    }
}
