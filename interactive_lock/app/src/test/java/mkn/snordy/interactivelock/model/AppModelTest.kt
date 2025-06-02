package mkn.snordy.interactivelock.model

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.painter.Painter
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AppModelTest {
    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Mock
    private lateinit var mockPackageManager: PackageManager

    @Mock
    private lateinit var mockPainter: Painter

    private lateinit var appModel: AppModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.apply()).then { }

        appModel =
            AppModel(
                name = "Test App",
                icon = mockPainter,
                packageName = "com.test.app",
                sharedPreferences = mockSharedPreferences,
                packageM = mockPackageManager,
                context = mockContext,
            )
    }

    @Test
    fun `test setPassword updates shared preferences with correct password format`() {
        val password = "testpassword"
        val expectedPasswordKey = "com.test.app"
        appModel.setPassword(password, mockEditor)
        verify(mockEditor).putString(expectedPasswordKey, password)
        verify(mockEditor).apply()
    }

    @Test
    fun `test getLockType returns correct type for text password`() {
        val password = "testpassword"
        appModel.setPassword(password, mockEditor)
        assert(appModel.getLockType() == LockType.TEXT)
    }

    @Test
    fun `test getLockType returns correct type for voice password`() {
        appModel.setPassword("vtest", mockEditor)
        assert(appModel.getLockType() == LockType.VOICE)
    }

    @Test
    fun `test getLockType returns correct type for bongo password`() {
        appModel.setPassword("b1231", mockEditor)
        assert(appModel.getLockType() == LockType.BONGO)
    }

    @Test
    fun `test getLockType returns NONE for no password`() {
        whenever(mockSharedPreferences.getString("com.test.app", "n")).thenReturn("n")
        assert(appModel.getLockType() == LockType.NONE)
    }
}
