package com.push.dev.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.push.dev.expensetracker.ui.navigation.NavGraph
import com.push.dev.expensetracker.ui.theme.ExpenseTrackerTheme
import com.push.dev.expensetracker.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannel(this)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                NavGraph()
            }
        }
    }
}
