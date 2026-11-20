package com.mycompany.testflight

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mycompany.testflight.designsystem.MyTestflightTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(
                CREAM_ARGB,
                CREAM_ARGB,
            ),
        )
        setContent {
            MyTestflightTheme {
                TestflightApp()
            }
        }
    }
}

private val CREAM_ARGB = 0xFFFFF1ED.toInt()
