package mkn.snordy.interactivelock.model

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.suspendCancellableCoroutine
import mkn.snordy.interactivelock.customToast.CustomToast
import mkn.snordy.interactivelock.locks.BongoLockActivity
import mkn.snordy.interactivelock.locks.NoLockActivity
import mkn.snordy.interactivelock.locks.SetLockActivity
import mkn.snordy.interactivelock.locks.TextLockActivity
import mkn.snordy.interactivelock.locks.VoiceActivity

enum class LockType {
    VOICE,
    PASSWORD,
    BONGO,
    TEXT,
    NONE,
}

// класс, описывающий приложение
class AppModel(
    val name: String,
    val icon: Painter?,
    val packageName: String,
    sharedPreferences: SharedPreferences,
    packageM: PackageManager,
    private val context: Context,
) {
    private var lockType = LockType.NONE
    private var stringPassword = "0"
    private val packageManager = packageM

    init {
        if (sharedPreferences.contains(packageName)) { // если была установлена блокировка
            var data = sharedPreferences.getString(packageName, "").toString()
            stringPassword = data.substring(1) // получаем пароль
            when (data[0]) { // первый символ указывает на тип блокировки
                'v' -> lockType = LockType.VOICE
                'b' -> lockType = LockType.BONGO
                'p' -> lockType = LockType.PASSWORD
                't' -> lockType = LockType.TEXT
                'n' -> lockType = LockType.NONE
            }
        }
    }

    override fun toString(): String {
        return name
    }

    fun getLockType(): LockType = lockType

    fun setPassword(
        newPassword: String,
        editor: Editor,
    ) {
        stringPassword = newPassword.lowercase().substring(1)
        when (newPassword[0]) {
            'v' -> lockType = LockType.VOICE
            'b' -> lockType = LockType.BONGO
            'p' -> lockType = LockType.PASSWORD
            't' -> lockType = LockType.TEXT
            'n' -> lockType = LockType.NONE
        }
        editor.putString(packageName, newPassword.lowercase())
        editor.apply()
    }

    // запуск соответствующей блокировки при попытке открыть приложение
    suspend fun runBlockForResult(
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>,
    ): Boolean =
        suspendCancellableCoroutine { continuation ->
            var intent = Intent()
            intent =
                when (lockType) {
                    LockType.VOICE -> Intent(context, VoiceActivity::class.java)
                    LockType.BONGO -> Intent(context, BongoLockActivity::class.java)
                    LockType.PASSWORD -> Intent(context, VoiceActivity::class.java)
                    LockType.NONE -> Intent(context, NoLockActivity::class.java)
                    LockType.TEXT -> Intent(context, TextLockActivity::class.java)
                }
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

    fun runApp(isLockPassed: Boolean) {
        if (isLockPassed) { // если блокировка пройдена
            val launchIntent: Intent? =
                packageManager.getLaunchIntentForPackage(
                    packageName,
                ) // получаем интент для запуска текущего приложения
            if (launchIntent != null) {
                try {
                    context.startActivity(launchIntent)
                } catch (e: Exception) {
                    CustomToast.showErrorToast(context, "Failed to launch app.")
                }
            } else {
                CustomToast.showErrorToast(context, "App not found.")
            }
        } else {
            CustomToast.showErrorToast(context, "Password isn't correct")
        }
    }
}
