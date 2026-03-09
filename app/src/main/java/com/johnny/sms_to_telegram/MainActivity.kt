package com.johnny.sms_to_telegram

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.johnny.sms_to_telegram.ui.theme.Sms_to_telegramTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                // Handle permission grant status
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sms_to_telegramTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SmsToTelegramApp()
                }
            }
        }

        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_PHONE_NUMBERS
                    )
                )
            }
        }
    }
}

@Composable
fun SmsToTelegramApp() {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("SmsToTelegramPrefs", Context.MODE_PRIVATE) }
    var botTokenState by remember { mutableStateOf(sharedPref.getString("BOT_TOKEN", "") ?: "") }
    var chatIdState by remember { mutableStateOf(sharedPref.getString("CHAT_ID", "") ?: "") }

    val hasToken = botTokenState.isNotBlank() && chatIdState.isNotBlank()

    var currentScreen by remember { mutableStateOf(if (hasToken) "Main" else "Settings") }

    when (currentScreen) {
        "Main" -> MainScreen(onNavigateToSettings = { currentScreen = "Settings" })
        "Settings" -> SettingsScreen(onSettingsSaved = { botToken, chatId ->
            botTokenState = botToken
            chatIdState = chatId
            currentScreen = "Main"
        })
    }
}


@Composable
fun MainScreen(onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("SmsToTelegramPrefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()
    // TODO: Check Telegram connection status
    val isSmsPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Telegram Connection: Connected") // Placeholder
        Text("SMS Permission: ${if (isSmsPermissionGranted) "Granted" else "Denied"}")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToSettings) {
            Text("Change Telegram Token")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            coroutineScope.launch {
                val botToken = sharedPref.getString("BOT_TOKEN", "") ?: ""
                val chatId = sharedPref.getString("CHAT_ID", "") ?: ""
                val result = TelegramApi.sendMessage(botToken, chatId, "This is a test message from SmsToTelegram.")
                result.fold(
                    onSuccess = {
                        launch { Toast.makeText(context, "Test message sent successfully", Toast.LENGTH_SHORT).show() }
                    },
                    onFailure = {
                        launch { Toast.makeText(context, "Failed to send test message: ${it.message}", Toast.LENGTH_LONG).show() }
                    }
                )
            }
        }) {
            Text("Test Message")
        }
    }
}

@Composable
fun SettingsScreen(onSettingsSaved: (String, String) -> Unit) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("SmsToTelegramPrefs", Context.MODE_PRIVATE)

    var botToken by remember { mutableStateOf(sharedPref.getString("BOT_TOKEN", "") ?: "") }
    var chatId by remember { mutableStateOf(sharedPref.getString("CHAT_ID", "") ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = botToken,
            onValueChange = { botToken = it },
            label = { Text("Telegram Bot Token") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = chatId,
            onValueChange = { chatId = it },
            label = { Text("Telegram Chat ID") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            with(sharedPref.edit()) {
                putString("BOT_TOKEN", botToken)
                putString("CHAT_ID", chatId)
                apply()
            }
            onSettingsSaved(botToken, chatId)
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
        }) {
            Text("Save")
        }
    }
}