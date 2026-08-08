# Pushoff Android - Quick Start Guide

## 5-Minute Setup

### Step 1: Download MediaPipe Model (2 min)
```bash
cd /workspace/PushoffAndroid/app/src/main/assets
curl -O https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_full/1/pose_landmarker_full.task
```

### Step 2: Create Supabase Project (3 min)
1. Go to https://supabase.com → New Project
2. Wait for provisioning (~2 min)
3. Go to SQL Editor and run the schema from `NEXT_STEPS.md`
4. Copy your project URL and anon key

### Step 3: Configure Secrets (1 min)
Create `/workspace/PushoffAndroid/local.properties`:
```properties
supabase.url=https://your-project.supabase.co
supabase.key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Step 4: Build & Run
```bash
cd /workspace/PushoffAndroid
./gradlew assembleDebug
```

Install on your Android device and test!

---

## File Structure Summary

```
PushoffAndroid/
├── app/src/main/java/com/pushoff/
│   ├── core/
│   │   ├── camera/          # CameraX + MediaPipe integration
│   │   ├── pose/            # Push-up detection algorithm
│   │   ├── database/        # Room offline storage
│   │   ├── network/         # Supabase backend client
│   │   └── anticheat/       # Play Integrity + cheat detection
│   │
│   ├── domain/
│   │   ├── model/           # Data classes
│   │   ├── repository/      # Repository interfaces
│   │   └── usecase/         # Business logic (ELO, XP, validation)
│   │
│   ├── feature/
│   │   ├── workout/         # Main workout screen with camera
│   │   └── ranked/          # Ranked matchmaking UI
│   │
│   └── game/
│       └── engine/          # LibGDX game loop
│
├── README.md                # Full documentation
├── NEXT_STEPS.md           # Detailed setup guide
└── QUICKSTART.md           # This file
```

---

## Key Features Implemented

✅ **Push-up Detection**
- MediaPipe Pose Landmarker for body tracking
- Elbow angle calculation (UP: >155°, DOWN: <100°)
- Body alignment checking
- Rep quality scoring (Depth, Alignment, ROM, Speed)
- Damage calculation based on form

✅ **Workout Modes**
- Free Workout (basic counter)
- Campaign (boss battles)
- Speed Challenge (60 seconds)
- Survival (endless mode)
- Ranked 1v1 (ELO matchmaking)

✅ **Backend Integration**
- Supabase Auth (email/password)
- PostgreSQL database
- Real-time updates
- Row-level security

✅ **Anti-Cheat System**
- Play Integrity API
- Workout signature hashing
- Cheat pattern detection:
  - Mechanical timing detection
  - Impossible rep counts
  - Form score anomalies
  - Rapid-fire reps

✅ **ELO Ranking System**
- Starting ELO: 1000
- K-factor: 32
- Progressive matchmaking range:
  - 0-5s: ±100 ELO
  - 5-15s: ±150 ELO
  - 15-30s: ±250 ELO

✅ **Rank Tiers**
- Bronze (<1000)
- Silver (1000-1199)
- Gold (1200-1399)
- Platinum (1400-1599)
- Diamond (1600-1799)
- Master (1800-1999)
- Grandmaster (2000+)

---

## Architecture Diagram

```
┌─────────────────────────────────────────────┐
│           Jetpack Compose UI                │
│  ┌─────────────┐    ┌─────────────────────┐ │
│  │ Home Screen │    │  Workout Screen     │ │
│  └─────────────┘    │  - Camera preview   │ │
│                     │  - Boss HP bar      │ │
│  ┌─────────────┐    │  - Rep counter      │ │
│  │ Ranked Mode │    │  - Form metrics     │ │
│  └─────────────┘    └─────────────────────┘ │
└─────────────────────────────────────────────┘
                    │
┌───────────────────┼─────────────────────────┐
│              Domain Layer                   │
│  ┌─────────────┐    ┌─────────────────────┐ │
│  │ UseCases    │    │  Repositories       │ │
│  │ - ELO calc  │    │  - Auth             │ │
│  │ - XP calc   │    │  - Workouts         │ │
│  │ - Validation│    │  - Matches          │ │
│  └─────────────┘    └─────────────────────┘ │
└───────────────────┼─────────────────────────┘
                    │
┌───────────────────┼─────────────────────────┐
│              Core Layer                     │
│  ┌─────────────┐    ┌─────────────────────┐ │
│  │ CameraX     │───▶│  MediaPipe Pose     │ │
│  │             │    │  - 33 landmarks     │ │
│  │             │    │  - Live streaming   │ │
│  └─────────────┘    └─────────────────────┘ │
│                            │                │
│  ┌─────────────┐    ┌──────▼──────────────┐ │
│  │ Room DB     │    │  PushUpDetector     │ │
│  │ (Offline)   │    │  - State machine    │ │
│  └─────────────┘    │  - Quality scoring  │ │
│                     │  - Damage calc      │ │
│  ┌─────────────┐    └──────┬──────────────┘ │
│  │ Supabase    │◀──────────┘                │
│  │ (Backend)   │                             │
│  └─────────────┘    ┌─────────────────────┐ │
│                     │  AntiCheatManager   │ │
│  ┌─────────────┐    │  - Play Integrity   │ │
│  │ LibGDX      │    │  - Signature hash   │ │
│  │ (Game)      │    └─────────────────────┘ │
│  └─────────────┘                            │
└─────────────────────────────────────────────┘
```

---

## Tech Stack Reference

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | 1.9+ |
| UI | Jetpack Compose | 1.5.4 |
| Camera | CameraX | 1.3.0 |
| Pose Detection | MediaPipe | 0.10.8 |
| Database | Room | 2.6.1 |
| Backend | Supabase | 2.5.0 |
| Networking | Ktor | 2.3.6 |
| DI | Hilt | 2.48.1 |
| Game Engine | LibGDX | 1.12.1 |
| Anti-Cheat | Play Integrity | 1.3.0 |
| Analytics | Firebase | 32.6.0 |

---

## Testing Checklist

Before launch, verify:

- [ ] Camera permission granted
- [ ] Pose detection works in various lighting
- [ ] Rep counting accurate (compare with manual count)
- [ ] Form scoring reflects actual form
- [ ] Supabase connection successful
- [ ] User can sign up/login
- [ ] Workouts save to database
- [ ] Leaderboard displays correctly
- [ ] Matchmaking finds opponents
- [ ] ELO updates after matches
- [ ] Anti-cheat flags suspicious workouts

---

## Common Issues & Solutions

### Issue: "pose_landmarker_full.task not found"
**Solution:** Download the model file to `app/src/main/assets/`

### Issue: "Camera permission denied"
**Solution:** Request permission at runtime before starting camera

### Issue: "Supabase connection failed"
**Solution:** Verify URL and key in `local.properties`, check RLS policies

### Issue: "Too many reps detected"
**Solution:** Adjust elbow angle thresholds or minimum rep duration

### Issue: "Matchmaking never finds opponent"
**Solution:** Expand ELO range or add bots for low-population times

---

## Next Actions

1. **Download the MediaPipe model** (required for build)
2. **Set up Supabase backend** (required for auth/multiplayer)
3. **Test on physical device** (camera requires real hardware)
4. **Implement remaining features** (see NEXT_STEPS.md)
5. **Add game assets** (boss sprites, animations)
6. **Configure Google Play** (for release)

Good luck building! 💪🎮
