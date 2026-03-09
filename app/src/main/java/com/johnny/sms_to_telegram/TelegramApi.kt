package com.johnny.sms_to_telegram

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TelegramApi {

    suspend fun sendMessage(botToken: String, chatId: String, text: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val url = URL("https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedText")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to send message: $responseCode ${connection.responseMessage}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}