package mkn.snordy.interactivelock.locks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import mkn.snordy.interactivelock.R
import mkn.snordy.interactivelock.ui.MainActivity

class SetLockActivity : ComponentActivity() {
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val password = result.data?.getStringExtra("password")
                    // возвращаем его в Main
                    setResult(
                        RESULT_OK,
                        Intent(baseContext, MainActivity::class.java).putExtra(
                            "password",
                            password,
                        ),
                    )
                    Log.i("MY_LOG", "Password is changed")
                } else {
                    Log.i("MY_LOG", "Password is not changed")
                }
                finish()
            }
        var btnSize = 64
        setContent {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(30.dp).padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .border(3.dp, shape = RectangleShape, color = Color.Black),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    Alignment.CenterVertically,
                ) {
                    IconButton(
                        modifier = Modifier.size(btnSize.dp),
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                runBongoForResult(baseContext, activityResultLauncher)
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.bongo_n),
                            contentDescription = "bongo icon",
                            modifier = Modifier.size(64.dp),
                        )
                    }

                    Text("Bongo Cat Lock", fontSize = 18.sp)
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .border(3.dp, shape = RectangleShape, color = Color.Black),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    Alignment.CenterVertically,
                ) {
                    IconButton(
                        modifier = Modifier.size(btnSize.dp),
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                runVoiceForResult(baseContext, activityResultLauncher)
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.mic_icon),
                            contentDescription = "Mic icon",
                            modifier = Modifier.size(btnSize.dp),
                        )
                    }
                    Text("Voice Lock", fontSize = 18.sp)
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .border(3.dp, shape = RectangleShape, color = Color.Black),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    Alignment.CenterVertically,
                ) {
                    IconButton(
                        modifier =
                            Modifier
                                .size(btnSize.dp)
                                .offset(x = 5.dp),
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                runTextLockForResult(baseContext, activityResultLauncher)
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.text_lock_icon),
                            contentDescription = "text_lock icon",
                            modifier = Modifier.size(96.dp),
                        )
                    }
                    Text("Text Lock", fontSize = 18.sp)
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .border(3.dp, shape = RectangleShape, color = Color.Black),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    Alignment.CenterVertically,
                ) {
                    IconButton(
                        modifier = Modifier.size(btnSize.dp),
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                runNoLockForResult(baseContext, activityResultLauncher)
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.no_lock_icon),
                            contentDescription = "No_lock icon",
                            modifier = Modifier.size(96.dp),
                        )
                    }
                    Text("No Lock", fontSize = 18.sp)
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

    suspend fun runTextLockForResult(
// запускает активность с текстовой блокировкой с пометкой установки пароля
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>,
    ): Boolean =
        suspendCancellableCoroutine { continuation ->
            val intent = Intent(context, TextLockActivity::class.java)
            intent.putExtra("set", true)
            activityResultLauncher.launch(intent)
        }

    suspend fun runNoLockForResult(
// запускает активность для снятия блокировки
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>,
    ): Boolean =
        suspendCancellableCoroutine { continuation ->
            val intent = Intent(context, NoLockActivity::class.java)
            intent.putExtra("set", true)
            activityResultLauncher.launch(intent)
        }
}
