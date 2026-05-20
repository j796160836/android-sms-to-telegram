# SmsToTelegram 📱 🔋

語言：[English](README-en.md) | [繁體中文](README-zh-tw.md)

一個可以自動將手機的 **SMS 簡訊**、**來電通知** 以及 **電池狀態** 轉發到您的 Telegram 機器人的 Android 應用程式。特別適合擁有多台設備或需要遠端監控備用機狀況的使用者。

## 🌟 核心功能

- **📩 簡訊轉發**：即時將接收到的 SMS 轉發至 Telegram，包含發件人與內容。
- **📞 來電提醒**：即時通知未接或正在響鈴的來電號碼。
- **🔋 電池全方位監控**：
  - **低電量警告**：電量低於門檻時自動提醒。
  - **電量恢復通知**：電池脫離低電量狀態時通知。
  - **充滿電提醒**：達到 100% 時通知，保護電池壽命。
  - **電源插拔追蹤**：接上或拔掉充電器時即時回報。
- **📱 多設備識別**：每條訊息均標註手機型號（如 `[Samsung SM-G9910]`），方便管理多台裝置。
- **🛠️ 一鍵狀態報告**：在 App 中點擊即可發送包含目前電量、連線狀態與設備資訊的完整報告。
- **💎 訊息美化**：支援 Telegram HTML 格式，使用粗體、程式碼塊與分隔線，資訊階層清晰。
- **🚀 極致可靠性**：
  - **前台服務 (Foreground Service)**：確保 App 在背景或深度睡眠時依然能穩定運作。
  - **開機啟動**：手機重啟後自動恢復監控服務。

## 🚀 快速開始

### 1. 取得 Telegram Bot 資訊
1. 在 Telegram 中搜尋 [@BotFather](https://t.me/botfather) 並建立一個新機器人，取得 **API Token**。
2. 搜尋 [@userinfobot](https://t.me/userinfobot) 取得您的 **Chat ID**。

### 2. 設定 App
1. 下載並安裝 App。
2. 開啟 App 後，進入設定頁面填入您的 **Bot Token** 與 **Chat ID** 並儲存。
3. 根據系統提示，允許必要的權限（簡訊、電話狀態、通知等）。
4. **建議**：點擊主畫面的「Disable Battery Optimization」，確保系統不會因省電功能而中斷轉發。

## 🛠️ 技術細節

- **開發語言**：Kotlin
- **UI 框架**：Jetpack Compose (Material 3)
- **非同步處理**：Kotlin Coroutines
- **背景運行**：Foreground Service + BroadcastReceiver
- **最低版本需求**：Android 7.0 (API 24) 以上

## 🔒 隱私與安全性

- 本應用程式**僅**會將訊息傳送至您設定的 Telegram Bot。
- 所有資料均保存在您的本地設備中，不會上傳至任何第三方伺服器（除了 Telegram API）。
- 請妥善保管您的 Bot Token，避免洩漏。

## 📜 權限需求

為了確保功能正常，App 需要以下權限：
- `RECEIVE_SMS`: 接收並轉發簡訊。
- `READ_PHONE_STATE`: 監測來電狀態。
- `READ_PHONE_NUMBERS`: 取得本機門號資訊（選用）。
- `INTERNET`: 與 Telegram API 通訊。
- `FOREGROUND_SERVICE`: 確保背景運行不中斷。
- `POST_NOTIFICATIONS`: Android 13+ 顯示服務執行狀態。
- `RECEIVE_BOOT_COMPLETED`: 支援開機自動啟動。

---

LICENSE: MIT

*Developed with ❤️ for better device connectivity.*
