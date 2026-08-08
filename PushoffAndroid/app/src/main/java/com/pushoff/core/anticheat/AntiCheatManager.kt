package com.pushoff.core.anticheat

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.pushoff.domain.model.Workout
import com.pushoff.domain.usecase.ValidateWorkoutSignature
import kotlinx.coroutines.tasks.await

/**
 * Anti-cheat system using Play Integrity API and workout signature validation.
 */
class AntiCheatManager(private val context: Context) {
    
    private val integrityManager = IntegrityManagerFactory.create(context)
    private val workoutValidator = ValidateWorkoutSignature()
    
    /**
     * Request Play Integrity token for device/app verification
     */
    suspend fun requestIntegrityToken(): IntegrityResult {
        return try {
            val tokenRequest = integrityManager.integrityTokenRequest {
                // Optional: Add nonce for replay protection
                nonce = java.util.UUID.randomUUID().toString()
            }
            
            val tokenResponse = integrityManager.requestIntegrityToken(tokenRequest).await()
            val token = tokenResponse.token()
            
            IntegrityResult.Success(token)
        } catch (e: Exception) {
            IntegrityResult.Failure(e)
        }
    }
    
    /**
     * Verify device integrity before allowing ranked mode
     */
    suspend fun verifyDeviceIntegrity(): DeviceIntegrityStatus {
        return try {
            val result = requestIntegrityToken()
            
            when (result) {
                is IntegrityResult.Success -> {
                    // In production, send token to backend for verification
                    // Backend would decode and verify with Google Play Integrity API
                    DeviceIntegrityStatus.Verified(result.token)
                }
                is IntegrityResult.Failure -> {
                    DeviceIntegrityStatus.Failed(result.exception.message ?: "Unknown error")
                }
            }
        } catch (e: Exception) {
            DeviceIntegrityStatus.Failed(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Validate workout data for cheating indicators
     */
    fun validateWorkout(workout: Workout, repTimestamps: List<Long>): WorkoutValidationResult {
        val signatureValidation = workoutValidator.isSuspicious(workout, repTimestamps)
        
        return WorkoutValidationResult(
            isValid = signatureValidation.isValid,
            issues = signatureValidation.issues,
            riskLevel = signatureValidation.riskLevel
        )
    }
    
    /**
     * Generate secure workout signature for server verification
     */
    fun generateWorkoutSignature(
        workout: Workout,
        repTimestamps: List<Long>,
        repDurations: List<Long>,
        formScores: List<Float>
    ): String {
        val data = buildString {
            append(workout.id)
            append("|")
            append(workout.reps)
            append("|")
            append(workout.duration)
            append("|")
            append(repTimestamps.joinToString(","))
            append("|")
            append(repDurations.joinToString(","))
            append("|")
            append(formScores.joinToString(",") { "%.2f".format(it) })
            append("|")
            append(System.currentTimeMillis())
        }
        
        return java.security.MessageDigest.getInstance("SHA-256")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Check if user can access ranked mode based on integrity status
     */
    suspend fun canAccessRankedMode(): RankedModeAccess {
        val integrityStatus = verifyDeviceIntegrity()
        
        return when (integrityStatus) {
            is DeviceIntegrityStatus.Verified -> {
                RankedModeAccess.Granted
            }
            is DeviceIntegrityStatus.Failed -> {
                RankedModeAccess.Denied(integrityStatus.reason)
            }
        }
    }
}

sealed class IntegrityResult {
    data class Success(val token: String) : IntegrityResult()
    data class Failure(val exception: Exception) : IntegrityResult()
}

sealed class DeviceIntegrityStatus {
    data class Verified(val token: String) : DeviceIntegrityStatus()
    data class Failed(val reason: String) : DeviceIntegrityStatus()
}

data class WorkoutValidationResult(
    val isValid: Boolean,
    val issues: List<String>,
    val riskLevel: com.pushoff.domain.usecase.RiskLevel
)

sealed class RankedModeAccess {
    object Granted : RankedModeAccess()
    data class Denied(val reason: String) : RankedModeAccess()
}

/**
 * Detect common cheat patterns in workout data
 */
object CheatPatternDetector {
    
    /**
     * Detect mechanically perfect timing (bot-like behavior)
     */
    fun detectMechanicalTiming(repTimestamps: List<Long>): Boolean {
        if (repTimestamps.size < 5) return false
        
        val intervals = repTimestamps.zipWithNext { a, b -> b - a }
        val avgInterval = intervals.average()
        val variance = intervals.map { kotlin.math.abs(it - avgInterval) }.average()
        
        // Humans have natural variation; bots are too consistent
        return variance < 30.0 // Less than 30ms variance is suspicious
    }
    
    /**
     * Detect impossible rep counts
     */
    fun detectImpossibleRepCount(reps: Int, durationMs: Long): Boolean {
        val durationMinutes = durationMs / 60000.0
        val maxRepsPerMinute = 40.0 // World record pace
        
        return reps > (durationMinutes * maxRepsPerMinute * 1.2) // 20% buffer
    }
    
    /**
     * Detect form score anomalies
     */
    fun detectFormAnomalies(formScores: List<Float>): Boolean {
        if (formScores.isEmpty()) return false
        
        val average = formScores.average()
        val allPerfect = formScores.all { it > 98f }
        
        // All perfect scores across many reps is suspicious
        return allPerfect && formScores.size > 20
    }
    
    /**
     * Detect rapid-fire reps (no recovery time)
     */
    fun detectRapidFireReps(repTimestamps: List<Long>): Boolean {
        if (repTimestamps.size < 3) return false
        
        val intervals = repTimestamps.zipWithNext { a, b -> b - a }
        val minInterval = 800L // Minimum realistic interval in ms
        
        return intervals.any { it < minInterval * 0.5 } // Less than half minimum
    }
}
