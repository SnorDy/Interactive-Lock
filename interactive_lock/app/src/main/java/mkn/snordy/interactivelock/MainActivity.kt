package mkn.snordy.interactivelock.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mkn.snordy.interactivelock.R
import mkn.snordy.interactivelock.customToast.CustomToast
import mkn.snordy.interactivelock.model.AppModel
import mkn.snordy.interactivelock.model.AppModelsAdapter
import mkn.snordy.interactivelock.other.QuestionActivity
import mkn.snordy.interactivelock.view.AppView
import mkn.snordy.interactivelock.viewModel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var setPasswordLauncher: ActivityResultLauncher<Intent>
    private lateinit var currentContext: Context
    private lateinit var currentAppViewModel: AppViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var notifySharedPreferences: SharedPreferences
    private lateinit var questionSharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var notifyEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isDefaultLauncher()) {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        } else {
            sharedPreferences =
                getSharedPreferences(
                    "AppLocks",
                    MODE_PRIVATE,
                ) // хранит типы блокировок для каждого приложения
            questionSharedPreferences =
                getSharedPreferences(
                    "ResetQuestion",
                    MODE_PRIVATE,
                ) // хранит контрольные вопросы с ответами для сброса блокировок
            notifySharedPreferences =
                getSharedPreferences(
                    "Notification",
                    MODE_PRIVATE,
                ) // хранит приложение и флаг true если пришло уведомление
            editor = sharedPreferences.edit()
            notifyEditor = notifySharedPreferences.edit()

            if (!questionSharedPreferences.contains("isFirstLaunch")) { // если запуск первый
                var intent =
                    Intent(
                        baseContext,
                        QuestionActivity::class.java,
                    )
                startActivity(intent) // запускаем активность с контрольными вопросами
            }

            enableEdgeToEdge()
            activityResultLauncher = // отслеживает была ли блокировка пройдена успешно
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == RESULT_OK) { // если да
                        if (currentAppViewModel.isSettingPassword) { // если текущее приложение в режиме установки пароля
                            CoroutineScope(Dispatchers.Main)
                                .launch {
                                    currentAppViewModel.runSetLockActivity(
// запускаем активность с выбором блокировки
                                        currentContext,
                                        setPasswordLauncher,
                                    )
                                }
                            currentAppViewModel.isSettingPassword = false
                        } else { // если нет, то запускаем приложение с флагом, что блокировка пройдена
                            currentAppViewModel.runApp(true)
                            Log.i("MY_LOG", "App is opened")
                        }
                    } else {
                        currentAppViewModel.runApp(false) // если нет, то передаем false, как знак, что блокировка не пройдена
                        Log.i("MY_LOG", "App opening is CANCELED!")
                    }
                }
            setPasswordLauncher = // отслеживает результат установки нового пароля
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == RESULT_OK) {
                        currentAppViewModel.setPassword(
                            result.data?.getStringExtra("password") ?: "",
                            editor,
                        )
                        CustomToast.Companion.showSuccessToast(
                            baseContext,
                            "Password for ${currentAppViewModel.name} is changed",
                        )
                    } else {
                        CustomToast.Companion.showInfoToast(
                            baseContext,
                            "BACK",
                        )
                    }
                }
            setContent {
                myLauncher(activityResultLauncher)
            }
        }
    }

    private fun isDefaultLauncher(): Boolean { // проверка на то,что приложение главный экран по умочанию
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, 0)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    fun setCurrentApp(currApp: AppViewModel) {
        currentAppViewModel = currApp
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI(windowController: WindowInsetsControllerCompat?) { // сокрытие navigation bar
        windowController?.hide(WindowInsetsCompat.Type.systemBars())
        windowController?.hide(WindowInsetsCompat.Type.statusBars())
        windowController?.hide(WindowInsetsCompat.Type.navigationBars())
    }

    @SuppressLint("RememberReturnType")
    @Composable
    fun myLauncher(activityResultLauncher: ActivityResultLauncher<Intent>) {
        val context = LocalContext.current
        val view = LocalView.current
        val currentWindow = (view.context as? Activity)?.window
        val windowInsetsController = // сокрытие navigation bar
            remember(view) {
                WindowCompat.getInsetsController(
                    currentWindow,
                    view,
                )
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hideSystemUI(windowInsetsController)
        }

        val packageManager =
            context.packageManager // объект, хранящий информацию о приложениях на устройстве
        val intent = Intent(Intent.ACTION_MAIN, null) // указывает, что хотим запустить приложение
        intent.addCategory(Intent.CATEGORY_LAUNCHER) // ищем приложения, которые могут быть запущены с главного экрана

        val appList =
            context.packageManager.queryIntentActivities(
                intent,
                0,
            ) // получаем все приложения по интенту
                .filterNotNull().filter {
                    it.activityInfo.loadLabel(packageManager)
                        .toString() != "Interactive Lock" // пропускаем свое
                }
                .map {
                    AppModel(
                        it.activityInfo.loadLabel(packageManager).toString(),
                        drawableToPainter(it.activityInfo.loadIcon(packageManager)),
                        it.activityInfo.packageName,
                        sharedPreferences,
                        packageManager,
                        context,
                    )
                }

        val appListAdapter = AppModelsAdapter(appList)
        val appViewList =
            appListAdapter.appsList.map { app ->
                AppView(
                    AppViewModel(app, notifySharedPreferences),
                )
            }
        menuBar()
        drawApps(appViewList, activityResultLauncher = activityResultLauncher)
    }

    fun drawableToPainter(drawable: Drawable): Painter {
        return BitmapPainter(
            drawable.toBitmap().asImageBitmap(),
        )
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    @Composable
    fun expandedMenu() {
        var isExpanded by remember {
            mutableStateOf(
                false,
            )
        }
        Box(
            modifier =
                Modifier.Companion.offset(
                    x = 5.dp,
                ),
        ) {
            IconButton(
                modifier =
                    Modifier.Companion
                        .size(56.dp)
                        .offset(x = 15.dp),
                onClick = { isExpanded = true },
            ) {
                Icon(
                    painter = painterResource(R.drawable.menu_icon),
                    contentDescription = "menu_icon",
                    modifier = Modifier.Companion.size(36.dp),
                )
            }

            DropdownMenu(
                modifier =
                    Modifier.Companion
                        .background(color = Color.Companion.White)
                        .border(
                            color = Color.Companion.Black,
                            width = 1.dp,
                        ),
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
            ) {
                DropdownMenuItem(
                    onClick = {
                        val intent = Intent(currentContext, QuestionActivity::class.java)
                        intent.putExtra("reset", true)
                        isExpanded = false
                        startActivity(intent)
                    },
                    text = { Text("Reset passwords") },
                )
                Divider(color = Color.Black)
                DropdownMenuItem(
                    onClick = {
                        isExpanded = false
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    text = { Text("Access to notifications") },
                )
                Divider(color = Color.Black)
                DropdownMenuItem(
                    onClick = {
                        isExpanded = false
                        startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                    },
                    text = { Text("Exit") },
                )
            }
        }
    }

    @Composable
    fun menuBar() {
        var currentTime by remember { mutableStateOf(getCurrentTime()) }
        LaunchedEffect(key1 = true) { // запускаем 1 раз корутину, которая отвечает за сокрытие статус бара и обновления текущего времени
            while (true) {
                delay(1000)
                currentTime = getCurrentTime()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.hide(WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE)
                } else {
                    // Для старых версий Android
                    window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
                }
            }
        }
        Row(
            modifier =
                Modifier.Companion
                    .fillMaxWidth()
                    .size(50.dp)
                    .border(1.dp, color = Color.Companion.Black),
        ) {
            Text(
                modifier =
                    Modifier.Companion
                        .offset(x = 130.dp, y = 15.dp)
                        .fillMaxSize(),
                textAlign = TextAlign.Companion.Center,
                text = currentTime,
                fontSize = 22.sp,
            )
        }
        expandedMenu()
    }

    @Composable
    fun drawApps(
        appList: List<AppView>,
        modifier: Modifier = Modifier.Companion,
        activityResultLauncher: ActivityResultLauncher<Intent>,
    ) {
        val context = LocalContext.current
        LazyVerticalGrid(
            columns = GridCells.Adaptive(84.dp),
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(top = 50.dp),
        ) {
            items(appList) { app ->
                app.appListItem(
                    modifier =
                        Modifier.Companion
                            .size(90.dp),
                    context,
                    activityResultLauncher,
                ) { setCurrentApp(app.appViewModel) }
                currentAppViewModel = app.appViewModel
                currentContext = context
            }
        }
    }
}
