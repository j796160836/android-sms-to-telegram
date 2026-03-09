package com.johnny.sms_to_telegram

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (smsMessage in messages) {
                val messageBody = smsMessage.messageBody
                val sender = smsMessage.originatingAddress

                // Get the phone number of the device
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val devicePhoneNumber = try {
                    // This requires the READ_PHONE_NUMBERS permission
                    telephonyManager.line1Number
                } catch (e: SecurityException) {
                    "N/A"
                }

                val fullMessage = "From: $sender\nTo: $devicePhoneNumber\nMessage: $messageBody"

                // Get Bot Token and Chat ID from SharedPreferences
                val sharedPref = context.getSharedPreferences("SmsToTelegramPrefs", Context.MODE_PRIVATE)
                val botToken = sharedPref.getString("BOT_TOKEN", "")
                val chatId = sharedPref.getString("CHAT_ID", "")

                if (botToken.isNullOrEmpty() || chatId.isNullOrEmpty()) {
                    Log.e("SmsReceiver", "Bot Token or Chat ID is not set.")
                    return
                }

                sendToTelegram(botToken, chatId, fullMessage)
            }
        }
    }

    private fun sendToTelegram(botToken: String, chatId: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val encodedMessage = URLEncoder.encode(message, "UTF-8")
                val url = URL("https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                val responseCode = urlConnection.responseCode
                Log.d("SmsReceiver", "Telegram response code: $responseCode")
                urlConnection.disconnect()
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error sending to Telegram", e)
            }
        }
    }
}