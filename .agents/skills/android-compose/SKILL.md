---
name: android-compose
description: Standards and technical guidelines for building the native Kotlin Android application for PharmaCare using Jetpack Compose, Material 3, ViewModel, and Retrofit.
---

# Android Compose Skill

This skill governs the native Android implementation for PharmaCare in `android/PharmaCareAndroid`.

## Architecture Layers
1. **UI Layer**:
   - Built 100% with Jetpack Compose and Material 3.
   - Declarative UI matching PharmaCare brand aesthetics (deep pine green `#12372F`, mint accents, clean cards).
   - Navigation Compose with typed arguments and role-based route guards.
2. **State & Presentation Layer**:
   - Android Architecture Components `ViewModel` exposing `StateFlow<UiState>`.
   - Immutable UI states handling `Loading`, `Success`, and `Error`.
3. **Data & Network Layer**:
   - Repository pattern abstracting network operations.
   - Retrofit 2 + OkHttp client with interceptors for JWT Bearer token authentication.
   - Configurable API base URL defaulting to `http://10.0.2.2:3000/api/` for Android Emulator.
   - JSON serialization (Kotlinx Serialization or Moshi).

## Core Screens
- Login Screen (auth token storage via EncryptedSharedPreferences or DataStore)
- Dashboard Screen (real-time metric summaries)
- Inventory & Batch Management Screen (search, category filtering, expiry alerts)
- Sales & Billing POS Screen (cart, batch selection, atomic checkout)
- Purchases Screen (supplier PO entry)
- Reports & Analytics Screen

## Build and Testing Standards
- Gradle Kotlin DSL (`build.gradle.kts`).
- Compatibility with installed Android Studio and JDK versions.
- Unit testing for ViewModels and integration testing with mock web server where applicable.
