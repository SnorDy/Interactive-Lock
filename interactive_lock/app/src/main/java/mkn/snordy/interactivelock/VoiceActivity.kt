package mkn.snordy.interactivelock

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale

class VoiceActivity : ComponentActivity() {
    private lateinit var activityLockLauncher: ActivityResultLauncher<Intent>
    private var recognizedText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val realPassword = intent.getStringExtra("password")

        enableEdgeToEdge()

        activityLockLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    recognizedText += result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!![0].lowercase()
                    if (recognizedText == realPassword) {
                        setResult(Activity.RESULT_OK)
                        Log.i("MY_LOG", "App is opened")
                    }
                } else {
                    Log.i("MY_LOG", "App opening is CANCELED!")
                }
                finish()
            }
        voiceInput()
    }

    private fun voiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault(),
        )
        try {
            activityLockLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, " " + e.message, Toast.LENGTH_SHORT).show()
        }
    }
}
