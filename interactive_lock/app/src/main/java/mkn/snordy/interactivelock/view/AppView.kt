package mkn.snordy.interactivelock.view

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mkn.snordy.interactivelock.viewModel.AppViewModel

class AppView(val appViewModel: AppViewModel) {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun appListItem(
        modifier: Modifier,
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        setCurrentApp: (AppViewModel) -> Unit,
    ) {
        Column(
            modifier =
                modifier
                    .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            appViewModel.drawApp(context, activityResultLauncher, setCurrentApp)
        }
    }
}
