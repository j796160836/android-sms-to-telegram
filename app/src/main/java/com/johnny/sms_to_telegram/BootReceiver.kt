package com.johnny.sms_to_telegram

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            val sharedPref = context.getSharedPreferences("SmsToTelegramPrefs", Context.MODE_PRIVATE)
            val botToken = sharedPref.getString("BOT_TOKEN", "") ?: ""
            val chatId = sharedPref.getString("CHAT_ID", "") ?: ""

            if (botToken.isNotBlank() && chatId.isNotBlank()) {
                val serviceIntent = Intent(context, BatteryService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }

                CoroutineScope(Dispatchers.IO).launch {
                    TelegramApi.sendMessage(botToken, chatId, "SmsToTelegram App started after device boot.")
                }
            }
        }
    }
}