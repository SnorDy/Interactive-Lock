package mkn.snordy.interactivelock.locks

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
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
        isSetPassword = intent.getBooleanExtra("set", false)
        realPassword = intent.getStringExtra("password").toString()

        setContent {
            MyScreen()
        }
    }

    @SuppressLint("ReturnFromAwaitPointerEventScope")
    @Composable
    fun ImageButtonWithSides(
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
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .onGloballyPositioned { coordinates ->
                    width = coordinates.size.width.toFloat()
                } // Высота кнопки
                .pointerInput(Unit) {
                    coroutineScope {
                        mutex.withLock {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    onSwipe()
                                    isSwipeInProgress = false
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    if (dragAmount < -50) {
                                        isSwipeInProgress = true

                                        currentImage = swipeImage
                                        launch {
                                            delay(200)

                                            currentImage = baseImage

                                        }
                                    }
                                })
                        }
                    }
                }
                .pointerInput(Unit) {
                    coroutineScope {
                        while (true) {
                            val event = awaitPointerEventScope {
                                awaitPointerEvent()
                            }

                            val down = event.changes.firstOrNull { it.pressed }
                            val x = down?.position?.x?.toFloat() ?: 0f

                            if (down != null) {
                                touchCount = event.changes.count { it.pressed }
//
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
            contentAlignment = Alignment.Center
        ) {
            // Картинка
            Image(
                painter = currentImage,
                contentDescription = "Button Image",
                modifier = Modifier.fillMaxSize()
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(50.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),

                border = BorderStroke(2.dp, Color.Black),
                onClick = {
                    if (isSetPassword) {
                        if (newPassword.isEmpty()) {
                            CustomToast.showErrorToast(baseContext, "The password can't be empty!")
                        } else {
                            setResult(
                                RESULT_OK,
                                Intent().putExtra("password", "b$newPassword")
                            )
                            finish()
                        }
                    } else if (newPassword == realPassword) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        finish()
                    }
                }) { Text(color = Color.Black, text = "DONE") }

            Button(
                modifier = Modifier.size(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(5.dp),
                border = BorderStroke(2.dp, Color.Black),
                onClick = {
                    newPassword = ""
                    CustomToast.showInfoToast(baseContext, "RESET")

                }
            ) { Text(color = Color.Black, text = "RESET") }
        }
    }

    @Composable
    fun MyScreen() {
        val leftImage = painterResource(id = R.drawable.bongo_r)
        val rightImage = painterResource(id = R.drawable.bongo_l)
        val baseImage = painterResource(id = R.drawable.bongo_n)
        val twoFingersImage = painterResource(id = R.drawable.bongo_b)

        ImageButtonWithSides(
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
            }
        )
    }

}