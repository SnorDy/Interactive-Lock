package mkn.snordy.interactivelock.locks

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import es.dmoral.toasty.Toasty
import mkn.snordy.interactivelock.R

class BongoLockActivity : ComponentActivity() {
    var isSetPassword = false
    var realPassword = ""
    var newPassword = "";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val intent = intent
        isSetPassword = intent.getBooleanExtra("set", false)
        realPassword = intent.getStringExtra("password").toString()

        setContent {
            MyScreen()
        }
    }

    @Composable
    fun ImageButtonWithSides(
        leftImage: Painter,
        rightImage: Painter,
        baseImage: Painter,
        twoFingersImage: Painter,
        onLeftClick: () -> Unit,
        onRightClick: () -> Unit,
        onTwoFingerClick: () -> Unit = {},
        modifier: Modifier = Modifier
    ) {

        var currentImage by remember { mutableStateOf(baseImage) } // Текущее изображение
        var touchCount by remember { mutableStateOf(0) }
        var isTwoFingerClickInProgress by remember { mutableStateOf(false) }
        var isLeftClickLaunched by remember { mutableStateOf(false) }
        var isRightClickLaunched by remember { mutableStateOf(false) }
        var width by remember { mutableStateOf(0f) }


        Box(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .onGloballyPositioned { coordinates ->
                    width = coordinates.size.width.toFloat()
                } // Высота кнопки
                .pointerInput(Unit) { // Обработка двух пальцев
                    coroutineScope {
                        while (true) {
                            val event = awaitPointerEventScope {
                                awaitPointerEvent()
                            }

                            val down = event.changes.firstOrNull { it.pressed }
                            val x = down?.position?.x?.toFloat() ?: 0f

                            if (down != null) {
                                touchCount = event.changes.count { it.pressed }
                                if (touchCount == 2 && !isTwoFingerClickInProgress) {
                                    isTwoFingerClickInProgress = true
                                    onTwoFingerClick()
                                    currentImage = twoFingersImage
                                    launch {
                                        delay(200)
                                        isTwoFingerClickInProgress = false
                                        currentImage = baseImage

                                    }
                                } else if (touchCount == 1) {
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

                                    } else if (!isLeftClickLaunched && !isRightClickLaunched) {
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

                            Toasty.custom(
                                baseContext,
                                "The password can't be empty!",
                                R.drawable.bongo_b,
                                es.dmoral.toasty.R.color.errorColor,
                                2000,
                                true,
                                true
                            ).show()
                        } else {
                            setResult(
                                Activity.RESULT_OK,
                                Intent().putExtra("password", "b$newPassword")
                            )
                            finish()
                        }
                    } else if (newPassword == realPassword) {
                        setResult(Activity.RESULT_OK)
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
                    Toasty.custom(
                        baseContext,
                        "RESET",
                        R.drawable.bongo_b,
                        R.color.teal_700,
                        1000,
                        true,
                        true
                    ).show()


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
            twoFingersImage = twoFingersImage,
            onLeftClick = {
                newPassword += "1"
                Log.d("PRESS", "LEFT PRESSED")
            },
            onRightClick = {
                newPassword += "2"
                Log.d("PRESS", "RIGHT PRESSED")
            },
            onTwoFingerClick = {
                newPassword += "3"
                Log.d("PRESS", "TWO PRESSED")
            }
        )
    }

}