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
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mkn.snordy.interactivelock.model.AppModel
import mkn.snordy.interactivelock.view.AppView
import mkn.snordy.interactivelock.viewModel.AppViewModel

class MainActivity : ComponentActivity() {
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var setPasswordLauncher: ActivityResultLauncher<Intent>
    private lateinit var currentContext: Context
    private lateinit var currentAppViewModel: AppViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("AppLocks", MODE_PRIVATE)
        sharedPreferences.edit().clear().commit()
        editor = sharedPreferences.edit()

        enableEdgeToEdge()
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    if (currentAppViewModel.isSettingPassword) {
                        CoroutineScope(Dispatchers.Main).launch {
                            currentAppViewModel.runSetLockActivity(
                                currentContext,
                                setPasswordLauncher,
                            )
                        }
                        currentAppViewModel.isSettingPassword = false
                    } else {
                        currentAppViewModel.runApp(true)
                        Log.i("MY_LOG", "App is opened")
                    }
                } else {
                    currentAppViewModel.runApp(false)
                    Log.i("MY_LOG", "App opening is CANCELED!")
                }
            }
        setPasswordLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    currentAppViewModel.setPassword(
                        result.data?.getStringExtra("password") ?: "",
                        editor
                    )
                    Toasty.custom(
                        baseContext,
                        "Password for ${currentAppViewModel.name} is changed",
                        R.drawable.success_icon,
                        es.dmoral.toasty.R.color.successColor,
                        2000,
                        true,
                        true
                    ).show()
                } else {
                    Toasty.custom(
                        baseContext,
                        "Password for ${currentAppViewModel.name} is changed",
                        R.drawable.success_icon,
                        es.dmoral.toasty.R.color.successColor,
                        2400,
                        true,
                        true
                    ).show()
                }
            }
        setContent {
            myLauncher(activityResultLauncher)

        }
    }
    fun setCurrentApp(currApp: AppViewModel){currentAppViewModel=currApp}


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

        val appList =
            context.packageManager.queryIntentActivities(intent, 0)
                .filterNotNull()
                .map {
                    AppModel(
                        it.activityInfo.loadLabel(packageManager).toString(),
                        drawableToPainter(it.activityInfo.loadIcon(packageManager)),
                        it.activityInfo.packageName,
                        sharedPreferences,
                        packageManager, context
                    )
                }

        val appListAdapter = AppModelsAdapter(appList)
        val appViewList = appListAdapter.appsList.map { app -> AppView(AppViewModel(app)) }
        drawApp(appViewList, activityResultLauncher = activityResultLauncher)
    }

    fun drawableToPainter(drawable: Drawable): Painter {
        return BitmapPainter(drawable.toBitmap().asImageBitmap())
    }



    @Composable
    fun drawApp(
        appList: List<AppView>,
        modifier: Modifier = Modifier,
        activityResultLauncher: ActivityResultLauncher<Intent>,
    ) {
        val context = LocalContext.current
        LazyVerticalGrid(
            columns = GridCells.Adaptive(84.dp),
            modifier = modifier.fillMaxSize(),
        ) {
            items(appList) { app ->
                app.AppListItem(

                    modifier =
                        Modifier
                            .size(90.dp),
                    context,
                    activityResultLauncher,
                    {setCurrentApp(app.appViewModel)}
                )
                currentAppViewModel= app.appViewModel
                currentContext = context

            }
        }
    }
}
