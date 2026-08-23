package com.johnny.sms_to_telegram

import android.content.Context
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager

object SimInfoUtils {

    /**
     * Slot number and phone number are kept separate from the carrier name since the carrier
     * name is often the roaming network's name rather than the home carrier, and can run long
     * enough that it needs its own line in the UI.
     */
    data class SimLabel(val primary: String, val carrier: String?) {
        fun toSingleLine(): String = if (carrier != null) "$primary - $carrier" else primary
        fun toTwoLines(): String = if (carrier != null) "$primary\n$carrier" else primary
    }

    fun formatLabel(info: SubscriptionInfo): SimLabel {
        val slot = info.simSlotIndex + 1
        val carrier = info.carrierName?.toString()?.takeIf { it.isNotBlank() }
            ?: info.displayName?.toString()?.takeIf { it.isNotBlank() }
        val number = try {
            info.number?.takeIf { it.isNotBlank() }
        } catch (e: SecurityException) {
            null
        }
        val primary = if (number != null) "SIM $slot ($number)" else "SIM $slot"
        return SimLabel(primary, carrier)
    }

    /** Labels for every active SIM, ordered by slot. Requires READ_PHONE_STATE. */
    fun getActiveSimLabels(context: Context): List<SimLabel> {
        return try {
            val subscriptionManager =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            subscriptionManager.activeSubscriptionInfoList
                ?.sortedBy { it.simSlotIndex }
                ?.map { formatLabel(it) }
                ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }
}
