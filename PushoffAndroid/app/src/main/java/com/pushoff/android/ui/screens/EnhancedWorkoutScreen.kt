package com.pushoff.android.ui.screens

import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.viewinterop.AndroidView
import com.pushoff.android.ui.components.*

@Composable
fun EnhancedWorkoutScreen(
    reps: Int = 23,
    currentDamage: Int = 150,
    bossHealth: Float = 8420f,
    bossMaxHealth: Float = 10000f,
    bossName: String = "DRAGON LORD",
    formQuality: Float = 86f,
    depthScore: Float = 90f,
    alignmentScore: Float = 82f,
    romScore: Float = 100f,
    speedScore: Float = 70f,
    isWorkingOut: Boolean = true,
    onEndWorkout: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17))
    ) {
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC000000),
                            Color(0x40000000),
                            Color(0xCC000000)
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bossName,
                            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                            color = Color(0xFFFF5252),
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "LVL 15",
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            color = Color(0xFFB0B8C4)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    HealthBar(
                        currentHealth = bossHealth,
                        maxHealth = bossMaxHealth,
                        showLabel = true,
                        isEnemy = true
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF3D00),
                                        Color(0xFF0A0E17)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👹",
                            style = androidx.compose.material3.MaterialTheme.typography.displayLarge
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RepCounterRing(
                    reps = reps,
                    targetReps = 100,
                    modifier = Modifier.size(140.dp)
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormQualityBadge(
                        qualityScore = formQuality,
                        modifier = Modifier
                    )
                    
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2533))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        StatRow("Depth", depthScore)
                        StatRow("Alignment", alignmentScore)
                        StatRow("ROM", romScore)
                        StatRow("Speed", speedScore)
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentDamage > 0) {
                    val isCritical = currentDamage >= 150
                    DamagePopup(
                        damage = currentDamage,
                        isCritical = isCritical
                    )
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF1E2533)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatColumn("STREAK", "5", Color(0xFFFFAB00))
                        StatColumn("BEST", "12", Color(0xFF00E5FF))
                        StatColumn("CALORIES", "142", Color(0xFF00E676))
                        StatColumn("TIME", "2:34", Color(0xFF2979FF))
                    }
                }
                
                Button(
                    onClick = onEndWorkout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isWorkingOut
                ) {
                    Text(
                        text = if (isWorkingOut) "END WORKOUT" else "WORKOUT COMPLETE",
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = Color(0xFFB0B8C4)
        )
        Text(
            text = "${value.toInt()}%",
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = when {
                value >= 90 -> Color(0xFF00E676)
                value >= 75 -> Color(0xFFFFAB00)
                value >= 60 -> Color(0xFFFF9800)
                else -> Color(0xFFFF3D00)
            },
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = Color(0xFFB0B8C4),
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
