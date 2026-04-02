package com.johnny.sms_to_telegram

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhoneCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.PHONE_STATE") {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                if (incomingNumber != null) {
                    val sharedPref = context.getSharedPreferences("SmsToTelegramPrefs", Context.MODE_PRIVATE)
                    val botToken = sharedPref.getString("BOT_TOKEN", "") ?: ""
                    val chatId = sharedPref.getString("CHAT_ID", "") ?: ""

                    if (botToken.isNotBlank() && chatId.isNotBlank()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            TelegramApi.sendMessage(botToken, chatId, "Incoming call from: $incomingNumber")
                        }
                    }
                }
            }
        }
    }
}