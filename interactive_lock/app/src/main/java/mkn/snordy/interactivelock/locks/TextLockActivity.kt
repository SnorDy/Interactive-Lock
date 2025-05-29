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
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue


import androidx.compose.ui.unit.dp

import es.dmoral.toasty.Toasty

import mkn.snordy.interactivelock.R

class TextLockActivity : ComponentActivity() {
    var isSetPassword = false
    var realPassword = ""
    var newPassword = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val intent = intent
        newPassword = ""
        isSetPassword = intent.getBooleanExtra("set", false)
        realPassword = intent.getStringExtra("password").toString()

        setContent {


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(50.dp),
                verticalArrangement = Arrangement.SpaceAround
//                horizontalArrangement = Arrangement.SpaceAround
            ) {
                var textEditValue by remember { mutableStateOf(TextFieldValue("")) };
                TextField(
                    value = textEditValue,
                    onValueChange = {
                        textEditValue =  it.copy(text = it.text.lowercase())
                        newPassword = textEditValue.text
                    },
                    label = { Text(text = "Password") },
                    placeholder = { Text(text = "Enter the password") },


                )
                Button(
                    modifier = Modifier
                        .fillMaxWidth().padding(50.dp)
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
                                    Intent().putExtra("password", "t$newPassword")
                                )
                                finish()
                            }
                        } else if (newPassword == realPassword) {
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            finish()
                        }
                    }){Text(color = Color.Black, text = "DONE")}
            }
        }


    }
}