# 🛡️ Call Blocker - Professional Android Call Protection & Blocking System

[English](README.md) | [繁體中文](README_zh-TW.md)

![Android](https://img.shields.io/badge/Android-8.0%2B%20(API%2026%2B)-brightgreen.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-blue.svg)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)
![Architecture](https://img.shields.io/badge/Architecture-MVI%2FMVVM-orange.svg)
![License](https://img.shields.io/badge/Rights-All%20Rights%20Reserved-red.svg)

**Call Blocker** is a powerful, offline-first, and privacy-focused Android call screening and spam blocking application. Built with modern Android technologies including **Android Telecom API (`CallScreeningService`)**, **Jetpack Compose Material 3**, **Room Database**, and **DataStore**, Call Blocker provides real-time protection against unwanted calls, telemarketers, and spam.

---

## ✨ Key Features

### 1. ⚡ Pre-Ring Interception (`CallScreeningService`)
- **System-Level Interception**: Utilizes Android's `CallScreeningService` to evaluate incoming numbers and intercept them before your phone rings or vibrates.
- **Role Integration**: Interoperates with Android's default `ROLE_CALL_SCREENING` to query and sync with native system block lists (`BlockedNumberContract`).

### 2. 🎯 Flexible Interception Actions
- **Reject Call (Hang Up)**: Automatically disconnects blocked callers with a busy signal.
- **Silence Call (Ignore)**: Mutes ringtones and hides incoming call UI without hanging up (Android 10+).
- **Auto-Add Rejected Calls**: Automatically appends rejected or blocked numbers to your custom block list.

### 3. 🔄 Outgoing Call Callback Exemption
- **Highest Priority Exemption**: If you place an outgoing call to a non-contact number, Call Blocker automatically exempts incoming callbacks from that number within a configurable window (**Off**, **1 Day**, **3 Days**, or **1 Week**), overriding all other blocking rules.

### 4. 📱 Smart Multi-SIM International Protection
- **Multi-SIM Awareness**: Dynamically polls all active SIM card country ISOs using `SubscriptionManager`.
- **Accurate International Detection**: Evaluates incoming calls against all active SIM regions to prevent false blocks on dual-SIM devices.

### 5. 🔍 Wildcard Pattern Matching & Dynamic Impact Preview
- **E.164 Standardization**: Formats phone numbers globally using Google `libphonenumber`.
- **Wildcard `*` Prefix Rules**: Supports wildcard patterns (e.g., `+886912*`) to block entire number prefixes.
- **Dynamic Country Code Preview**: Previews affected countries and territories in real-time when entering international prefixes.

### 6. 🔐 Encrypted Backup & Restore (AES-256-GCM)
- **High-Security Backups**: Uses AES-256-GCM encryption with PBKDF2 key derivation (10,000 iterations + 128-bit random salt/IV) to export `.spamdb` backup files containing your custom block list.
- **100% Offline Portability**: Backups can be encrypted and restored locally with a password without transmitting data to external servers.

### 7. 🌐 16-Language Localization & Professional Onboarding
- **Full Localization (i18n)**: 100% localized in 16 languages (Traditional Chinese TW/HK, Simplified Chinese, English, Japanese, Korean, Spanish, French, German, Italian, Portuguese, Russian, Vietnamese, Thai, and Indonesian).
- **In-App Instant Language Switching**: Integrates `AppCompatDelegate.setApplicationLocales()` for instant language switching without app restarts.
- **High-Contrast Dark Theme**: Material 3 elevated dark theme for comfortable legibility and zero hardcoded UI colors.

---

## 🛠️ Project Architecture

The project follows modern Android **MVI / MVVM (Model-View-ViewModel)** architectural principles with **Unidirectional Data Flow (UDF)**:

```
app/src/main/java/io/github/hohojia886/callblocker/
├── CallBlockerApp.kt                  # Application class initializing WorkManager
├── data/
│   ├── db/                            # Room Database (Entities: BlockedNumber, CallHistory, BlockAnalytics, ContactsCache)
│   └── pref/                          # DataStore PreferencesManager (Settings state)
├── service/
│   └── CallBlockerScreeningService.kt # Core real-time call screening interceptor
├── ui/
│   ├── MainActivity.kt                # Single Activity entry point & Role handling
│   ├── MainScreen.kt                  # Scaffold container, bottom nav & Role warning banner
│   ├── onboarding/                    # 3-step Onboarding screen & ViewModel
│   ├── dashboard/                     # Statistics dashboard & analytics charts
│   ├── history/                       # Interception history screen & ViewModel
│   ├── settings/                      # Custom block list management, backup & settings
│   └── theme/                         # Material 3 Compose theme (Color, Theme)
└── util/
    ├── BackupCryptoManager.kt         # AES-256-GCM encryption manager
    ├── ContactPickerHelper.kt         # Contact picker helper
    ├── ContactsSyncWorker.kt          # Background WorkManager contacts sync
    ├── CountryCodeProvider.kt         # ITU-T country codes and impact preview
    ├── NotificationHelper.kt          # System notification manager
    ├── OutgoingCallHelper.kt          # Recent outgoing call log exemption query
    ├── PhoneNumberUtils.kt            # E.164 formatting & wildcard matching
    ├── SystemBlockListExporter.kt     # System block list exporter
    └── SystemBlockListImporter.kt     # System block list importer
```

---

## 💻 Build & Self-Compilation Guide

Due to **Google Play Store's developer identity real-name verification policy changes** (e.g. D-U-N-S number, real-name developer account requirements, personal identity exposure), distributing through the Play Store imposes significant privacy hurdles on independent open projects. 

Instead, **Call Blocker is designed for self-compilation**: anyone can **Fork** this repository, open it in Android Studio, and build their own signed APK in less than 2 minutes—giving you **100% source code transparency, total privacy, zero data tracking, and complete independence**.

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1+) or 2026.x
- **JDK**: Java 17 (JBR 17)
- **Android SDK**: `compileSdk = 37`, `targetSdk = 35`, `minSdk = 26`
- **Gradle**: `9.7.1` (AGP `9.4.1`, Kotlin `2.4.20`, KSP `2.3.12`)

### 1. Fork & Clone Repository
1. Click the **Fork** button at the top right of this GitHub page to fork the repository to your account.
2. Clone your forked repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/CallBlocker.git
   cd CallBlocker
   ```

### 2. Open in Android Studio
1. Launch Android Studio.
2. Choose **Open** and select the cloned `CallBlocker` folder.
3. Allow Gradle sync to complete.

### 3. One-Click Build (`Build_APKs.bat`)
The repository includes an embedded debug keystore (`app/keystore/debug.keystore`). You can build both Debug and Release APKs out-of-the-box without manual keystore setup:
- On Windows: Double-click **`Build_APKs.bat`** in the project root folder.
- Or run in terminal:
  ```bash
  # Run Unit Tests
  ./gradlew :app:testDebugUnitTest

  # Build Debug & Release APKs
  ./gradlew :app:assembleDebug :app:assembleRelease
  ```
- **Output Files**:
  - Debug APK: `app/build/outputs/apk/debug/call-blocker-v1.0.0-debug.apk`
  - Release APK (with R8 enabled): `app/build/outputs/apk/release/call-blocker-v1.0.0-release.apk`

### 4. One-Click Install to Phone (`Install_APK.bat`)
1. Connect your Android phone to your computer via USB and enable **USB Debugging**.
2. Double-click **`Install_APK.bat`** in the project root folder.
3. The script automatically detects Android Studio's built-in `adb.exe` path and provides an interactive menu:
   - Option `[1]`: Install Debug APK
   - Option `[2]`: Install Release APK (Recommended)
   - Option `[3]`: Exit

---

## 🔒 Privacy & Security

**Call Blocker is 100% offline and privacy-focused:**
- **Zero Network Access**: The `AndroidManifest.xml` contains **no `INTERNET` permission**. It is physically impossible for the app to send data anywhere.
- **Zero Logcat Output**: Contains zero `Log` calls, ensuring no phone numbers or interception logs leak into system logs.
- **Local Data Only**: All blocking rules, call history, and analytics are stored strictly in private local SQLite storage on your phone.

---

## 📄 Copyright & Terms of Use

Copyright © 2026 Call Blocker. All Rights Reserved.

This GitHub repository is publicly hosted for personal learning, code review, and self-compilation.
No software usage license is currently granted. Without explicit permission from the author, this project or its source code may not be used for commercial distribution, re-packaging, re-licensing, or integration into other products or services.
Without explicit permission from the author, this project or its modified versions may not be published or uploaded to Google Play, Amazon Appstore, or other third-party application stores.
You are welcome to Fork this repository on GitHub for personal research, modification, and self-compilation; however, Forking does not constitute obtaining a license for commercial use or redistribution.
If you require authorization for other purposes, please contact the author first.
