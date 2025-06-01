package mkn.snordy.interactivelock.locks

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mkn.snordy.interactivelock.R
import mkn.snordy.interactivelock.customToast.CustomToast

class BongoLockActivity : ComponentActivity() {
    var isSetPassword = false
    var realPassword = ""
    var newPassword = ""
    val mutex = Mutex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val intent = intent
        newPassword = ""
        isSetPassword = intent.getBooleanExtra("set", false) // проверка на режим установки пароля
        realPassword = intent.getStringExtra("password").toString()
        setContent {
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
            myScreen()
        }
    }

    @SuppressLint("ReturnFromAwaitPointerEventScope")
    @Composable
    fun imageButtonWithSides(
        modifier: Modifier = Modifier,
        leftImage: Painter,
        rightImage: Painter,
        baseImage: Painter,
        swipeImage: Painter,
        onLeftClick: () -> Unit,
        onRightClick: () -> Unit,
        onSwipe: () -> Unit = {},
    ) {
        var currentImage by remember { mutableStateOf(baseImage) } // Текущее изображение
        var touchCount by remember { mutableIntStateOf(0) }
        var isSwipeInProgress by remember { mutableStateOf(false) }
        var isLeftClickLaunched by remember { mutableStateOf(false) }
        var isRightClickLaunched by remember { mutableStateOf(false) }
        var width by remember { mutableFloatStateOf(0f) }

        Box(
            modifier =
                modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .onGloballyPositioned { coordinates ->
                        width = coordinates.size.width.toFloat()
                    }
                    .pointerInput(Unit) { // добавляет обработку ввода к Box
                        coroutineScope {
                            mutex.withLock { // обработка свайпа вверх в отдельной корутине
                                detectVerticalDragGestures(
                                    onDragEnd = { // если движение закончено, то добавляем цифру к паролю и ставим соответствующий флаг
                                        onSwipe()
                                        isSwipeInProgress = false
                                    },
                                    onVerticalDrag = { change, dragAmount -> // вызывается при каждом вертикальном свайпе
                                        if (dragAmount < -50) { // если палец достаточно сильно протащили вверх
                                            isSwipeInProgress = true
                                            currentImage = swipeImage
                                            launch {
                                                delay(200)
                                                currentImage = baseImage
                                            }
                                        }
                                    },
                                )
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        coroutineScope {
                            while (true) {
                                val event =
                                    awaitPointerEventScope {
                                        awaitPointerEvent()
                                    }

                                val down = event.changes.firstOrNull { it.pressed }
                                val x = down?.position?.x?.toFloat() ?: 0f

                                if (down != null) {
                                    touchCount = event.changes.count { it.pressed }
                                    if (touchCount == 1 && !isSwipeInProgress) {
                                        // Обработка одного пальца
                                        if (x < width / 2 && !isLeftClickLaunched && !isRightClickLaunched) {
                                            onLeftClick()
                                            currentImage = leftImage
                                            isLeftClickLaunched = true
                                            launch {
                                                delay(200)
                                                currentImage = baseImage
                                                isLeftClickLaunched = false
                                            }
                                        } else if (!isLeftClickLaunched && !isRightClickLaunched && !isSwipeInProgress) {
                                            onRightClick()
                                            currentImage = rightImage
                                            isRightClickLaunched = true
                                            launch {
                                                delay(200)
                                                currentImage = baseImage
                                                isRightClickLaunched = false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = currentImage,
                contentDescription = "Button Image",
                modifier = Modifier.fillMaxSize(),
            )
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(50.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Button(
                modifier =
                    Modifier
                        .size(100.dp)
                        .background(Color.White),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color.Black),
                onClick = {
                    if (isSetPassword) { // если в режиме установки пароля
                        if (newPassword.isEmpty()) { // и пароль пустой
                            CustomToast.showErrorToast(baseContext, "The password can't be empty!")
                        } else {
                            // если пароль установлен, то возвращаем ОК
                            // и к интенту добавляем пароль, первый символ - тип блокировки
                            setResult(
                                RESULT_OK,
                                Intent().putExtra("password", "b$newPassword"),
                            )
                            finish()
                        }
                    } else if (newPassword == realPassword) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        finish()
                    }
                },
            ) { Text(color = Color.Black, text = "DONE") }

            Button(
                modifier = Modifier.size(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(5.dp),
                border = BorderStroke(2.dp, Color.Black),
                onClick = {
                    newPassword = ""
                    CustomToast.showInfoToast(baseContext, "RESET")
                },
            ) { Text(color = Color.Black, text = "RESET") }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI(windowController: WindowInsetsControllerCompat?) {
        windowController?.hide(WindowInsetsCompat.Type.systemBars())
        windowController?.hide(WindowInsetsCompat.Type.statusBars())
        windowController?.hide(WindowInsetsCompat.Type.navigationBars())
    }

    @Composable
    fun myScreen() {
        val leftImage = painterResource(id = R.drawable.bongo_r)
        val rightImage = painterResource(id = R.drawable.bongo_l)
        val baseImage = painterResource(id = R.drawable.bongo_n)
        val twoFingersImage = painterResource(id = R.drawable.bongo_b)

        imageButtonWithSides(
            leftImage = leftImage,
            rightImage = rightImage,
            baseImage = baseImage,
            swipeImage = twoFingersImage,
            onLeftClick = {
                newPassword += "1"
                Log.i("PRESS", "LEFT PRESSED")
            },
            onRightClick = {
                newPassword += "2"
                Log.i("PRESS", "RIGHT PRESSED")
            },
            onSwipe = {
                newPassword += "3"
                Log.i("PRESS", "TWO PRESSED")
            },
        )
    }
}
