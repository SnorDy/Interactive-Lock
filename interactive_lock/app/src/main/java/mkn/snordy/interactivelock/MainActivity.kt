package mkn.snordy.interactivelock

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var setPasswordLauncher: ActivityResultLauncher<Intent>
    private lateinit var currentContext: Context
    private lateinit var currentAppModel: AppModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: Editor
    private var isSettingPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("AppLocks", MODE_PRIVATE)
        sharedPreferences.edit().clear().commit()
        editor = sharedPreferences.edit()
        enableEdgeToEdge()
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    if (isSettingPassword) {
                        CoroutineScope(Dispatchers.Main).launch {
                            currentAppModel.runSetLockActivity(
                                currentContext,
                                setPasswordLauncher,
                            )
                        }
                        isSettingPassword = false
                    } else {
                        currentAppModel.runApp(packageManager, currentContext, true)
                        Log.i("MY_LOG", "App is opened")
                    }
                } else {
                    currentAppModel.runApp(packageManager, currentContext, false)
                    Log.i("MY_LOG", "App opening is CANCELED!")
                }
            }
        setPasswordLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    currentAppModel.setPassword(result.data?.getStringExtra("password") ?: "", editor)
                    Toast.makeText(
                        baseContext,
                        "Password for ${currentAppModel.packageName} is changed",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    Toast.makeText(
                        baseContext,
                        "Password for ${currentAppModel.name} is changed",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        setContent {
            myLauncher(activityResultLauncher)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI(windowController: WindowInsetsControllerCompat?) {
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
        val windowInsetsController =
            remember(view) {
                WindowCompat.getInsetsController(
                    currentWindow,
                    view,
                )
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hideSystemUI(windowInsetsController)
        }
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        Log.i("APPLIST", "START")
        val appList =
            context.packageManager.queryIntentActivities(intent, 0)
                .filterNotNull()
                .map {
                    AppModel(
                        it.activityInfo.loadLabel(packageManager).toString(),
                        drawableToPainter(it.activityInfo.loadIcon(packageManager)),
                        it.activityInfo.packageName,
                        sharedPreferences,
                    )
                }

        val appListAdapter = AppModelsAdapter(appList)
        Log.i("APPLIST", appList.toString())
        drawApp(appListAdapter, packageManager, activityResultLauncher = activityResultLauncher)
    }

    fun drawableToPainter(drawable: Drawable): Painter {
        return BitmapPainter(drawable.toBitmap().asImageBitmap())
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun appListItem(
        app: AppModel,
        modifier: Modifier,
        packageManager: PackageManager,
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>,
    ) {
        Column(
            modifier =
                modifier
                    .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (app.icon != null) {
                Box(
                    modifier =
                        Modifier
                            .combinedClickable(
                                onClick = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        currentAppModel = app
                                        currentContext = context
                                        app.runBlockForResult(context, activityResultLauncher)
                                    }
                                },
                                onLongClick = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        currentAppModel = app
                                        currentContext = context
                                        isSettingPassword = true
                                        app.runBlockForResult(context, activityResultLauncher)
                                    }
                                },
                            )
                            .size(48.dp),
                ) {
                    Image(
                        painter = app.icon,
                        contentDescription = app.name,
                        modifier = Modifier.size(48.dp),
                    )
                }
            } else {
                Box(
                    modifier =
                        Modifier
                            .combinedClickable(
                                onClick = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        currentAppModel = app
                                        currentContext = context
                                        app.runBlockForResult(context, activityResultLauncher)
                                    }
                                },
                                onLongClick = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        currentAppModel = app
                                        currentContext = context
                                        isSettingPassword = true
                                        app.runBlockForResult(context, activityResultLauncher)
                                    }
                                },
                            )
                            .size(48.dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = app.name,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
            Log.i("APPLIST", app.name)
            Text(
                text = app.name,
                color = Color.Black,
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
            )
        }
    }

    @Composable
    fun drawApp(
        adapter: AppModelsAdapter,
        packageManager: PackageManager,
        modifier: Modifier = Modifier,
        activityResultLauncher: ActivityResultLauncher<Intent>,
    ) {
        val apps = adapter.appsList
        val context = LocalContext.current
        val configuration = LocalConfiguration.current
        Log.i("APPLIST", (configuration.screenWidthDp / 48).toString())
        LazyVerticalGrid(
            columns = GridCells.Adaptive(84.dp),
            modifier = modifier.fillMaxSize(),
        ) {
            items(items = apps) { app ->
                appListItem(
                    app = app,
                    modifier =
                        Modifier
                            .size(90.dp),
                    packageManager = packageManager,
                    context,
                    activityResultLauncher,
                )
            }
        }
    }
}
