package mkn.snordy.interactivelock.view

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import mkn.snordy.interactivelock.viewModel.AppViewModel

class AppView(val appViewModel: AppViewModel) {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun AppListItem(
        modifier: Modifier,
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        setCurrentApp: (AppViewModel)-> Unit
    ) {
        Column(
            modifier =
                modifier
                    .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            appViewModel.drawApp(context, activityResultLauncher,setCurrentApp)
        }
    }
}