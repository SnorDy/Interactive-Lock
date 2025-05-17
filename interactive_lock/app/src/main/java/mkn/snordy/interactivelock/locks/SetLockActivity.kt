package mkn.snordy.interactivelock.locks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import mkn.snordy.interactivelock.MainActivity
import mkn.snordy.interactivelock.R

class SetLockActivity : ComponentActivity() {
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val password = result.data?.getStringExtra("password")
                    setResult(
                        RESULT_OK,
                        Intent(baseContext, MainActivity::class.java).putExtra("password", password),
                    )
                    Log.i("MY_LOG", "Password is changed")
                } else {
                    Log.i("MY_LOG", "Password is not changed")
                }
                finish()
            }
        val intent = intent
        setContent {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        ,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            runBongoForResult(baseContext, activityResultLauncher)
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.bongo_n),
                        contentDescription = "bongo icon",
                        modifier = Modifier.size(256.dp),
                    )
                }
                IconButton(
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            runVoiceForResult(baseContext, activityResultLauncher)
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.mic_icon),
                        contentDescription = "Mic icon",
                        modifier = Modifier.size(128.dp),
                    )
                }
                IconButton(
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            runNoLockForResult(baseContext,activityResultLauncher)
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.no_lock_icon),
                        contentDescription = "No_lock icon",
                        modifier = Modifier.size(128.dp),
                    )
                }
            }
        }
    }

    suspend fun runVoiceForResult(
// запускает голосовой ввод с пометкой установки пароля
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>,
    ): Boolean =
        suspendCancellableCoroutine { continuation ->
            val intent = Intent(context, VoiceActivity::class.java)
            intent.putExtra("set", true)
            activityResultLauncher.launch(intent)
        }
    suspend fun runBongoForResult(
// запускает активность с котиком с пометкой установки пароля
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>,
    ): Boolean =
        suspendCancellableCoroutine { continuation ->
            val intent = Intent(context, BongoLockActivity::class.java)
            intent.putExtra("set", true)
            activityResultLauncher.launch(intent)
        }

    suspend fun runNoLockForResult(
// запускает активность с котиком с пометкой установки пароля
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>,
    ): Boolean =
        suspendCancellableCoroutine { continuation ->
            val intent = Intent(context, NoLockActivity::class.java)
            intent.putExtra("set", true)
            activityResultLauncher.launch(intent)
        }
}
