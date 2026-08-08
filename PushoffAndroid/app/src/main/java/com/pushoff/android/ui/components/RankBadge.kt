package com.pushoff.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class RankTier(val displayName: String, val color1: Color, val color2: Color, val borderColor: Color) {
    BRONZE("Bronze", Color(0xFFCD7F32), Color(0xFF8B4513), Color(0xFFFFD700)),
    SILVER("Silver", Color(0xFFC0C0C0), Color(0xFF808080), Color(0xFFFFFFFF)),
    GOLD("Gold", Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFFF00)),
    PLATINUM("Platinum", Color(0xFFE5E4E2), Color(0xFFCCCCCC), Color(0xFF00E5FF)),
    DIAMOND("Diamond", Color(0xFFB9F2FF), Color(0xFF00B8CC), Color(0xFF2979FF)),
    MASTER("Master", Color(0xFF9D4EDD), Color(0xFF6A0DAD), Color(0xFFFF006E)),
    GRANDMASTER("Grandmaster", Color(0xFFFF006E), Color(0xFF9D4EDD), Color(0xFFFFFFFF))
}

fun getRankTierFromElo(elo: Int): RankTier = when {
    elo >= 2000 -> RankTier.GRANDMASTER
    elo >= 1800 -> RankTier.MASTER
    elo >= 1600 -> RankTier.DIAMOND
    elo >= 1400 -> RankTier.PLATINUM
    elo >= 1200 -> RankTier.GOLD
    elo >= 1000 -> RankTier.SILVER
    else -> RankTier.BRONZE
}

@Composable
fun RankBadge(
    elo: Int,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val tier = getRankTierFromElo(elo)
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Badge circle
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(tier.color1, tier.color2)
                    )
                )
                .border(
                    width = 3.dp,
                    color = tier.borderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = elo.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )
        }
        
        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tier.displayName,
                color = tier.color1,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }
    }
}
