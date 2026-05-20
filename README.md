# SmsToTelegram 📱 🔋

Language: [English](README.md) | [繁體中文](README-zh-tw.md)

A powerful and reliable Android application that automatically forwards your phone's **SMS messages**, **incoming call notifications**, and **battery status** to your Telegram bot. Especially useful for users who own multiple devices or need to remotely monitor the status of a secondary phone.

## 🌟 Core Features

- **📩 SMS Forwarding**: Instantly forwards incoming SMS messages to Telegram, including sender and content.
- **📞 Incoming Call Alerts**: Real-time notifications for missed or ringing incoming call numbers.
- **🔋 Comprehensive Battery Monitoring**:
  - **Low Battery Warning**: Automatically alerts you when the battery drops below the threshold.
  - **Battery Recovery Notification**: Notifies you when the battery rises out of the low-battery state.
  - **Full Charge Reminder**: Notifies you when the battery reaches 100%, helping protect battery life.
  - **Power Plug/Unplug Tracking**: Reports in real time when the charger is connected or disconnected.
- **📱 Multi-Device Identification**: Every message is tagged with the phone model (e.g. `[Samsung SM-G9910]`), making it easy to manage multiple devices.
- **🛠️ One-Tap Status Report**: Tap once inside the app to send a complete report including current battery level, connection status, and device information.
- **💎 Beautified Messages**: Supports Telegram HTML formatting with bold text, code blocks, and dividers for a clear information hierarchy.
- **🚀 Maximum Reliability**:
  - **Foreground Service**: Ensures the app keeps running stably in the background or during deep sleep.
  - **Boot Startup**: Automatically resumes monitoring after the phone restarts.

## 🚀 Quick Start

### 1. Get Your Telegram Bot Information
1. Search for [@BotFather](https://t.me/botfather) in Telegram, create a new bot, and obtain the **API Token**.
2. Search for [@userinfobot](https://t.me/userinfobot) to obtain your **Chat ID**.

### 2. Configure the App
1. Download and install the app.
2. Open the app, go to the settings page, fill in your **Bot Token** and **Chat ID**, and save.
3. Follow the system prompts to grant the required permissions (SMS, phone state, notifications, etc.).
4. **Recommended**: Tap "Disable Battery Optimization" on the main screen to ensure forwarding isn't interrupted by the system's power-saving features.

## 🛠️ Technical Details

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Asynchronous Processing**: Kotlin Coroutines
- **Background Execution**: Foreground Service + BroadcastReceiver
- **Minimum Requirement**: Android 7.0 (API 24) or above

## 🔒 Privacy & Security

- This app **only** sends messages to the Telegram bot you have configured.
- All data is stored locally on your device and is never uploaded to any third-party server (except the Telegram API).
- Please keep your Bot Token safe and avoid leaking it.

## 📜 Permission Requirements

To ensure proper functionality, the app requires the following permissions:
- `RECEIVE_SMS`: Receive and forward SMS messages.
- `READ_PHONE_STATE`: Monitor incoming call status.
- `READ_PHONE_NUMBERS`: Retrieve the device's phone number (optional).
- `INTERNET`: Communicate with the Telegram API.
- `FOREGROUND_SERVICE`: Ensure uninterrupted background operation.
- `POST_NOTIFICATIONS`: Display service running status on Android 13+.
- `RECEIVE_BOOT_COMPLETED`: Support automatic startup on boot.

---

LICENSE: MIT

*Developed with ❤️ for better device connectivity.*
