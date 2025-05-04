package mkn.snordy.interactivelock

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.suspendCancellableCoroutine
import mkn.snordy.interactivelock.locks.SetLockActivity
import mkn.snordy.interactivelock.locks.VoiceActivity

enum class LockType {
    VOICE,
    PASSWORD,
    NONE,
}

class AppModel(
    val name: String,
    val icon: Painter?,
    val packageName: String,
    sharedPreferences: SharedPreferences,
) {
    private var lockType = LockType.NONE
    private var stringPassword = "пароль"

    init {
        if (sharedPreferences.contains(packageName)) {
            stringPassword = sharedPreferences.getString(packageName, "").toString()
        }
    }

    override fun toString(): String {
        return name
    }

    fun setPassword(
        newPassword: String,
        editor: Editor,
    ) {
        stringPassword = newPassword.lowercase()
        editor.putString(packageName, stringPassword)
        editor.apply()
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

    suspend fun runSetLockActivity(
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>,
    ): Boolean =
        suspendCancellableCoroutine { continuation ->
            val intent = Intent(context, SetLockActivity::class.java)
            intent.putExtra("app", name)
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
