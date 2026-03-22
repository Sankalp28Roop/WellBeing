# Wellbeing Android Clone

Production-grade Digital Wellbeing clone built with Kotlin and Jetpack Compose.

## Architecture
- MVVM + Clean Architecture
- Hilt for Dependency Injection
- Room for Persistent Storage
- WorkManager for Background Tasks
- Jetpack Compose for UI

## Setup
1. Open this project in Android Studio (Koala or later).
2. Build and run on a device/emulator running Android 10+ (API 29).
3. Follow the Onboarding flow to grant necessary permissions.

## System Features (ADB)
```bash
adb shell pm grant com.wellbeing android.permission.WRITE_SECURE_SETTINGS
```
