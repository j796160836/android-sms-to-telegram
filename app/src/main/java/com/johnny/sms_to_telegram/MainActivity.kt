package com.johnny.sms_to_telegram

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
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

        startBatteryService()

        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) -> {
                val permissions = mutableListOf(
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.READ_PHONE_STATE
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                requestPermissionLauncher.launch(permissions.toTypedArray())
            }
        }
    }

    private fun startBatteryService() {
        val serviceIntent = Intent(this, BatteryService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
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
    val isSmsPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    val isPhoneStatePermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        powerManager.isIgnoringBatteryOptimizations(context.packageName)
    } else {
        true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
        Text("Device: $deviceName", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        
        val isConfigured = sharedPref.getString("BOT_TOKEN", "").isNullOrBlank().not() && 
                         sharedPref.getString("CHAT_ID", "").isNullOrBlank().not()
        
        Text("Status: ${if (isConfigured) "✅ Configured" else "❌ Not Configured"}")
        Text("SMS Permission: ${if (isSmsPermissionGranted) "Granted" else "Denied"}")
        Text("Phone State Permission: ${if (isPhoneStatePermissionGranted) "Granted" else "Denied"}")
        Text("Battery Optimization: ${if (isIgnoringBatteryOptimizations) "Ignored" else "Active"}")
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToSettings) {
            Text("Change Telegram Token")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            coroutineScope.launch {
                val botToken = sharedPref.getString("BOT_TOKEN", "") ?: ""
                val chatId = sharedPref.getString("CHAT_ID", "") ?: ""
                
                val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                    context.registerReceiver(null, ifilter)
                }
                val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
                
                val statusReport = """
                    🚀 <b>Status Report</b>
                    ━━━━━━━━━━━━━━━
                    📱 <b>Device:</b> $deviceName
                    🔋 <b>Battery:</b> $level%
                    📶 <b>Service:</b> Running
                    ✅ <b>Telegram:</b> Connected
                """.trimIndent()
                
                val result = TelegramApi.sendMessage(botToken, chatId, statusReport)
                result.fold(
                    onSuccess = {
                        launch { Toast.makeText(context, "Status report sent successfully", Toast.LENGTH_SHORT).show() }
                    },
                    onFailure = {
                        launch { Toast.makeText(context, "Failed to send report: ${it.message}", Toast.LENGTH_LONG).show() }
                    }
                )
            }
        }) {
            Text("Send Status Report")
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (!isIgnoringBatteryOptimizations) {
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            }) {
                Text("Disable Battery Optimization")
            }
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