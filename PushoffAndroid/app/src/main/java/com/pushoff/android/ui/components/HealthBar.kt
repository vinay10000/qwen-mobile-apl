package com.pushoff.android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pushoff.android.ui.theme.PushoffTheme

/**
 * Animated health bar component for boss/player HP display
 */
@Composable
fun HealthBar(
    currentHealth: Float,
    maxHealth: Float,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    isEnemy: Boolean = false
) {
    val healthPercent = (currentHealth / maxHealth).coerceIn(0f, 1f)
    
    // Animate health changes smoothly
    val animatedHealth by animateFloatAsState(
        targetValue = healthPercent,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "healthAnimation"
    )
    
    // Determine color based on health percentage
    val barColor = when {
        animatedHealth > 0.6f -> Color(0xFF00E676) // Green
        animatedHealth > 0.3f -> Color(0xFFFFAB00) // Orange
        else -> Color(0xFFFF3D00) // Red
    }
    
    Column(modifier = modifier) {
        // Health bar track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .background(Color(0xFF1E2533))
        ) {
            // Animated health fill
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width((animatedHealth * 100).percent)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                barColor.copy(alpha = 0.8f),
                                barColor
                            )
                        )
                    )
            )
            
            // Shine effect overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.05f)
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite
                        )
                    )
            )
        }
        
        // Optional label
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isEnemy) Arrangement.End else Arrangement.Start
            ) {
                Text(
                    text = "${currentHealth.toInt()} / ${maxHealth.toInt()}",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    color = Color(0xFFB0B8C4),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Circular progress indicator for rep counter
 */
@Composable
fun RepCounterRing(
    reps: Int,
    targetReps: Int = 100,
    modifier: Modifier = Modifier,
    showNumber: Boolean = true
) {
    val progress by animateFloatAsState(
        targetValue = (reps.toFloat() / targetReps).coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "repProgress"
    )
    
    Box(
        modifier = modifier
            .size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background ring
        Canvas(
            modifier = Modifier.size(120.dp)
        ) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF1E2533),
                        Color(0xFF1E2533)
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx())
            )
        }
        
        // Progress ring
        Canvas(
            modifier = Modifier.size(120.dp)
        ) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF4E54C7),
                        Color(0xFF00E5FF)
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
        
        // Rep count in center
        if (showNumber) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$reps",
                    style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "REPS",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    color = Color(0xFFB0B8C4),
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

/**
 * Damage number popup that floats up and fades out
 */
@Composable
fun DamagePopup(
    damage: Int,
    isCritical: Boolean = false,
    onAnimationEnd: () -> Unit = {}
) {
    var offsetY by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(1f) }
    var scale by remember { mutableStateOf(1f) }
    
    LaunchedEffect(Unit) {
        // Float up animation
        animate(
            initialValue = 0f,
            targetValue = -100f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        ) { value, _ ->
            offsetY = value
        }
        
        // Fade out
        animate(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = tween(400, delayMillis = 400)
        ) { value, _ ->
            alpha = value
        }
        
        // Scale up slightly
        animate(
            initialValue = 1f,
            targetValue = 1.5f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        ) { value, _ ->
            scale = value
        }
        
        onAnimationEnd()
    }
    
    Text(
        text = if (isCritical) "CRITICAL! -$damage" else "-$damage",
        style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
        color = if (isCritical) Color(0xFFFF1744) else Color(0xFFFF5252),
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .graphicsLayer {
                translationY = offsetY
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 8.dp,
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
    )
}
