# 🛡️ Call Blocker - 專業級 Android 通話防護與騷擾攔截系統

[English](README.md) | [繁體中文](README_zh-TW.md)

![Android](https://img.shields.io/badge/Android-16%2B%20(API%2026%2B)-brightgreen.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-blue.svg)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)
![Architecture](https://img.shields.io/badge/Architecture-MVI%2FMVVM-orange.svg)
![License](https://img.shields.io/badge/Rights-All%20Rights%20Reserved-red.svg)

**Call Blocker** 是一款功能強大、完全本機離線運作且高度重視個人隱私的 Android 來電篩選與騷擾攔截應用程式。採用現代化 Android 技術打造，包含 **Android 電信系統 API (`CallScreeningService`)**、**Jetpack Compose Material 3**、**Room Database** 與 **DataStore**，為您提供即時、精準的垃圾來電與推銷電話防護。

---

## ✨ 核心功能特色

### 1. ⚡ 響鈴前精準攔截 (`CallScreeningService`)
- **系統級來電篩選**：採用 Android 官方 `CallScreeningService` API，在手機響鈴或震動前，即時比對來電號碼並執行攔截。
- **系統角色相容**：完美相容 Android `ROLE_CALL_SCREENING` 角色，支援讀取與同步原廠 Phone App 的原生封鎖名單 (`BlockedNumberContract`)。

### 2. 🎯 彈性的攔截處置動作
- **直接掛斷 (Reject)**：自動拒接封鎖來電並傳送忙線訊號。
- **僅靜音 (Silence)**：關閉來電響鈴並隱藏來電畫面，不主動掛斷，讓對方自然響完（Android 10+）。
- **自動加入封鎖名單**：可開啟自動將拒接或手動掛斷之號碼加入自訂封鎖名單。

### 3. 🔄 近期主動撥出電話回撥豁免
- **最高優先級豁免**：若您近期曾主動撥打電話給非聯絡人，系統允許對方在指定期限（**1 天內**、**3 天內**或 **1 週內**）撥入回電，自動無視所有攔截規則（包含非聯絡人、黑名單、國外來電等）。

### 4. 📱 智慧 Multi-SIM 國際電話防護
- **多卡狀態偵測**：透過 `SubscriptionManager` 動態輪詢手機中所有已安裝 SIM 卡的國家 ISO 碼。
- **精準跨國判斷**：比對來電號碼是否屬於任一 SIM 卡之本地國碼，有效避免雙卡雙待手機誤擋跨國來電。

### 5. 🔍 萬用字元前綴比對與動態影響範圍預覽
- **E.164 國際標準化**：整合 Google `libphonenumber` 進行全球電話號碼標準化格式轉換。
- **萬用字元 `*` 規則**：支援萬用字元前綴（如 `+886912*`），可一鍵封鎖整個號碼區段。
- **國碼動態影響預覽**：輸入國際前綴時，即時動態預覽預計受影響的國家與地區。

### 6. 🔐 加密備份與還原 (AES-256-GCM)
- **高強度加密備份**：採用 `AES-256-GCM` 配合 `PBKDF2` 密碼衍生演算法（10,000 次反覆運算與獨立隨機 Salt/IV），將自訂封鎖名單匯出為 `.spamdb` 加密備份檔。
- **100% 本機可攜性**：備份與還原全程於本機完成，完全不需將數據上傳至任何雲端或第三方伺服器。

### 7. 🌐 16 國語言支援與專業深色主題
- **多國語言在地化 (i18n)**：100% 完整支援 16 種語言（繁體中文台/港、簡體中文、英文、日文、韓文、西班牙文、法文、德文、義大利文、葡萄牙文、俄文、越南文、泰文、印尼文）。
- **App 內即時語言切換**：整合 `AppCompatDelegate.setApplicationLocales()`，無需重啟 App 即可即時切換界面語言。
- **高對比深色主題**：採用 Material 3 階層式深色主題，完全無硬編碼色彩，夜間閱讀舒適不刺眼。

---

## 🛠️ 專案架構 (Project Architecture)

本專案遵循現代化 Android **MVI / MVVM (Model-View-ViewModel)** 架構與 **單向資料流 (UDF)** 設計原則：

```
app/src/main/java/io/github/hohojia886/callblocker/
├── CallBlockerApp.kt                  # Application 類別與 WorkManager 初始化
├── data/
│   ├── db/                            # Room Database (Entities, DAOs, Database)
│   └── pref/                          # DataStore PreferencesManager (偏好設定)
├── service/
│   └── CallBlockerScreeningService.kt # 核心即時來電篩選攔截服務
├── ui/
│   ├── MainActivity.kt                # 單一 Activity 入口與系統角色調用
│   ├── MainScreen.kt                  # 底部導覽列、Scaffold 容器與預設角色提醒橫幅
│   ├── onboarding/                    # 3 步驟開頭引導畫面與 ViewModel
│   ├── dashboard/                     # 通話防護儀表板與統計圖表
│   ├── history/                       # 攔截紀錄畫面與 ViewModel
│   ├── settings/                      # 自訂封鎖名單管理、加密備份與設定
│   └── theme/                         # Material 3 Compose 主題設定 (Color, Theme)
└── util/
    ├── BackupCryptoManager.kt         # AES-256-GCM 加密備份管理員
    ├── ContactPickerHelper.kt         # 通訊錄選擇輔助工具
    ├── ContactsSyncWorker.kt          # 背景 WorkManager 通訊錄快取同步
    ├── CountryCodeProvider.kt         # ITU-T 國際國碼與影響預覽工具
    ├── NotificationHelper.kt          # 系統通知管理員
    ├── OutgoingCallHelper.kt          # 近期主動撥出紀錄查詢工具
    ├── PhoneNumberUtils.kt            # E.164 格式化與萬用字元比對工具
    ├── SystemBlockListExporter.kt     # 系統封鎖名單匯出工具
    └── SystemBlockListImporter.kt     # 系統封鎖名單匯入工具
```

---

## 💻 建置與自行編譯指引 (Build & Self-Compilation)

因應 **Google Play 商店近期推行的開發者實名認證政策改變**（例如企業/個人開發者需提供 D-U-N-S 企業編碼、個人真實身份證件與地址公開等），於 Google Play 上架對於獨立開發專案造成顯著的個人隱私風險。

因此，**Call Blocker 採用完全開放自行編譯（Self-Compilation）模式**：任何使用者均可 **Fork** 本專案，使用 Android Studio 在 2 分鐘內編譯出屬於自己的應用程式 APK，確保 **100% 原始碼透明、零數據追蹤，且完全不受 Google Play 上架政策限制與個人實名隱私曝光**。

### 環境需求
- **Android Studio**：Ladybug (2024.2.1+) 或 2026.x 以上版本
- **JDK**：Java 17 (JBR 17)
- **Android SDK**：`compileSdk = 37`, `targetSdk = 35`, `minSdk = 26`
- **Gradle**：`9.7.1` (AGP `9.4.1`, Kotlin `2.4.20`, KSP `2.3.12`)

### 1. Fork 與 Clone 專案
1. 點擊本 GitHub 頁面右上角之 **Fork** 按鈕，將專案複製至您的 GitHub 帳號下。
2. 將您 Fork 的專案 Clone 至本機：
   ```bash
   git clone https://github.com/YOUR_USERNAME/CallBlocker.git
   cd CallBlocker
   ```

### 2. 在 Android Studio 中開啟專案
1. 開啟 Android Studio。
2. 點擊 **Open** 並選擇 Clone 下來的 `CallBlocker` 資料夾。
3. 等待 Gradle Sync 完成。

### 3. 一鍵式腳本自動編譯 (`Build_APKs.bat`)
專案已內建預先配置好的 Debug 金鑰 (`app/keystore/debug.keystore`)，所有人 Clone 專案後無需手動設定任何密鑰即可直接編譯 Debug 與 Release 版本：
- 在 Windows 環境下，直接雙擊專案根目錄下的 **`Build_APKs.bat`** 即可自動完成編譯。
- 或於終端機執行：
  ```bash
  # 執行單元測試
  ./gradlew :app:testDebugUnitTest

  # 自動編譯 Debug 與 Release APK
  ./gradlew :app:assembleDebug :app:assembleRelease
  ```
- **產出檔案路徑**：
  - Debug APK: `app/build/outputs/apk/debug/call-blocker-v1.0.0-debug.apk`
  - Release APK (開啟 R8 混淆與壓縮): `app/build/outputs/apk/release/call-blocker-v1.0.0-release.apk`

### 4. 一鍵式 ADB 安裝至手機 (`Install_APK.bat`)
1. 將您的 Android 手機透過 USB 連接至電腦，並開啟 **USB 偵錯 (USB Debugging)**。
2. 雙擊專案根目錄下的 **`Install_APK.bat`**。
3. 腳本會自動尋找 Android Studio 內建的 `adb.exe` 並提供互動式選單：
   - 選項 `[1]`：安裝 Debug APK
   - 選項 `[2]`：安裝 Release APK (推薦)
   - 選項 `[3]`：離開

---

## 🔒 隱私與安全性聲明 (Privacy & Security)

**Call Blocker 保證 100% 離線與極致隱私保護：**
- **零網路權限**：`AndroidManifest.xml` 中 **完全不包含 `INTERNET` 權限**，程式碼物理上無法傳送任何數據至外部。
- **零 Logcat 輸出**：原始碼 0 處 `Log` 調用，確保來電號碼與攔截紀錄不會洩漏至系統日誌。
- **數據全本機儲存**：所有封鎖名單、通話紀錄與統計圖表僅存於您手機內部的私人 SQLite 資料庫中。

---

## 📄 版權與使用條款 (Copyright & Terms of Use)

Copyright © 2026 Call Blocker. All Rights Reserved. 保留所有權利。

本 GitHub 儲存庫公開供個人學習、程式碼審查及自行編譯使用。
目前未授予任何軟體使用授權。 未經作者明確許可，不得將本專案或其程式碼用於商業發行、重新打包、再授權，或將其整合至其他產品或服務。
未經作者明確許可，不得將本專案或其修改版本上架至 Google Play、Amazon Appstore 或其他第三方應用程式商店。
您可以在 GitHub 上 Fork 本專案供個人研究、修改及自行編譯；但 Fork 並不代表取得商業使用或重新發布的授權。
如需其他用途的授權，請先聯絡作者。
