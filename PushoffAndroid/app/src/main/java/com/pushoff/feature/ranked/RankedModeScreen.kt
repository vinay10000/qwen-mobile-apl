package com.pushoff.feature.ranked

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushoff.domain.model.LeaderboardEntry
import com.pushoff.domain.repository.MatchStatus

/**
 * Ranked 1v1 matchmaking and battle screen.
 * Uses ELO-based matchmaking with progressive range expansion.
 */
@Composable
fun RankedModeScreen(
    playerElo: Int = 1200,
    onMatchFound: (String) -> Unit,
    onMatchCancelled: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearching by remember { mutableStateOf(false) }
    var searchDuration by remember { mutableStateOf(0L) }
    var currentRange by remember { mutableStateOf(100) } // ELO range ±100
    
    // Simulated opponent found
    var matchStatus by remember { mutableStateOf<MatchStatus>(MatchStatus.Searching) }
    
    LaunchedEffect(isSearching) {
        if (isSearching) {
            // Simulate matchmaking timer and range expansion
            while (isSearching) {
                kotlinx.coroutines.delay(1000)
                searchDuration += 1000
                
                // Expand ELO range over time
                when {
                    searchDuration > 15000 -> currentRange = 250
                    searchDuration > 5000 -> currentRange = 150
                }
                
                // Simulate finding a match after random time
                if (searchDuration > 8000 && matchStatus == MatchStatus.Searching) {
                    matchStatus = MatchStatus.MatchFound(
                        opponentId = "opponent_123",
                        opponentElo = playerElo + (Math.random() * 100 - 50).toInt()
                    )
                }
            }
        } else {
            searchDuration = 0L
            currentRange = 100
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A237E), // Dark blue
                            Color(0xFF0D47A1),
                            Color(0xFF1565C0)
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "RANKED BATTLE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(top = 32.dp)
            )
            
            // Player ELO badge
            Card(
                modifier = Modifier
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOUR ELO",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = playerElo.toString(),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700) // Gold
                    )
                    
                    // Rank badge
                    RankBadge(elo = playerElo)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Matchmaking status
            when (val status = matchStatus) {
                is MatchStatus.Searching -> {
                    MatchmakingUI(
                        isSearching = isSearching,
                        searchDuration = searchDuration,
                        currentRange = currentRange,
                        onStartSearch = { isSearching = true },
                        onCancelSearch = {
                            isSearching = false
                            matchStatus = MatchStatus.Searching
                            onMatchCancelled()
                        }
                    )
                }
                is MatchStatus.MatchFound -> {
                    MatchFoundUI(
                        opponentElo = status.opponentElo,
                        onAccept = {
                            onMatchFound(status.opponentId)
                        },
                        onDecline = {
                            matchStatus = MatchStatus.Searching
                            isSearching = true
                        }
                    )
                }
                else -> {}
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Leaderboard preview
            Text(
                text = "TOP PLAYERS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LeaderboardPreview()
        }
    }
}

@Composable
private fun MatchmakingUI(
    isSearching: Boolean,
    searchDuration: Long,
    currentRange: Int,
    onStartSearch: () -> Unit,
    onCancelSearch: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isSearching) {
            Button(
                onClick = onStartSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700) // Gold
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "FIND OPPONENT",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E)
                )
            }
        } else {
            // Searching animation
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = Color(0xFFFFD700),
                strokeWidth = 6.dp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "SEARCHING...",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "${searchDuration / 1000}s • ELO Range: ±$currentRange",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
            
            OutlinedButton(
                onClick = onCancelSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CANCEL", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun MatchFoundUI(
    opponentElo: Int,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "OPPONENT FOUND!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32) // Green
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "YOU",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = "ELO $opponentElo",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E)
                    )
                }
                
                Text(
                    text = "VS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Red
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "OPPONENT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = "ELO $opponentElo",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E)
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("DECLINE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ACCEPT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RankBadge(elo: Int) {
    val rank = when {
        elo >= 2000 -> "Grandmaster" to Color(0xFFFFD700)
        elo >= 1800 -> "Master" to Color(0xFFE91E63)
        elo >= 1600 -> "Diamond" to Color(0xFF2196F3)
        elo >= 1400 -> "Platinum" to Color(0xFF9C27B0)
        elo >= 1200 -> "Gold" to Color(0xFFFFD700)
        elo >= 1000 -> "Silver" to Color(0xFF9E9E9E)
        else -> "Bronze" to Color(0xFF795548)
    }
    
    Card(
        modifier = Modifier.padding(top = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = rank.second.copy(alpha = 0.2f)
        )
    ) {
        Text(
            text = rank.first,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = rank.second,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun LeaderboardPreview() {
    val fakeLeaderboard = listOf(
        LeaderboardEntry("1", "ProPlayer", null, 2150, 1, 245, 32),
        LeaderboardEntry("2", "PushupKing", null, 2080, 2, 231, 45),
        LeaderboardEntry("3", "FitnessGuru", null, 1950, 3, 198, 52)
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
    ) {
        items(fakeLeaderboard.size) { index ->
            val entry = fakeLeaderboard[index]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "#${entry.rank}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = entry.username,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${entry.elo} ELO",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFFD700)
                )
            }
        }
    }
}
