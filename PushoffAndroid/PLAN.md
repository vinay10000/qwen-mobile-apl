# Pushoff Android - Project Plan & Status

## 📱 What is Pushoff?

**Pushoff** is a gamified AI push-up app that turns workouts into RPG battles. Users perform real push-ups detected by on-device pose tracking (MediaPipe), dealing damage to enemies/bosses in various game modes.

### Core Features
- **AI Pose Detection**: Real-time push-up counting and form analysis using MediaPipe Pose Landmarker
- **Game Modes**: Campaign, Speed Challenge, Horde, Survival, Ranked 1v1
- **Ranked System**: ELO-based matchmaking with 7 ranks (Bronze → Grandmaster)
- **Progression**: XP, levels, achievements, streaks, quests, stats, leaderboards
- **Privacy-First**: All camera processing happens on-device; only workout results sent to server

### Tech Stack
```
Kotlin + Jetpack Compose + CameraX + MediaPipe + LibGDX + Supabase + Firebase
```

---

## ✅ What Has Been Built

### 1. Project Structure
- [x] Complete Android project with Gradle build files (`build.gradle.kts`)
- [x] Clean Architecture folders: `core/`, `feature/`, `game/`, `domain/`
- [x] Package structure following MVVM + Repository pattern
- [x] Dependency declarations for all required libraries

### 2. Core Detection Logic
- [x] `PushUpDetector.kt` - State machine for rep detection
  - Elbow angle calculation (shoulder-elbow-wrist)
  - Hip alignment validation
  - Rep quality scoring (depth, alignment, ROM, speed)
  - Quality tiers: Perfect (90-100%), Good (75-89%), Weak (60-74%), Invalid (<60%)
- [x] `PoseLandmarkCalculator.kt` - Joint angle utilities
- [x] `RepQualityScore.kt` - Data models for form metrics

### 3. Camera Integration (Skeleton)
- [x] `PoseCameraController.kt` - CameraX setup with MediaPipe integration points
- [x] `CameraPreviewOverlay.kt` - Compose UI for camera layering
- [x] Permission handling structure

### 4. Game Engine (Placeholder)
- [x] `LibGdxGameWrapper.kt` - Bridge between Compose and LibGDX
- [x] `BossBattleRenderer.kt` - Damage popup logic
- [x] `CombatStateMachine.kt` - HP/damage/XP calculations

### 5. Backend Client (Supabase)
- [x] `SupabaseClient.kt` - Initialized client setup
- [x] `AuthRepository.kt` - Authentication interface
- [x] `WorkoutRepository.kt` - Workout data sync
- [x] `MatchmakingRepository.kt` - ELO and match handling
- [x] Database schema defined in documentation

### 6. UI/UX System
- [x] **Design System**
  - [x] `colors.xml` - Gaming theme (dark purples, cyans, rank colors)
  - [x] `typography.xml` - Text scales (display, headline, body)
  - [x] `dimens.xml` - Spacing system (8dp grid)
  - [x] `themes.xml` - Material Theme definitions
- [x] **Reusable Components**
  - [x] `HealthBar.kt` - Animated HP bar with color transitions
  - [x] `RepCounterRing.kt` - Circular progress with spring animations
  - [x] `DamagePopup.kt` - Floating damage numbers (+crit support)
  - [x] `FormQualityBadge.kt` - PERFECT/GOOD/WEAK/INVALID badges
  - [x] `FormBreakdown.kt` - Detailed metrics visualization
  - [x] `RankBadge.kt` - 7 tier rank badges (Bronze→Grandmaster)
- [x] **Screens**
  - [x] `EnhancedWorkoutScreen.kt` - Complete workout UI with HUD overlay
  - [x] `RankedModeScreen.kt` - Matchmaking UI with ELO display
- [x] **Drawables & Animations**
  - [x] 10 drawable XMLs (buttons, cards, badges, overlays)
  - [x] 5 animation XMLs (fade, slide, scale, damage popup)

### 7. Anti-Cheat System
- [x] `WorkoutSignature.kt` - Compact workout data structure
- [x] `CheatDetectionValidator.kt` - Server-side validation logic
- [x] `PlayIntegrityChecker.kt` - Google Play Integrity integration

### 8. Documentation
- [x] `NEXT_STEPS.md` - Setup guide with SQL schema
- [x] `QUICKSTART.md` - 5-minute quick start
- [x] `UI_UX_IMPROVEMENTS.md` - Design system documentation
- [x] `PLAN.md` - This file

---

## 🚧 What Yet To Be Built

### HIGH PRIORITY (MVP Blockers)

#### 1. MediaPipe Integration Completion
- [ ] Download MediaPipe Pose Landmarker model to `app/src/main/assets/`
- [ ] Implement `PoseLandmarkerProcessor.kt` - Actual MediaPipe inference
- [ ] Connect `PoseCameraController` to `PushUpDetector` with live landmark stream
- [ ] Add landmark confidence filtering
- [ ] Test on physical device with camera permissions

#### 2. Live Workout Flow
- [ ] Wire `EnhancedWorkoutScreen` to live pose detection
- [ ] Implement real-time rep counter updates
- [ ] Connect form quality scoring to damage calculation
- [ ] Add haptic feedback on valid reps
- [ ] Add audio cues (countdown, rep complete, perfect form)

#### 3. Backend Setup
- [ ] Create Supabase project
- [ ] Run SQL schema (provided in `NEXT_STEPS.md`)
- [ ] Configure Row Level Security policies
- [ ] Set up Firebase project (Analytics, Crashlytics, FCM)
- [ ] Add `google-services.json` and `supabase.properties`
- [ ] Implement Edge Functions for ELO calculation

#### 4. Authentication Flow
- [ ] Build Login/Signup screens with Compose
- [ ] Implement email/password + Google Sign-In
- [ ] Add onboarding flow (username, avatar selection)
- [ ] Session management and auto-login

#### 5. Game Loop Implementation
- [ ] Complete LibGDX integration for boss rendering
- [ ] Implement boss attack animations
- [ ] Add particle effects for damage/death
- [ ] Create 5+ boss characters with unique stats
- [ ] Implement combo system (consecutive perfect reps)

### MEDIUM PRIORITY (Core Features)

#### 6. Game Modes
- [ ] **Campaign Mode**: Level progression, boss ladder
- [ ] **Speed Challenge**: 60-second max rep mode
- [ ] **Survival Mode**: Boss attacks back, HP drain mechanic
- [ ] **Horde Mode**: Multiple enemies, AoE damage
- [ ] **Free Workout**: Basic counter without game elements

#### 7. Ranked Mode
- [ ] Implement matchmaking queue logic
- [ ] Build 1v1 battle screen (synchronized events)
- [ ] Add ELO update after matches
- [ ] Create seasonal reset logic
- [ ] Build rank promotion/demotion screens

#### 8. Progression System
- [ ] XP calculation and level-up logic
- [ ] Achievement system (database + UI)
- [ ] Daily/weekly quests
- [ ] Streak tracking with reminders
- [ ] Stats dashboard (total reps, best form, etc.)

#### 9. Leaderboards & Social
- [ ] Global leaderboard screen
- [ ] Friends leaderboard filter
- [ ] Profile screen with stats/history
- [ ] Global chat implementation (Supabase Realtime)

### LOW PRIORITY (Polish & Scale)

#### 10. Monetization
- [ ] Integrate Google Play Billing
- [ ] Implement Pro subscription tiers
- [ ] Add paywall screens
- [ ] Create Pro-only features toggle

#### 11. Advanced Features
- [ ] Custom character skins
- [ ] Voice coaching ("Keep your hips up!")
- [ ] Workout history calendar view
- [ ] Export data (CSV, Health Connect)
- [ ] Apple Watch companion (future iOS port prep)

#### 12. ML Improvements (Post-MVP)
- [ ] Collect anonymized pose sequences
- [ ] Train custom TensorFlow Lite model for form classification
- [ ] Detect specific form issues (hip sag, elbow flare)
- [ ] ONNX Runtime integration

---

## 📋 Step-by-Step Implementation Guide

### Phase 1: Get Pose Detection Working (Week 1)

**Goal**: See live push-up counts on screen

1. **Setup MediaPipe**
   ```bash
   # Download model
   wget https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_full/1/pose_landmarker_full.task -P app/src/main/assets/
   ```

2. **Implement Processor**
   - Create `PoseLandmarkerProcessor.kt` in `core/pose/`
   - Initialize MediaPipe Pose Landmarker with `.task` file
   - Process each camera frame → landmarks list
   - Stream landmarks to `PushUpDetector`

3. **Connect Pipeline**
   - Modify `PoseCameraController` to use processor
   - Update `EnhancedWorkoutScreen` state with rep count
   - Test on device: do push-ups, see counter increment

4. **Validate Form Scoring**
   - Add visual debug overlay showing angles
   - Tune thresholds (elbow < 100° = down, > 155° = up)
   - Test edge cases (partial reps, fast reps, bad form)

### Phase 2: Backend & Auth (Week 2)

**Goal**: User can sign up and save workouts

1. **Supabase Setup**
   - Create project at supabase.com
   - Run SQL from `NEXT_STEPS.md` in SQL Editor
   - Enable Email auth + Google OAuth
   - Copy URL and anon key to `local.properties`

2. **Firebase Setup**
   - Create project at console.firebase.google.com
   - Add Android app (package: com.pushoff.app)
   - Download `google-services.json` → `app/`
   - Enable Analytics, Crashlytics

3. **Auth Screens**
   - Build `LoginScreen.kt`, `SignupScreen.kt`
   - Implement `AuthViewModel` with Supabase Auth
   - Add navigation: Auth → Home

4. **Workout Sync**
   - On workout end, upload `WorkoutResult` to Supabase
   - Fetch user's workout history
   - Display in Profile screen

### Phase 3: Game Loop (Week 3)

**Goal**: Boss takes damage from real push-ups

1. **LibGDX Integration**
   - Add LibGDX dependencies
   - Create `GameActivity` to host LibGDX
   - Implement `BossSprite` with HP bar
   - Trigger damage animation on rep complete

2. **Combat Logic**
   - Map rep quality → damage (Perfect=150, Good=100, etc.)
   - Implement boss HP reduction
   - Add boss death state → victory screen
   - Reward XP and gold

3. **Polish**
   - Add sound effects (punch, hit, victory)
   - Add particle effects (blood, sparks)
   - Screen shake on heavy damage

### Phase 4: Ranked Mode (Week 4)

**Goal**: Players can match and compete

1. **Matchmaking**
   - Implement queue system in `MatchmakingRepository`
   - Use Supabase Realtime for match found notifications
   - Progressive ELO range expansion (±100 → ±150 → ±250)

2. **Battle Flow**
   - Both players start workout simultaneously
   - Upload rep events to server every 5 seconds
   - Server syncs opponent progress
   - First to defeat boss wins (or most damage in time limit)

3. **ELO System**
   - Implement formula in `EloCalculator.kt`
   - Update ratings post-match
   - Handle disconnects/cheats

4. **Leaderboards**
   - Build global/top 100 screen
   - Add rank filters (Bronze, Silver, etc.)
   - Show player's rank and progress

### Phase 5: Polish & Launch Prep (Week 5-6)

**Goal**: App Store ready

1. **Monetization**
   - Integrate Play Billing Library
   - Create subscription products (weekly/monthly/yearly)
   - Implement paywall for Pro features

2. **Anti-Cheat**
   - Enable Play Integrity API
   - Implement server-side cheat detection
   - Add reporting system

3. **Testing**
   - Write unit tests for detectors
   - UI tests for critical flows
   - Beta test with 50 users

4. **Launch**
   - Create Play Store listing
   - Prepare screenshots/videos
   - Submit for review

---

## 🗂️ File Reference

### Core Logic
| File | Purpose | Status |
|------|---------|--------|
| `core/pose/PushUpDetector.kt` | Rep detection state machine | ✅ Done |
| `core/pose/PoseLandmarkCalculator.kt` | Angle calculations | ✅ Done |
| `core/pose/PoseLandmarkerProcessor.kt` | MediaPipe inference | ❌ TODO |
| `core/camera/PoseCameraController.kt` | CameraX setup | ⚠️ Skeleton |

### UI Components
| File | Purpose | Status |
|------|---------|--------|
| `ui/components/HealthBar.kt` | Animated HP bar | ✅ Done |
| `ui/components/RepCounterRing.kt` | Circular rep counter | ✅ Done |
| `ui/components/DamagePopup.kt` | Floating damage numbers | ✅ Done |
| `ui/components/FormQualityBadge.kt` | Form feedback badge | ✅ Done |
| `ui/components/RankBadge.kt` | Rank visualization | ✅ Done |
| `ui/screens/EnhancedWorkoutScreen.kt` | Main workout UI | ✅ Done |
| `ui/screens/RankedModeScreen.kt` | Matchmaking UI | ✅ Done |

### Backend
| File | Purpose | Status |
|------|---------|--------|
| `data/network/SupabaseClient.kt` | Supabase init | ✅ Done |
| `data/repository/AuthRepository.kt` | Auth logic | ⚠️ Interface only |
| `data/repository/WorkoutRepository.kt` | Workout sync | ⚠️ Interface only |
| `data/repository/MatchmakingRepository.kt` | ELO/matches | ⚠️ Interface only |

### Game
| File | Purpose | Status |
|------|---------|--------|
| `game/LibGdxGameWrapper.kt` | LibGDX bridge | ⚠️ Placeholder |
| `game/BossBattleRenderer.kt` | Boss rendering | ⚠️ Placeholder |
| `game/CombatStateMachine.kt` | Combat logic | ✅ Logic done |

### Anti-Cheat
| File | Purpose | Status |
|------|---------|--------|
| `core/security/WorkoutSignature.kt` | Compact workout data | ✅ Done |
| `core/security/CheatDetectionValidator.kt` | Cheat validation | ✅ Done |
| `core/security/PlayIntegrityChecker.kt` | Device integrity | ⚠️ Needs API key |

---

## 🔑 Critical Configuration Needed

### 1. MediaPipe Model
Download to: `app/src/main/assets/pose_landmarker_full.task`
```
URL: https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_full/1/pose_landmarker_full.task
```

### 2. Supabase Credentials
Create `local.properties`:
```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
```

### 3. Firebase Config
Add `app/google-services.json` from Firebase Console

### 4. Play Integrity API
Enable in Google Cloud Console and add SHA-1 fingerprint

---

## 🎯 Success Criteria for Next Agent

**You can consider Phase 1 complete when:**
1. App launches on physical Android device
2. Camera preview shows in `EnhancedWorkoutScreen`
3. Doing a push-up increments the rep counter
4. "PERFECT" badge appears for good form reps
5. Damage numbers popup when boss is hit

**Do NOT proceed to Phase 2 until Phase 1 is verified working.**

---

## 📞 Support & Resources

- **MediaPipe Docs**: https://developers.google.com/mediapipe/solutions/vision/pose_landmarker
- **CameraX Docs**: https://developer.android.com/training/camerax
- **Supabase Kotlin SDK**: https://github.com/supabase-community/supabase-kt
- **LibGDX Wiki**: https://libgdx.com/wiki/
- **Compose Animation**: https://developer.android.com/jetpack/compose/animation

---

*Last Updated: $(date)*
*Project Version: 0.1.0-alpha*
