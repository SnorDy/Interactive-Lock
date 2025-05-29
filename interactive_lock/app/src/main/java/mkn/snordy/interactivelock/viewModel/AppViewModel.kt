package mkn.snordy.interactivelock.viewModel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mkn.snordy.interactivelock.R
import mkn.snordy.interactivelock.model.AppModel

class AppViewModel(val appModel: AppModel) {
    var isSettingPassword = false
    val name
        get() = appModel.name

    fun runApp(isLockPassed: Boolean) {
        appModel.runApp(isLockPassed)
    }

    fun setPassword(password: String, editor: SharedPreferences.Editor) {
        appModel.setPassword(password, editor)
    }

    suspend fun runSetLockActivity(
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        appModel.runSetLockActivity(context, activityResultLauncher)
    }


    suspend fun runBlockForResult(
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        appModel.runBlockForResult(context, activityResultLauncher)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun drawApp(
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        setCurrentApp: (AppViewModel) -> Unit
    ) {

        Box(
            modifier =
                Modifier
                    .combinedClickable(
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {

                                appModel.runBlockForResult(context, activityResultLauncher)
                            }
                            setCurrentApp(this)
                        },
                        onLongClick = {
                            CoroutineScope(Dispatchers.Main).launch {

                                isSettingPassword = true
                                appModel.runBlockForResult(context, activityResultLauncher)
                            }
                            setCurrentApp(this)
                        },
                    )
                    .size(48.dp),
        ) {
            Image(
                painter = appModel.icon ?: painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = appModel.name,
                modifier = Modifier.size(48.dp),
            )
        }


        Text(
            text = appModel.name,
            color = Color.Black,
            fontSize = 8.sp,
            textAlign = TextAlign.Center,
        )
    }
}