package com.pushoff.domain.repository

import com.pushoff.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user data and authentication
 */
interface AuthRepository {
    val currentUser: Flow<User?>
    
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signUpWithEmail(email: String, password: String, username: String): Result<User>
    suspend fun signOut()
    suspend fun updateElo(userId: String, newElo: Int): Result<Unit>
    suspend fun addXp(userId: String, amount: Long): Result<Unit>
}

/**
 * Repository interface for workout data
 */
interface WorkoutRepository {
    suspend fun saveWorkout(workout: Workout): Result<String>
    suspend fun getWorkouts(userId: String, limit: Int = 20): Flow<List<Workout>>
    suspend fun getWorkoutById(id: String): Result<Workout>
    suspend fun deleteWorkout(id: String): Result<Unit>
}

/**
 * Repository interface for ranked matches and matchmaking
 */
interface MatchmakingRepository {
    suspend fun findMatch(playerElo: Int): Result<String> // Returns match ID
    suspend fun submitMatchResult(match: RankedMatch): Result<Unit>
    suspend fun getMatchStatus(matchId: String): Flow<MatchStatus>
    suspend fun cancelMatchmaking(matchId: String): Result<Unit>
}

sealed class MatchStatus {
    object Searching : MatchStatus()
    data class MatchFound(val opponentId: String, val opponentElo: Int) : MatchStatus()
    data class MatchStarted(val matchId: String) : MatchStatus()
    data class MatchCompleted(val result: RankedMatch) : MatchStatus()
    data class Error(val message: String) : MatchStatus()
}

/**
 * Repository interface for leaderboards
 */
interface LeaderboardRepository {
    suspend fun getGlobalLeaderboard(limit: Int = 50): Flow<List<LeaderboardEntry>>
    suspend fun getUserRank(userId: String): Flow<LeaderboardEntry?>
    suspend fun getFriendsLeaderboard(userIds: List<String>): Flow<List<LeaderboardEntry>>
}

/**
 * Repository interface for achievements
 */
interface AchievementRepository {
    suspend fun getAllAchievements(): List<Achievement>
    suspend fun getUserAchievements(userId: String): Flow<List<UserAchievement>>
    suspend fun unlockAchievement(userId: String, achievementId: String): Result<Unit>
}

/**
 * Repository interface for campaign progress
 */
interface CampaignRepository {
    suspend fun getCampaignLevels(): List<CampaignLevel>
    suspend fun getUserProgress(userId: String): Flow<CampaignProgress>
    suspend fun completeLevel(userId: String, levelId: Int): Result<Unit>
}

data class CampaignProgress(
    val completedLevels: List<Int>,
    val currentLevel: Int,
    val totalStars: Int
)
