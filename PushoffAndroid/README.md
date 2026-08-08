# Pushoff Android - AI Push-up Game App

A gamified fitness app that turns push-ups into epic battles using on-device AI pose detection.

## 🎮 Features

### Workout Modes
- **Free Workout** - Basic push-up counter with form analysis
- **Campaign** - Battle through levels defeating bosses
- **Speed Challenge** - Max reps in 60 seconds
- **Survival** - Endless mode with continuous boss attacks
- **Ranked 1v1** - ELO-based competitive matchmaking

### Core Technology
- **On-device Pose Detection** - MediaPipe Pose Landmarker for privacy and low latency
- **Rep Quality Scoring** - Depth, alignment, ROM, and speed analysis
- **Anti-Cheat System** - Workout signatures and Play Integrity API
- **LibGDX Game Engine** - Particle effects, animations, and boss battles

## 🏗 Architecture

```
┌───────────────── Android App ──────────────────┐
│                                                 │
│  Jetpack Compose UI                             │
│        │                                        │
│        ▼                                        │
│  Game Engine / Workout State                    │
│        │                                        │
│   ┌────┴─────────┐                              │
│   ▼              ▼                              │
│ Pose Engine    Game Engine                      │
│   │              │                              │
│ CameraX       HP / Damage / XP                  │
│   │           Bosses / Effects                  │
│   ▼              │                              │
│ MediaPipe       │                               │
│ Pose Landmarker │                               │
│   │              │                              │
│   └──────┬───────┘                              │
│          ▼                                      │
│   Rep Detection                                 │
│          │                                      │
│          ▼                                      │
│   Workout Results                               │
└──────────┬──────────────────────────────────────┘
           │
           ▼
       Backend API
           │
     ┌─────┼─────────┐
     ▼     ▼         ▼
   Auth  Database  Leaderboards
                   Matchmaking
```

## 🛠 Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | Clean Architecture + MVVM |
| Camera | CameraX |
| Pose Detection | MediaPipe Pose Landmarker |
| Local DB | Room |
| Networking | Ktor Client |
| Serialization | Kotlinx Serialization |
| Async | Coroutines + Flow |
| DI | Hilt |
| Animations | Compose Animation + Lottie |
| Game Engine | LibGDX |
| Backend | Supabase (PostgreSQL + Auth + Realtime) |
| Analytics | Firebase Analytics |
| Crash Reporting | Firebase Crashlytics |
| Anti-Cheat | Play Integrity API |

## 📁 Project Structure

```
app/
├── core/
│   ├── camera/          # CameraX integration
│   ├── pose/            # MediaPipe pose detection
│   ├── network/         # API clients
│   ├── database/        # Room entities
│   └── analytics/       # Tracking
│
├── feature/
│   ├── auth/            # Authentication screens
│   ├── home/            # Home dashboard
│   ├── workout/         # Workout screen with camera
│   ├── campaign/        # Campaign mode
│   ├── ranked/          # Ranked matchmaking
│   ├── leaderboard/     # Global rankings
│   ├── profile/         # User profile
│   └── settings/        # App settings
│
├── game/
│   ├── engine/          # LibGDX game loop
│   ├── rendering/       # Visual effects
│   └── combat/          # Battle logic
│
└── domain/
    ├── model/           # Data classes
    ├── repository/      # Repository interfaces
    └── usecase/         # Business logic
```

## 🔑 Key Implementation Details

### Push-up Detection Algorithm

```kotlin
// State machine for rep detection
UP (elbow > 155°) 
  ↓ angle < 100° + body alignment
DOWN
  ↓ angle > 155° + body alignment  
REP COMPLETE + Quality Score
```

### Rep Quality Metrics
- **Depth** (30%) - Shoulder below elbow level
- **Alignment** (30%) - Body straightness (hip position)
- **Range of Motion** (20%) - Elbow angle variation
- **Speed** (20%) - Optimal 2-3 seconds per rep

### Damage Calculation
```kotlin
Perfect (90-100%) → 150 damage
Good (75-89%)     → 100 damage
Weak (60-74%)     → 50 damage
Invalid (<60%)    → 0 damage
```

### ELO System
```kotlin
Expected Score: EA = 1 / (1 + 10^((RB - RA)/400))
New Rating: R' = R + K × (Actual - Expected)
K-factor: 32
Starting ELO: 1000
```

### Matchmaking Range Expansion
```
0-5 sec:   ±100 ELO
5-15 sec:  ±150 ELO
15-30 sec: ±250 ELO
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34
- Physical Android device with camera (for testing)

### Setup

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Add MediaPipe pose model to `assets/`:
   - Download `pose_landmarker_full.task` from MediaPipe
   - Place in `app/src/main/assets/`
5. Configure backend:
   - Set up Supabase project
   - Update `local.properties` with credentials
6. Run on device (Camera requires physical device)

### Configuration

Create `local.properties`:
```properties
supabase.url=YOUR_SUPABASE_URL
supabase.key=YOUR_SUPABASE_ANON_KEY
firebase.app.id=YOUR_FIREBASE_APP_ID
```

## 🔒 Privacy & Security

- **All pose processing happens on-device** - No camera frames sent to server
- **Minimal data upload** - Only workout summaries (reps, duration, form scores)
- **Play Integrity API** - Prevents modified APKs and rooted devices in ranked mode
- **Workout signatures** - Cryptographic hashing for cheat detection

## 📊 Database Schema

```sql
-- Users table
users (id, username, email, avatar_url, xp, level, elo, created_at)

-- Workouts table
workouts (id, user_id, type, duration, reps, valid_reps, form_score, calories, created_at)

-- Matches table
matches (id, player1_id, player2_id, player1_score, player2_score, winner_id, created_at)

-- Leaderboard view
leaderboard (user_id, elo, rank, wins, losses)
```

## 🎯 Roadmap

### Phase 1 (Current)
- ✅ Core pose detection
- ✅ Workout screen UI
- ✅ Basic game engine
- ✅ Ranked matchmaking UI

### Phase 2
- [ ] Backend integration (Supabase)
- [ ] Full campaign mode
- [ ] Achievement system
- [ ] Social features

### Phase 3
- [ ] Custom character skins
- [ ] Seasonal events
- [ ] Guild/clan system
- [ ] Tournament mode

## 💰 Monetization Strategy

**Free Tier:**
- Free workouts
- Basic campaign
- Limited challenges
- Basic stats

**Pro Tier:**
- Unlimited campaigns
- Advanced form analysis
- Ranked mode
- Custom characters
- No ads
- Detailed statistics

## 📝 License

MIT License - See LICENSE file for details

## 👥 Contributing

Contributions welcome! Please read CONTRIBUTING.md first.

---

Built with ❤️ using Kotlin, Jetpack Compose, MediaPipe, and LibGDX
