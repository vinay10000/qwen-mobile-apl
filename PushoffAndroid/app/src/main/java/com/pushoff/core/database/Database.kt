package com.pushoff.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room database for offline storage of workouts, user data, and achievements.
 */
@Database(
    entities = [
        LocalUser::class,
        LocalWorkout::class,
        LocalMatch::class,
        LocalAchievement::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PushoffDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun matchDao(): MatchDao
    abstract fun achievementDao(): AchievementDao
    
    companion object {
        const val DATABASE_NAME = "pushoff_database"
    }
}

/**
 * User entity for local storage
 */
@Entity(tableName = "users")
data class LocalUser(
    @PrimaryKey val id: String,
    val username: String,
    val email: String?,
    val avatarUrl: String?,
    val xp: Long,
    val level: Int,
    val elo: Int,
    val createdAt: Long
)

/**
 * Workout entity for local storage
 */
@Entity(tableName = "workouts", indices = [Index(value = ["userId"])])
data class LocalWorkout(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // WorkoutType name
    val duration: Long,
    val reps: Int,
    val validReps: Int,
    val averageFormScore: Float,
    val calories: Int,
    val damageDealt: Int,
    val createdAt: Long
)

/**
 * Match entity for local storage
 */
@Entity(tableName = "matches", indices = [Index(value = ["player1Id"]), Index(value = ["player2Id"])])
data class LocalMatch(
    @PrimaryKey val id: String,
    val player1Id: String,
    val player2Id: String,
    val player1Score: Int,
    val player2Score: Int,
    val winnerId: String?,
    val player1Reps: Int,
    val player2Reps: Int,
    val createdAt: Long
)

/**
 * Achievement entity for local storage
 */
@Entity(tableName = "achievements")
data class LocalAchievement(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val description: String,
    val iconResId: Int,
    val xpReward: Int,
    val unlockedAt: Long
)

/**
 * Type converters for Room
 */
class Converters {
    // Add converters if needed for complex types
}

/**
 * User DAO operations
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): LocalUser?
    
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): LocalUser?
    
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUserFlow(): Flow<LocalUser?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: LocalUser)
    
    @Update
    suspend fun updateUser(user: LocalUser)
    
    @Query("UPDATE users SET xp = :xp, level = :level WHERE id = :userId")
    suspend fun updateXpAndLevel(userId: String, xp: Long, level: Int)
    
    @Query("UPDATE users SET elo = :elo WHERE id = :userId")
    suspend fun updateElo(userId: String, elo: Int)
}

/**
 * Workout DAO operations
 */
@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getUserWorkouts(userId: String, limit: Int = 50): List<LocalWorkout>
    
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserWorkoutsFlow(userId: String): Flow<List<LocalWorkout>>
    
    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId")
    suspend fun getWorkoutCount(userId: String): Int
    
    @Query("SELECT SUM(reps) FROM workouts WHERE userId = :userId")
    suspend fun getTotalReps(userId: String): Int?
    
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: String): LocalWorkout?
    
    @Insert
    suspend fun insertWorkout(workout: LocalWorkout)
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkouts(workouts: List<LocalWorkout>)
    
    @Delete
    suspend fun deleteWorkout(workout: LocalWorkout)
}

/**
 * Match DAO operations
 */
@Dao
interface MatchDao {
    @Query("SELECT * FROM matches WHERE player1Id = :userId OR player2Id = :userId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getUserMatches(userId: String, limit: Int = 50): List<LocalMatch>
    
    @Query("SELECT * FROM matches WHERE player1Id = :userId OR player2Id = :userId ORDER BY createdAt DESC")
    fun getUserMatchesFlow(userId: String): Flow<List<LocalMatch>>
    
    @Query("SELECT COUNT(*) FROM matches WHERE (player1Id = :userId OR player2Id = :userId) AND winnerId = :userId")
    suspend fun getWinCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM matches WHERE player1Id = :userId OR player2Id = :userId")
    suspend fun getMatchCount(userId: String): Int
    
    @Insert
    suspend fun insertMatch(match: LocalMatch)
}

/**
 * Achievement DAO operations
 */
@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE userId = :userId")
    suspend fun getUserAchievements(userId: String): List<LocalAchievement>
    
    @Query("SELECT * FROM achievements WHERE userId = :userId")
    fun getUserAchievementsFlow(userId: String): Flow<List<LocalAchievement>>
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievement(achievement: LocalAchievement)
    
    @Query("SELECT COUNT(*) FROM achievements WHERE userId = :userId")
    suspend fun getAchievementCount(userId: String): Int
}
