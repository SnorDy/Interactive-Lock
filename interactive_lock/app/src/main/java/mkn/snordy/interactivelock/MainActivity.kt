package mkn.snordy.interactivelock

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            myLauncher()
        }
    }
}

@Composable
fun myLauncher() {
    val context = LocalContext.current

    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN, null)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)

    Log.i("APPLIST", "START")
    val appList =
        context.packageManager.queryIntentActivities(intent, 0)
            .filterNotNull()
            .map {
                AppModel(
                    it.activityInfo.loadLabel(pm).toString(),
                    drawableToPainter(it.activityInfo.loadIcon(pm)),
                    it.activityInfo.packageName,
                )
            }

    val appListAdapter = AppModelsAdapter(appList)
    Log.i("APPLIST", appList.toString())
    drawApp(appListAdapter, pm)
}

fun drawableToPainter(drawable: Drawable): Painter {
    return BitmapPainter(drawable.toBitmap().asImageBitmap())
}

@Composable
fun appListItem(
    app: AppModel,
    modifier: Modifier,
    packageManager: PackageManager,
    context: Context,
) {
    Column(
        modifier =
            modifier
                .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (app.icon != null) {
            IconButton(
                onClick = {
                    app.runApp(packageManager, context)
                },
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent),
            ) {
                Image(
                    painter = app.icon,
                    contentDescription = app.name,
                    modifier = Modifier.size(48.dp),
                )
            }
        } else {
            IconButton(
                onClick = { app.runApp(packageManager, context) },
                modifier = Modifier.size(48.dp),
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                    ),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = app.name,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
        Log.i("APPLIST", app.name)
        Text(text = app.name, color = Color.Black, fontSize = 8.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun drawApp(
    adapter: AppModelsAdapter,
    packageManager: PackageManager,
    modifier: Modifier = Modifier,
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
            )
        }
    }
}
