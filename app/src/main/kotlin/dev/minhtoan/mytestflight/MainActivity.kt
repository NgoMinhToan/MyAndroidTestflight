package dev.minhtoan.mytestflight

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import dev.minhtoan.mytestflight.designsystem.MyTestflightTheme
import dev.minhtoan.mytestflight.domain.update.UpdateController
import dev.minhtoan.mytestflight.update.AppForegroundTracker
import dev.minhtoan.mytestflight.update.UpdateDialogHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var updateController: UpdateController

    @Inject
    lateinit var foregroundTracker: AppForegroundTracker

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* Denial only hides notifications; the dialog still shows every update. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(
                CREAM_ARGB,
                CREAM_ARGB,
            ),
        )
        handleUpdateIntent(intent)
        requestNotificationPermission()

        setContent {
            MyTestflightTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    TestflightApp()
                    UpdateDialogHost(updateController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleUpdateIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        foregroundTracker.onActivityStarted()
    }

    override fun onStop() {
        foregroundTracker.onActivityStopped()
        super.onStop()
    }

    private fun handleUpdateIntent(intent: Intent?) {
        intent ?: return
        when {
            intent.getBooleanExtra(EXTRA_INSTALL_UPDATE, false) -> {
                intent.removeExtra(EXTRA_INSTALL_UPDATE)
                updateController.launchInstaller()
            }

            intent.getBooleanExtra(EXTRA_SHOW_UPDATE, false) -> {
                intent.removeExtra(EXTRA_SHOW_UPDATE)
                updateController.showDialog()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        const val EXTRA_SHOW_UPDATE = "dev.minhtoan.mytestflight.SHOW_UPDATE"
        const val EXTRA_INSTALL_UPDATE = "dev.minhtoan.mytestflight.INSTALL_UPDATE"

        fun updateIntent(context: Context, extra: String): Intent =
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(extra, true)
            }
    }
}

private val CREAM_ARGB = 0xFFFFF1ED.toInt()
