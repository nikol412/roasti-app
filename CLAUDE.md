# Roasti — KMP Coffee App

## Overview
Coffee community app (BrewGuide concept). Social feed, recipes, profiles.
Stack: Kotlin Multiplatform + Jetpack Compose (Android) + SwiftUI (iOS, planned).

## Targets
- **Android** — active development (Jetpack Compose)
- **iOS** — planned (SwiftUI, not yet configured)

## Architecture
- **composeApp** — Android UI module (Jetpack Compose). All UI lives in `androidMain`.
- **shared** — KMP business logic (models, repositories, network). Shared between Android and iOS via `commonMain`.
- **iosApp** — Xcode project, will use SwiftUI + shared module.

## What lives where

### composeApp/androidMain ✅
- UI screens (Jetpack Compose)
- Navigation (androidx.navigation:navigation-compose)
- Theme (Color, Type, Shape, Spacing)
- ViewModels (androidx.lifecycle)
- App entry point (App.kt)

### shared/commonMain ✅
- Domain models
- Repository interfaces
- Business logic
- Platform-agnostic networking

### shared/androidMain / shared/iosMain ⚠️
- Platform-specific implementations (camera, permissions, etc.)

### iosApp (Swift/Xcode) ⚠️
- SwiftUI screens (future)
- Calls shared module via Kotlin/Native bridge

## Key libraries (Android)
- Navigation: `androidx.navigation:navigation-compose` 2.8.9
- Lifecycle/ViewModel: `org.jetbrains.androidx.lifecycle` 2.9.6
- Compose: 1.8.1, Material3: 1.3.2

## Design tokens (from Figma / theme.css)
- Primary brand color: Orange600 `#EA580C`
- Neutral palette: Stone (warm, coffee-themed)
- Base font size: 16sp, line-height 1.5
- Base radius: 10dp, base spacing: 4dp

## Navigation structure
```
Login
└─ Main
   ├─ Feed (tab 1)
   ├─ Recipes (tab 2)
   └─ Profile (tab 3)
```

## Package structure
```
org.nikol.roasti (androidMain)
  App.kt
  navigation/     — AppNavHost, Routes
  ui/
    screens/      — LoginScreen, FeedScreen, RecipesScreen, ProfileScreen
    components/   — BottomBar
    theme/        — Color, Type, Shape, Spacing, Theme
```
