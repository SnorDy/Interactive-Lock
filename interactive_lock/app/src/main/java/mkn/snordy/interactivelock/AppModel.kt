package mkn.snordy.interactivelock

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.painter.Painter

class AppModel(val name: String, val icon: Painter?, val packageName: String) {
    override fun toString(): String {
        return name
    }

    fun runApp(
        packageManager: PackageManager,
        context: Context,
    ) {
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
    }
}
