package com.johnny.sms_to_telegram

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
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

                // Identify which SIM actually received this SMS (important for dual-SIM devices,
                // since TelephonyManager.line1Number always reports only the default SIM).
                val devicePhoneNumber = getReceivingSimLabel(context, intent)

                val fullMessage =
                    "<b>From:</b> <code>$sender</code>\n" +
                            "<b>To:</b> <code>$devicePhoneNumber</code>\n" +
                            "<b>Message:</b>\n$messageBody" +
                            "━━━━━━━━━━━━━━━\n" +
                            "📱 <b>Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}</b>\n"


                // Get Bot Token and Chat ID from SharedPreferences
                val sharedPref =
                    context.getSharedPreferences("SmsToTelegramPrefs", Context.MODE_PRIVATE)
                val botToken = sharedPref.getString("BOT_TOKEN", "")
                val chatId = sharedPref.getString("CHAT_ID", "")

                if (botToken.isNullOrEmpty() || chatId.isNullOrEmpty()) {
                    Log.e("SmsReceiver", "Bot Token or Chat ID is not set.")
                    return
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val result = TelegramApi.sendMessage(botToken, chatId, fullMessage)
                    result.onFailure { e ->
                        Log.e("SmsReceiver", "Error sending to Telegram", e)
                    }
                }
            }
        }
    }

    /**
     * Resolves a label for the SIM that actually received this SMS. On dual-SIM devices the
     * SMS broadcast intent carries the receiving subscription id (the exact extra key varies by
     * Android version/OEM), so we look that up instead of TelephonyManager.line1Number, which
     * always returns the default SIM's number regardless of which SIM the message arrived on.
     */
    private fun getReceivingSimLabel(context: Context, intent: Intent): String {
        val subscriptionId = intent.getIntExtra(
            "subscription",
            intent.getIntExtra(
                "android.telephony.extra.SUBSCRIPTION_INDEX",
                intent.getIntExtra("slot", SubscriptionManager.INVALID_SUBSCRIPTION_ID)
            )
        )

        if (subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            try {
                val subscriptionManager =
                    context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val info = subscriptionManager.getActiveSubscriptionInfo(subscriptionId)
                if (info != null) {
                    val slot = info.simSlotIndex + 1
                    val carrier = info.carrierName?.toString()?.takeIf { it.isNotBlank() }
                        ?: info.displayName?.toString()?.takeIf { it.isNotBlank() }
                        ?: "SIM $slot"
                    val number = try {
                        info.number?.takeIf { it.isNotBlank() }
                    } catch (e: SecurityException) {
                        null
                    }
                    return if (number != null) "SIM $slot $carrier ($number)" else "SIM $slot $carrier"
                }
            } catch (e: SecurityException) {
                // Fall through to the default-SIM fallback below.
            }
        }

        return try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.line1Number?.takeIf { it.isNotBlank() } ?: "N/A"
        } catch (e: SecurityException) {
            "N/A"
        }
    }
}