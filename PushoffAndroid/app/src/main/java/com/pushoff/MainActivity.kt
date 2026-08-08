package com.pushoff

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pushoff.feature.workout.WorkoutScreen
import com.pushoff.feature.ranked.RankedModeScreen
import com.pushoff.domain.model.WorkoutType

/**
 * Main Activity - Entry point for Pushoff Android app.
 * Uses Jetpack Compose for UI and Navigation Compose for routing.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                PushoffApp()
            }
        }
    }
}

@Composable
fun PushoffApp() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToWorkout = { navController.navigate("workout") },
                    onNavigateToRanked = { navController.navigate("ranked") },
                    onNavigateToCampaign = { navController.navigate("campaign") }
                )
            }
            
            composable("workout") {
                WorkoutScreen(
                    workoutType = WorkoutType.FREE_WORKOUT,
                    onWorkoutComplete = { result ->
                        // Handle workout completion
                        navController.popBackStack()
                    }
                )
            }
            
            composable("ranked") {
                RankedModeScreen(
                    playerElo = 1200,
                    onMatchFound = { opponentId ->
                        // Navigate to battle screen
                    },
                    onMatchCancelled = {
                        // Handle cancellation
                    }
                )
            }
            
            composable("campaign") {
                // Campaign screen placeholder
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("Campaign Mode - Coming Soon")
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: androidx.navigation.NavController) {
    NavigationBar {
        val items = listOf(
            "home" to "Home",
            "workout" to "Workout",
            "ranked" to "Ranked",
            "campaign" to "Campaign"
        )
        
        items.forEach { (route, label) ->
            NavigationBarItem(
                icon = { Text("📱") }, // Replace with actual icons
                label = { Text(label) },
                selected = false,
                onClick = {
                    navController.navigate(route) {
                        popUpTo("home")
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToWorkout: () -> Unit,
    onNavigateToRanked: () -> Unit,
    onNavigateToCampaign: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pushoff",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 32.dp)
        )
        
        Text(
            text = "Turn your push-ups into epic battles!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Quick action buttons
        Button(
            onClick = onNavigateToWorkout,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text("FREE WORKOUT", style = MaterialTheme.typography.titleMedium)
        }
        
        Button(
            onClick = onNavigateToRanked,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("RANKED BATTLE", style = MaterialTheme.typography.titleMedium)
        }
        
        OutlinedButton(
            onClick = onNavigateToCampaign,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text("CAMPAIGN MODE", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}
