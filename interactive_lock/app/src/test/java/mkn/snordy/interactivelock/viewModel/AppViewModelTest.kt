package mkn.snordy.interactivelock.viewModel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mkn.snordy.interactivelock.model.AppModel
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    @Mock
    private lateinit var mockAppModel: AppModel

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockActivityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var viewModel: AppViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.remove(any())).thenReturn(mockEditor)
        whenever(mockEditor.commit()).thenReturn(true)
        whenever(mockAppModel.name).thenReturn("Test App")
        whenever(mockAppModel.packageName).thenReturn("com.test.app")

        viewModel = AppViewModel(mockAppModel, mockSharedPreferences)
    }

    @Test
    fun `test runApp with lock passed removes notification`() {
        val isLockPassed = true
        viewModel.runApp(isLockPassed)
        verify(mockAppModel).runApp(isLockPassed)
        verify(mockSharedPreferences.edit()).remove("com.test.app")
        verify(mockEditor).commit()
    }

    @Test
    fun `test runApp with lock failed does not remove notification`() {
        val isLockPassed = false
        viewModel.runApp(isLockPassed)
        verify(mockAppModel).runApp(isLockPassed)
    }

    @Test
    fun `test setPassword delegates to appModel`() {
        val password = "ttestPassword"
        viewModel.setPassword(password, mockEditor)
        verify(mockAppModel).setPassword(password, mockEditor)
    }

    @Test
    fun `test name property returns appModel name`() {
        val name = viewModel.name
        assert(name == "Test App")
    }

    @Test
    fun `test runSetLockActivity delegates to appModel`() =
        runTest {
            viewModel.runSetLockActivity(mockContext, mockActivityResultLauncher)
            verify(mockAppModel).runSetLockActivity(mockContext, mockActivityResultLauncher)
        }
}
