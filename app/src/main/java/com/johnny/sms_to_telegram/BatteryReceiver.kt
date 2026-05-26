package com.johnny.sms_to_telegram

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatteryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val sharedPref = context.getSharedPreferences("SmsToTelegramPrefs", Context.MODE_PRIVATE)
        val botToken = sharedPref.getString("BOT_TOKEN", "") ?: ""
        val chatId = sharedPref.getString("CHAT_ID", "") ?: ""

        if (botToken.isBlank() || chatId.isBlank()) {
            Log.e("BatteryReceiver", "Bot Token or Chat ID is not set.")
            return
        }

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else -1
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"

        val message = when (action) {
            Intent.ACTION_BATTERY_LOW -> "⚠️ <b>[$deviceName]</b>\nLow Battery Warning! Level: <b>$batteryPct%</b>\nPlease charge your device."
            Intent.ACTION_BATTERY_OKAY -> "✅ <b>[$deviceName]</b>\nBattery OK. Level: <b>$batteryPct%</b>\nDevice is sufficiently charged."
            Intent.ACTION_POWER_CONNECTED -> "🔌 <b>[$deviceName]</b>\nPower Connected. Charging started (Level: <b>$batteryPct%</b>)."
            Intent.ACTION_POWER_DISCONNECTED -> {
                // Reset so a future full charge notifies again.
                sharedPref.edit().putBoolean("BATTERY_FULL_NOTIFIED", false).apply()
                "🔋 <b>[$deviceName]</b>\nPower Disconnected. Charging stopped (Level: <b>$batteryPct%</b>)."
            }
            Intent.ACTION_BATTERY_CHANGED -> {
                val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val isFull = status == BatteryManager.BATTERY_STATUS_FULL && batteryPct == 100
                if (isFull) {
                    // BATTERY_CHANGED fires repeatedly while plugged in at full charge;
                    // only notify on the first transition into the full state.
                    if (sharedPref.getBoolean("BATTERY_FULL_NOTIFIED", false)) {
                        return
                    }
                    sharedPref.edit().putBoolean("BATTERY_FULL_NOTIFIED", true).apply()
                    "⚡ <b>[$deviceName]</b>\nBattery Full! <b>100%</b>\nYou can unplug the charger."
                } else {
                    // Left the full state; allow the next full charge to notify.
                    sharedPref.edit().putBoolean("BATTERY_FULL_NOTIFIED", false).apply()
                    return
                }
            }
            else -> return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val result = TelegramApi.sendMessage(botToken, chatId, message)
            result.onFailure { e ->
                Log.e("BatteryReceiver", "Error sending to Telegram", e)
            }
        }
    }
}