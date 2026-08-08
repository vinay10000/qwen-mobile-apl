package com.pushoff.feature.workout

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.camera.view.PreviewView
import com.pushoff.core.pose.PushUpDetector
import com.pushoff.domain.model.WorkoutType

/**
 * Main workout screen with camera preview and game HUD overlay.
 * Portrait orientation for optimal push-up positioning.
 */
@Composable
fun WorkoutScreen(
    workoutType: WorkoutType = WorkoutType.FREE_WORKOUT,
    bossName: String = "Goblin",
    bossMaxHp: Int = 1000,
    onWorkoutComplete: (WorkoutResult) -> Unit,
    modifier: Modifier = Modifier
) {
    var repCount by remember { mutableStateOf(0) }
    var currentRep by remember { mutableStateOf<PushUpDetector.PushUpRep?>(null) }
    var isWorkoutActive by remember { mutableStateOf(false) }
    var workoutDuration by remember { mutableStateOf(0L) }
    
    // Animation states
    val damageOffset by animateFloatAsState(
        targetValue = if (currentRep != null) -100f else 0f,
        animationSpec = tween(500)
    )
    
    val qualityColor = when (currentRep?.quality) {
        PushUpDetector.RepQuality.PERFECT -> Color(0xFF00E676) // Green
        PushUpDetector.RepQuality.GOOD -> Color(0xFF29B6F6) // Blue
        PushUpDetector.RepQuality.WEAK -> Color(0xFFFFCA28) // Orange
        PushUpDetector.RepQuality.INVALID -> Color(0xFFEF5350) // Red
        null -> Color.Transparent
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Camera Preview (would be implemented with AndroidView in real app)
        CameraPreviewPlaceholder(
            modifier = Modifier.fillMaxSize()
        )
        
        // Game HUD Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Boss HP Bar (top)
            if (workoutType != WorkoutType.FREE_WORKOUT) {
                BossHpBar(
                    bossName = bossName,
                    currentHp = (bossMaxHp - repCount * 100).coerceAtLeast(0),
                    maxHp = bossMaxHp,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Center spacer - camera shows user
            Spacer(modifier = Modifier.weight(1f))
            
            // Rep counter and quality indicator (bottom)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Rep count
                    Text(
                        text = "REP $repCount",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Quality indicator
                    AnimatedVisibility(
                        visible = currentRep != null,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        currentRep?.let { rep ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.animateContentSize()
                            ) {
                                Text(
                                    text = rep.quality.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = qualityColor
                                )
                                
                                // Damage dealt
                                Text(
                                    text = "+${rep.damage} DMG",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                
                                // Form score breakdown
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    FormMetric("Depth", rep.score.depth)
                                    FormMetric("Alignment", rep.score.alignment)
                                    FormMetric("ROM", rep.score.rangeOfMotion)
                                    FormMetric("Speed", rep.score.speed)
                                }
                            }
                        }
                    }
                    
                    // Start/Stop button
                    Button(
                        onClick = {
                            isWorkoutActive = !isWorkoutActive
                            if (!isWorkoutActive) {
                                // Complete workout
                                onWorkoutComplete(
                                    WorkoutResult(
                                        reps = repCount,
                                        duration = workoutDuration,
                                        averageForm = currentRep?.score?.overall ?: 0f
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(top = 24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isWorkoutActive) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (isWorkoutActive) "STOP WORKOUT" else "START WORKOUT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BossHpBar(
    bossName: String,
    currentHp: Int,
    maxHp: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = bossName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "$currentHp / $maxHp HP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // HP bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(currentHp.toFloat() / maxHp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.error)
                )
            }
        }
    }
}

@Composable
private fun FormMetric(label: String, value: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "${value.toInt()}%",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        // Mini progress bar
        LinearProgressIndicator(
            progress = value / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun CameraPreviewPlaceholder(modifier: Modifier = Modifier) {
    // In real implementation, this would use AndroidView to embed PreviewView
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Text(
            text = "Camera Preview",
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

data class WorkoutResult(
    val reps: Int,
    val duration: Long,
    val averageForm: Float
)
