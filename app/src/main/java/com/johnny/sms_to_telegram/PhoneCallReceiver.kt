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
                        val deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = TelegramApi.sendMessage(botToken, chatId, "📞 <b>[$deviceName]</b>\nIncoming call from: <code>$incomingNumber</code>")
                            result.onFailure { e ->
                                android.util.Log.e("PhoneCallReceiver", "Error sending to Telegram", e)
                            }
                        }
                    }
                }
            }
        }
    }
}