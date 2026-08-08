package com.pushoff.core.pose

import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.atan2
import kotlin.math.toDegrees

/**
 * Core push-up detection engine using MediaPipe Pose Landmarks.
 * Runs entirely on-device for low latency and privacy.
 */
class PushUpDetector {
    
    // Joint indices from MediaPipe Pose Landmarks
    companion object {
        private const val NOSE = 0
        private const val LEFT_SHOULDER = 11
        private const val RIGHT_SHOULDER = 12
        private const val LEFT_ELBOW = 13
        private const val RIGHT_ELBOW = 14
        private const val LEFT_WRIST = 15
        private const val RIGHT_WRIST = 16
        private const val LEFT_HIP = 23
        private const val RIGHT_HIP = 24
        private const val LEFT_KNEE = 25
        private const val RIGHT_KNEE = 26
    }
    
    enum class RepState {
        UP,
        DOWN,
        TRANSITIONING
    }
    
    enum class RepQuality {
        PERFECT,    // 90-100%
        GOOD,       // 75-89%
        WEAK,       // 60-74%
        INVALID     // <60%
    }
    
    data class RepScore(
        val depth: Float,      // 0-100%
        val alignment: Float,  // 0-100%
        val rangeOfMotion: Float, // 0-100%
        val speed: Float,      // 0-100%
        val overall: Float     // 0-100%
    )
    
    data class PushUpRep(
        val repNumber: Int,
        val quality: RepQuality,
        val score: RepScore,
        val timestamp: Long,
        val damage: Int
    )
    
    private var currentState = RepState.UP
    private var repCount = 0
    private var lastRepTime = 0L
    private val minRepDuration = 800L // Minimum 0.8 seconds per rep to prevent cheating
    
    // Thresholds for push-up detection
    private val elbowAngleUpThreshold = 155f // Arms extended
    private val elbowAngleDownThreshold = 100f // Arms bent at bottom
    private val minHipAlignment = 0.85f // Body should be straight
    
    /**
     * Calculate angle between three points (shoulder, elbow, wrist)
     */
    fun calculateElbowAngle(
        shoulder: PoseLandmarkerResult.GroundTruthDetectionResult?,
        elbow: PoseLandmarkerResult.GroundTruthDetectionResult?,
        wrist: PoseLandmarkerResult.GroundTruthDetectionResult?
    ): Float {
        if (shoulder == null || elbow == null || wrist == null) return 0f
        
        val shoulderPos = shoulder.position()
        val elbowPos = elbow.position()
        val wristPos = wrist.position()
        
        // Vector from elbow to shoulder
        val vectorESx = shoulderPos.x() - elbowPos.x()
        val vectorESy = shoulderPos.y() - elbowPos.y()
        
        // Vector from elbow to wrist
        val vectorEWx = wristPos.x() - elbowPos.x()
        val vectorEWy = wristPos.y() - elbowPos.y()
        
        // Calculate angle using dot product
        val dotProduct = vectorESx * vectorEWx + vectorESy * vectorEWy
        val magnitudeES = kotlin.math.sqrt((vectorESx * vectorESx + vectorESy * vectorESy).toDouble()).toFloat()
        val magnitudeEW = kotlin.math.sqrt((vectorEWx * vectorEWx + vectorEWy * vectorEWy).toDouble()).toFloat()
        
        if (magnitudeES == 0f || magnitudeEW == 0f) return 0f
        
        val cosAngle = (dotProduct / (magnitudeES * magnitudeEW)).coerceIn(-1f, 1f)
        return Math.toDegrees(kotlin.math.acos(cosAngle.toDouble())).toFloat()
    }
    
    /**
     * Calculate body alignment (hip should be in line with shoulder and ankle)
     */
    fun calculateBodyAlignment(
        shoulder: PoseLandmarkerResult.GroundTruthDetectionResult?,
        hip: PoseLandmarkerResult.GroundTruthDetectionResult?,
        ankle: PoseLandmarkerResult.GroundTruthDetectionResult?
    ): Float {
        if (shoulder == null || hip == null || ankle == null) return 0f
        
        val shoulderPos = shoulder.position()
        val hipPos = hip.position()
        val anklePos = ankle.position()
        
        // Calculate deviation from straight line
        val shoulderHipSlope = (hipPos.y() - shoulderPos.y()) / (hipPos.x() - shoulderPos.x() + 0.001f)
        val hipAnkleSlope = (anklePos.y() - hipPos.y()) / (anklePos.x() - hipPos.x() + 0.001f)
        
        val slopeDiff = kotlin.math.abs(shoulderHipSlope - hipAnkleSlope)
        return (1f - slopeDiff.coerceAtMost(1f)).coerceIn(0f, 1f)
    }
    
    /**
     * Calculate depth of push-up based on shoulder height relative to elbow
     */
    fun calculateDepth(
        shoulder: PoseLandmarkerResult.GroundTruthDetectionResult?,
        elbow: PoseLandmarkerResult.GroundTruthDetectionResult?
    ): Float {
        if (shoulder == null || elbow == null) return 0f
        
        val shoulderY = shoulder.position().y()
        val elbowY = elbow.position().y()
        
        // In a proper push-up, shoulder should go below elbow level
        val depth = (shoulderY - elbowY).coerceIn(0f, 0.3f) / 0.3f
        return depth.coerceIn(0f, 1f)
    }
    
    /**
     * Process pose landmarks and detect push-up reps
     */
    fun processPose(result: PoseLandmarkerResult): PushUpRep? {
        val currentTime = System.currentTimeMillis()
        
        // Get left side landmarks (can also average both sides)
        val landmarks = result.landmarks().firstOrNull() ?: return null
        
        val shoulder = landmarks[LEFT_SHOULDER]
        val elbow = landmarks[LEFT_ELBOW]
        val wrist = landmarks[LEFT_WRIST]
        val hip = landmarks[LEFT_HIP]
        val ankle = landmarks[LEFT_KNEE] // Using knee as approximation
        
        // Check landmark confidence
        if (shoulder.visibility() < 0.5f || elbow.visibility() < 0.5f || 
            wrist.visibility() < 0.5f || hip.visibility() < 0.5f) {
            return null
        }
        
        // Calculate angles and metrics
        val elbowAngle = calculateElbowAngle(shoulder, elbow, wrist)
        val bodyAlignment = calculateBodyAlignment(shoulder, hip, ankle)
        val depth = calculateDepth(shoulder, elbow)
        
        // State machine for rep detection
        when (currentState) {
            RepState.UP -> {
                if (elbowAngle < elbowAngleDownThreshold && bodyAlignment > minHipAlignment) {
                    currentState = RepState.DOWN
                }
            }
            RepState.DOWN -> {
                if (elbowAngle > elbowAngleUpThreshold && bodyAlignment > minHipAlignment) {
                    // Check minimum duration to prevent cheating
                    if (currentTime - lastRepTime >= minRepDuration) {
                        currentState = RepState.UP
                        repCount++
                        lastRepTime = currentTime
                        
                        // Calculate rep quality
                        val repScore = calculateRepScore(depth, bodyAlignment, elbowAngle, currentTime)
                        
                        return PushUpRep(
                            repNumber = repCount,
                            quality = determineQuality(repScore.overall),
                            score = repScore,
                            timestamp = currentTime,
                            damage = calculateDamage(repScore.overall)
                        )
                    }
                }
            }
            RepState.TRANSITIONING -> {
                // Transitional state to smooth detection
            }
        }
        
        return null
    }
    
    private fun calculateRepScore(
        depth: Float,
        alignment: Float,
        elbowAngle: Float,
        currentTime: Long
    ): RepScore {
        // Depth score (how low did they go)
        val depthScore = depth * 100f
        
        // Alignment score (body straightness)
        val alignmentScore = alignment * 100f
        
        // Range of motion (elbow angle variation)
        val romScore = ((180f - elbowAngle) / 180f) * 100f
        
        // Speed score (optimal is 2-3 seconds per rep)
        val repDuration = currentTime - lastRepTime
        val optimalDuration = 2500L // 2.5 seconds ideal
        val speedScore = (100f - kotlin.math.abs(repDuration - optimalDuration) / 10f).coerceIn(0f, 100f)
        
        val overall = (depthScore * 0.3f + alignmentScore * 0.3f + 
                      romScore * 0.2f + speedScore * 0.2f)
        
        return RepScore(
            depth = depthScore,
            alignment = alignmentScore,
            rangeOfMotion = romScore,
            speed = speedScore,
            overall = overall
        )
    }
    
    private fun determineQuality(overallScore: Float): RepQuality {
        return when {
            overallScore >= 90f -> RepQuality.PERFECT
            overallScore >= 75f -> RepQuality.GOOD
            overallScore >= 60f -> RepQuality.WEAK
            else -> RepQuality.INVALID
        }
    }
    
    private fun calculateDamage(overallScore: Float): Int {
        return when {
            overallScore >= 90f -> 150 // Perfect form
            overallScore >= 75f -> 100 // Good form
            overallScore >= 60f -> 50  // Weak form
            else -> 0                  // Invalid
        }
    }
    
    fun reset() {
        currentState = RepState.UP
        repCount = 0
        lastRepTime = 0L
    }
    
    fun getRepCount(): Int = repCount
}
