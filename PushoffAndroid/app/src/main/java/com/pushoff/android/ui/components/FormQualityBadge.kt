package com.pushoff.android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Form quality badge that shows rep quality with color and animation
 */
@Composable
fun FormQualityBadge(
    qualityScore: Float, // 0-100
    modifier: Modifier = Modifier
) {
    val (badgeText, badgeColor, gradientColors) = when {
        qualityScore >= 90 -> Triple("PERFECT", Color(0xFF00E676), listOf(Color(0xFF00E676), Color(0xFF00C853)))
        qualityScore >= 75 -> Triple("GOOD", Color(0xFFFFAB00), listOf(Color(0xFFFFD54F), Color(0xFFFFAB00)))
        qualityScore >= 60 -> Triple("WEAK", Color(0xFFFF9800), listOf(Color(0xFFFFB74D), Color(0xFFFF9800)))
        else -> Triple("INVALID", Color(0xFFFF3D00), listOf(Color(0xFFFF5252), Color(0xFFFF3D00)))
    }
    
    var scale by remember { mutableStateOf(0.5f) }
    var alpha by remember { mutableStateOf(0f) }
    
    LaunchedEffect(qualityScore) {
        // Pop in animation
        animate(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) { value, _ ->
            scale = value
        }
        
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(200)
        ) { value, _ ->
            alpha = value
        }
    }
    
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(colors = gradientColors)
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(colors = gradientColors.map { it.copy(alpha = 0.5f) }),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = badgeText,
            color = Color.White,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
    }
}

/**
 * Detailed form breakdown showing individual metrics
 */
@Composable
fun FormBreakdown(
    depth: Float,
    alignment: Float,
    rangeOfMotion: Float,
    speed: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E2533))
            .padding(16.dp)
    ) {
        Text(
            text = "FORM BREAKDOWN",
            style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
            color = Color(0xFFB0B8C4),
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        FormMetricRow("Depth", depth)
        Spacer(modifier = Modifier.height(8.dp))
        FormMetricRow("Body Alignment", alignment)
        Spacer(modifier = Modifier.height(8.dp))
        FormMetricRow("Range of Motion", rangeOfMotion)
        Spacer(modifier = Modifier.height(8.dp))
        FormMetricRow("Speed", speed)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Overall score
        val overallScore = (depth + alignment + rangeOfMotion + speed) / 4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OVERALL",
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                color = Color(0xFFB0B8C4)
            )
            Text(
                text = "${overallScore.toInt()}%",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                color = when {
                    overallScore >= 90 -> Color(0xFF00E676)
                    overallScore >= 75 -> Color(0xFFFFAB00)
                    overallScore >= 60 -> Color(0xFFFF9800)
                    else -> Color(0xFFFF3D00)
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FormMetricRow(label: String, value: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = Color(0xFFB0B8C4),
            modifier = Modifier.width(100.dp)
        )
        
        // Progress bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF252D3D))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width((value / 100 * 100).dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4E54C7),
                                Color(0xFF00E5FF)
                            )
                        )
                    )
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "${value.toInt()}%",
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}
