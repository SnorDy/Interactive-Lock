package mkn.snordy.interactivelock

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.suspendCancellableCoroutine

enum class LockType {
    VOICE,
    PASSWORD,
    NONE,
}

class AppModel(val name: String, val icon: Painter?, val packageName: String) {
    private var lockType = LockType.NONE
    private var stringPassword = "пароль"

    override fun toString(): String {
        return name
    }

    fun setLockType(type: LockType) {
        lockType = type
    }

    suspend fun runBlockForResult(
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>,
    ): Boolean =
        suspendCancellableCoroutine { continuation ->
            val intent = Intent(context, VoiceActivity::class.java)
            intent.putExtra("password", stringPassword)
            activityResultLauncher.launch(intent)
        }

    fun runApp(
        packageManager: PackageManager,
        context: Context,
        isLockPassed: Boolean,
    ) {
        if (isLockPassed) {
            val launchIntent: Intent? = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                try {
                    context.startActivity(launchIntent)
                } catch (e: Exception) {
                    Log.e("LaunchApp", "Error launching app: ${e.message}")
                    Toast.makeText(context, "Failed to launch app.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("LaunchApp", "App not found with package: $packageName")
                Toast.makeText(context, "App not found.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Password isn't correct", Toast.LENGTH_SHORT).show()
        }
    }
}
