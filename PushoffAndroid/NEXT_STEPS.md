# Pushoff Android - Next Steps Guide

## ✅ Completed Components

### Core Features
- **Push-up Detection** (`PushUpDetector.kt`) - MediaPipe-based rep counting with quality scoring
- **Camera Integration** (`PoseCameraController.kt`) - CameraX + MediaPipe streaming
- **Workout UI** (`WorkoutScreen.kt`) - Compose UI with camera overlay and form metrics
- **Ranked Mode UI** (`RankedModeScreen.kt`) - ELO matchmaking interface with rank badges
- **Game Logic** (`GameUseCases.kt`) - ELO calculation, anti-cheat validation, XP system

### Backend & Storage
- **Supabase Client** (`SupabaseBackend.kt`) - Auth, workouts, matches, leaderboard APIs
- **Room Database** (`Database.kt`) - Offline storage for users, workouts, matches, achievements
- **Anti-Cheat System** (`AntiCheatManager.kt`) - Play Integrity API + workout signature validation

### Data Models
- User, Workout, RankedMatch, LeaderboardEntry, Achievement
- WorkoutType enum (FREE_WORKOUT, CAMPAIGN, SPEED_CHALLENGE, SURVIVAL, RANKED_MATCH)
- RepQuality enum (PERFECT, GOOD, WEAK, INVALID)

---

## 🚀 Immediate Next Steps

### 1. Download MediaPipe Pose Model
```bash
# Download the pose landmarker model
cd app/src/main/assets/

# Option A: Using curl (Linux/Mac)
curl -O https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_full/1/pose_landmarker_full.task

# Option B: Manual download
# Visit: https://ai.google.dev/edge/mediapipe/solutions/vision/pose_landmarker
# Download "pose_landmarker_full.task" and place in assets/
```

### 2. Configure Supabase Backend

Create a Supabase project at https://supabase.com:

```sql
-- Run this SQL in your Supabase SQL Editor

-- Users table
CREATE TABLE users (
  id UUID PRIMARY KEY REFERENCES auth.users(id),
  username TEXT UNIQUE NOT NULL,
  email TEXT,
  avatar_url TEXT,
  xp BIGINT DEFAULT 0,
  level INTEGER DEFAULT 1,
  elo INTEGER DEFAULT 1000,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Workouts table
CREATE TABLE workouts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  type TEXT NOT NULL,
  duration BIGINT NOT NULL,
  reps INTEGER NOT NULL,
  valid_reps INTEGER NOT NULL,
  average_form_score REAL NOT NULL,
  calories INTEGER DEFAULT 0,
  damage_dealt INTEGER DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Matches table
CREATE TABLE matches (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  player1_id UUID REFERENCES users(id),
  player2_id UUID REFERENCES users(id),
  player1_score INTEGER NOT NULL,
  player2_score INTEGER NOT NULL,
  winner_id UUID REFERENCES users(id),
  player1_reps INTEGER NOT NULL,
  player2_reps INTEGER NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE workouts ENABLE ROW LEVEL SECURITY;
ALTER TABLE matches ENABLE ROW LEVEL SECURITY;

-- Policies for users
CREATE POLICY "Users can view all users" ON users FOR SELECT USING (true);
CREATE POLICY "Users can update own profile" ON users FOR UPDATE USING (auth.uid() = id);
CREATE POLICY "Users can insert own profile" ON users FOR INSERT WITH CHECK (auth.uid() = id);

-- Policies for workouts
CREATE POLICY "Users can view own workouts" ON workouts FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own workouts" ON workouts FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Policies for matches
CREATE POLICY "Users can view matches they participated in" ON matches 
  FOR SELECT USING (auth.uid() = player1_id OR auth.uid() = player2_id);
CREATE POLICY "System can insert matches" ON matches FOR INSERT WITH CHECK (true);

-- Create indexes for performance
CREATE INDEX idx_workouts_user_id ON workouts(user_id);
CREATE INDEX idx_workouts_created_at ON workouts(created_at DESC);
CREATE INDEX idx_matches_player1 ON matches(player1_id);
CREATE INDEX idx_matches_player2 ON matches(player2_id);
CREATE INDEX idx_users_elo ON users(elo DESC);

-- Function to update ELO after match
CREATE OR REPLACE FUNCTION update_user_elo()
RETURNS TRIGGER AS $$
BEGIN
  -- Update winner ELO
  IF NEW.winner_id IS NOT NULL THEN
    UPDATE users SET elo = elo + 12 WHERE id = NEW.winner_id;
    
    -- Update loser ELO
    IF NEW.winner_id = NEW.player1_id THEN
      UPDATE users SET elo = elo - 12 WHERE id = NEW.player2_id;
    ELSE
      UPDATE users SET elo = elo - 12 WHERE id = NEW.player1_id;
    END IF;
  END IF;
  
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to update ELO automatically
CREATE TRIGGER trigger_update_elo_after_match
AFTER INSERT ON matches
FOR EACH ROW EXECUTE FUNCTION update_user_elo();
```

### 3. Create `local.properties` File

```properties
# In root of PushoffAndroid project
supabase.url=https://your-project.supabase.co
supabase.key=your-anon-key-here
firebase.app.id=1:123456789:android:abcdef123456
```

### 4. Add Firebase Configuration

1. Go to https://console.firebase.google.com
2. Create a new project or select existing
3. Add Android app with package name `com.pushoff`
4. Download `google-services.json`
5. Place in `app/` directory

### 5. Update AndroidManifest.xml

Ensure permissions are set:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```

### 6. Build & Run

```bash
# Sync Gradle
./gradlew sync

# Build debug APK
./gradlew assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk

# Or run directly from Android Studio
```

---

## 📋 Remaining Implementation Tasks

### Priority 1 - Core Functionality
- [ ] Integrate CameraX with WorkoutScreen (currently placeholder)
- [ ] Connect push-up detection to game engine (LibGDX)
- [ ] Implement real-time rep feedback animations
- [ ] Add sound effects for rep completion

### Priority 2 - Backend Integration
- [ ] Wire up SupabaseBackend with repositories
- [ ] Implement authentication flow (login/signup screens)
- [ ] Sync local Room database with Supabase
- [ ] Add offline-first sync logic

### Priority 3 - Game Modes
- [ ] Campaign mode with boss progression
- [ ] Speed challenge timer and scoring
- [ ] Survival mode with HP drain mechanic
- [ ] Character/enemy sprites and animations

### Priority 4 - Social Features
- [ ] Global chat implementation
- [ ] Friend system
- [ ] Achievement unlock notifications
- [ ] Push notifications for challenges

### Priority 5 - Polish
- [ ] Lottie animations for UI effects
- [ ] Haptic feedback on rep completion
- [ ] Onboarding tutorial
- [ ] Settings screen (audio, notifications, privacy)

---

## 🎮 LibGDX Game Engine Setup

To integrate LibGDX for boss battles:

```kotlin
// Create GameActivity.kt
class GameActivity : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val config = AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
            hideStatusBar = true
        }
        
        initialize(PushoffGame(config), config)
    }
}

// In PushoffGame.kt
class PushoffGame(val config: AndroidApplicationConfiguration) : Game() {
    override fun create() {
        // Initialize game world, boss, player
        // Set up renderers, cameras, input processors
    }
    
    override fun render() {
        // Game loop
        // Update game state
        // Render frames
    }
}
```

---

## 🔒 Security Checklist

- [x] Play Integrity API integration
- [x] Workout signature generation
- [x] Cheat pattern detection
- [ ] Server-side signature verification (backend function)
- [ ] Rate limiting on API endpoints
- [ ] Encrypted local storage for sensitive data

---

## 📊 Monetization Setup

For Google Play Billing:

```kotlin
// Add to build.gradle.kts
implementation("com.android.billingclient:billing-ktx:6.1.0")

// Define products
// Weekly Pro: ₹599
// Monthly Pro: ₹1,499
// Yearly Pro: ₹2,999
```

---

## 🧪 Testing Strategy

### Unit Tests
```kotlin
// Test push-up detection algorithm
@Test
fun testPushUpDetection_withPerfectForm_returnsPerfectQuality() {
    // Test elbow angle thresholds
    // Test body alignment calculations
    // Test rep timing validation
}

// Test ELO calculations
@Test
fun testEloChange_winnerGainsPoints() {
    // Verify expected score formula
    // Verify rating changes
}
```

### Integration Tests
- Camera permission handling
- Supabase connectivity
- Database CRUD operations
- Anti-cheat validation flows

### Manual Testing
- Physical device required for camera testing
- Test various lighting conditions
- Test different body types and positions
- Test network connectivity scenarios

---

## 📱 Device Requirements

**Minimum:**
- Android 8.0 (API 26)
- Rear camera with autofocus
- 2GB RAM
- Internet connection for ranked mode

**Recommended:**
- Android 12+ (API 31)
- 4GB+ RAM
- Good lighting environment
- Stable WiFi/5G for multiplayer

---

## 🎯 Success Metrics

Track these KPIs:
- Daily Active Users (DAU)
- Workout completion rate
- Average reps per session
- Ranked mode participation
- Conversion to Pro subscription
- User retention (D1, D7, D30)

---

## 📞 Support & Resources

- **MediaPipe Docs**: https://ai.google.dev/edge/mediapipe
- **Supabase Docs**: https://supabase.com/docs
- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **LibGDX Wiki**: https://libgdx.com/wiki/
- **Play Integrity**: https://developer.android.com/google/play/integrity

---

Built with ❤️ - Ready to transform push-ups into epic battles!
