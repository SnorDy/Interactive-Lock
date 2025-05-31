package mkn.snordy.interactivelock.locks
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import mkn.snordy.interactivelock.customToast.CustomToast

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
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                var textEditValue by remember { mutableStateOf(TextFieldValue("")) };
                TextField(modifier = Modifier.Companion
                    .fillMaxWidth()
                    .background(Color.Companion.White)
                    .border(width = 1.dp, color = Color.Companion.Black),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.LightGray,
                        unfocusedContainerColor = Color.LightGray,
                        disabledContainerColor = Color.Gray,
                        errorContainerColor = Color.Red.copy(alpha = 0.1f)
                    ),
                    value = textEditValue,
                    onValueChange = {
                        textEditValue = it.copy(text = it.text.lowercase())
                        newPassword = textEditValue.text
                    },
                    label = { Text(text = "Password") },
                    placeholder = { Text(text = "Enter the password") },


                    )
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .background(Color.White),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, Color.Black),
                    onClick = {
                        if (isSetPassword) {
                            if (newPassword.isEmpty()) {

                                CustomToast.showErrorToast(
                                    baseContext,
                                    "The password can't be empty!"
                                )
                            } else {
                                setResult(
                                    RESULT_OK,
                                    Intent().putExtra("password", "t$newPassword")
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
            }
        }
    }
}