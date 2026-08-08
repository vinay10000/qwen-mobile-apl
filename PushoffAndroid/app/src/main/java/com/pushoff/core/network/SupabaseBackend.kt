package com.pushoff.core.network

import io.github.jan.godtools.restfulbody.RestfulBody
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.authenticated
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Supabase backend client for authentication, database, and realtime features.
 */
class SupabaseBackend(
    supabaseUrl: String,
    supabaseKey: String
) {
    private val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey
    ) {
        install(Auth)
        install(Postgrest)
    }
    
    // Auth operations
    suspend fun signUp(email: String, password: String, username: String): Result<UserData> {
        return try {
            val authResult = client.auth.signUpWith(io.github.jan.supabase.gotrue.provider.Email) {
                this.email = email
                this.password = password
            }
            
            // Store additional user data
            val userId = authResult.user?.id ?: throw Exception("User ID not found")
            
            // Insert user profile
            client.from("users").insert(
                mapOf(
                    "id" to userId,
                    "username" to username,
                    "email" to email,
                    "xp" to 0,
                    "level" to 1,
                    "elo" to 1000
                )
            )
            
            Result.success(
                UserData(
                    id = userId,
                    username = username,
                    email = email,
                    xp = 0,
                    level = 1,
                    elo = 1000
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<UserData> {
        return try {
            val authResult = client.auth.signInWith(io.github.jan.supabase.gotrue.provider.Email) {
                this.email = email
                this.password = password
            }
            
            val userId = authResult.user?.id ?: throw Exception("User ID not found")
            
            // Fetch user profile
            val response = client.from("users")
                .select(columns = Columns.list("id", "username", "email", "xp", "level", "elo"))
                .eq("id", userId)
                .single()
                .decodeSingle<UserData>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signOut() {
        client.auth.signOut()
    }
    
    fun getCurrentUser(): UserData? {
        return client.auth.currentUserOrNull()?.let { authUser ->
            UserData(
                id = authUser.id,
                username = authUser.userMetadata["username"]?.toString() ?: "Unknown",
                email = authUser.email,
                xp = 0,
                level = 1,
                elo = 1000
            )
        }
    }
    
    // Workout operations
    suspend fun saveWorkout(workout: WorkoutData): Result<String> {
        return try {
            val currentUser = getCurrentUser() ?: throw Exception("Not authenticated")
            
            val response = client.from("workouts").insert(
                mapOf(
                    "user_id" to currentUser.id,
                    "type" to workout.type.name,
                    "duration" to workout.duration,
                    "reps" to workout.reps,
                    "valid_reps" to workout.validReps,
                    "average_form_score" to workout.averageFormScore,
                    "calories" to workout.calories,
                    "damage_dealt" to workout.damageDealt
                )
            )
            
            val workoutId = response.first()["id"] as? String ?: throw Exception("Failed to save workout")
            Result.success(workoutId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserWorkouts(userId: String, limit: Int = 50): List<WorkoutData> {
        return try {
            val response = client.from("workouts")
                .select()
                .eq("user_id", userId)
                .order("created_at", Postgrest.SortOrder.DESCENDING)
                .limit(limit)
                .decodeList<WorkoutData>()
            
            response
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Ranked match operations
    suspend fun saveMatch(match: MatchData): Result<String> {
        return try {
            val response = client.from("matches").insert(
                mapOf(
                    "player1_id" to match.player1Id,
                    "player2_id" to match.player2Id,
                    "player1_score" to match.player1Score,
                    "player2_score" to match.player2Score,
                    "winner_id" to match.winnerId,
                    "player1_reps" to match.player1Reps,
                    "player2_reps" to match.player2Reps
                )
            )
            
            val matchId = response.first()["id"] as? String ?: throw Exception("Failed to save match")
            Result.success(matchId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateElo(userId: String, newElo: Int): Result<Unit> {
        return try {
            client.from("users")
                .update(mapOf("elo" to newElo))
                .eq("id", userId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Leaderboard operations
    suspend fun getLeaderboard(limit: Int = 100): List<LeaderboardEntryData> {
        return try {
            val response = client.from("users")
                .select(columns = Columns.list("id", "username", "elo"))
                .order("elo", Postgrest.SortOrder.DESCENDING)
                .limit(limit)
                .decodeList<LeaderboardEntryData>()
            
            response.mapIndexed { index, entry ->
                entry.copy(rank = index + 1)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getPlayerRank(userId: String): Int? {
        return try {
            // Get all players ordered by ELO
            val response = client.from("users")
                .select(columns = Columns.list("id"))
                .order("elo", Postgrest.SortOrder.DESCENDING)
                .decodeList<Map<String, String>>()
            
            response.indexOfFirst { it["id"] == userId } + 1
        } catch (e: Exception) {
            null
        }
    }
    
    // Matchmaking - find opponent within ELO range
    suspend fun findOpponent(playerElo: Int, eloRange: Int): MatchmakingResult? {
        return try {
            val minElo = playerElo - eloRange
            val maxElo = playerElo + eloRange
            
            val currentUser = getCurrentUser() ?: return null
            
            val response = client.from("users")
                .select(columns = Columns.list("id", "username", "elo"))
                .gte("elo", minElo)
                .lte("elo", maxElo)
                .neq("id", currentUser.id)
                .order("elo", Postgrest.SortOrder.ASCENDING)
                .limit(1)
                .maybeSingle()
                .decodeSingle<LeaderboardEntryData>()
            
            response?.let {
                MatchmakingResult(
                    opponentId = it.id,
                    opponentUsername = it.username,
                    opponentElo = it.elo
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}

@Serializable
data class UserData(
    val id: String,
    val username: String,
    val email: String? = null,
    val xp: Long = 0L,
    val level: Int = 1,
    val elo: Int = 1000
)

@Serializable
data class WorkoutData(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val type: com.pushoff.domain.model.WorkoutType = com.pushoff.domain.model.WorkoutType.FREE_WORKOUT,
    val duration: Long = 0L,
    val reps: Int = 0,
    @SerialName("valid_reps") val validReps: Int = 0,
    @SerialName("average_form_score") val averageFormScore: Float = 0f,
    val calories: Int = 0,
    @SerialName("damage_dealt") val damageDealt: Int = 0,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class MatchData(
    val id: String = "",
    @SerialName("player1_id") val player1Id: String = "",
    @SerialName("player2_id") val player2Id: String = "",
    @SerialName("player1_score") val player1Score: Int = 0,
    @SerialName("player2_score") val player2Score: Int = 0,
    @SerialName("winner_id") val winnerId: String? = null,
    @SerialName("player1_reps") val player1Reps: Int = 0,
    @SerialName("player2_reps") val player2Reps: Int = 0,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class LeaderboardEntryData(
    val id: String = "",
    val username: String = "",
    val elo: Int = 0,
    val rank: Int = 0
)

data class MatchmakingResult(
    val opponentId: String,
    val opponentUsername: String,
    val opponentElo: Int
)
