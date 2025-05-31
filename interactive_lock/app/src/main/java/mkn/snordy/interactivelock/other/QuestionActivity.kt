package mkn.snordy.interactivelock.other

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import mkn.snordy.interactivelock.MainActivity
import mkn.snordy.interactivelock.R
import mkn.snordy.interactivelock.customToast.CustomToast

class QuestionActivity : ComponentActivity() {
    private lateinit var questionSharedPreferences: SharedPreferences
    private lateinit var mainSharedPreferences: SharedPreferences
    private lateinit var questionEditor: SharedPreferences.Editor
    private val questionList = mutableListOf<String>(
        "What was the name of your favorite teacher in high school?",
        "What was the name of your favorite childhood book?",
        "What is your favorite movie?",
        "What was your first pet’s name?"
    )
    private var isReset = false
    private val question = questionList.random()
    private lateinit var correctAnswer: String

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        return
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        questionSharedPreferences = getSharedPreferences("ResetQuestion", MODE_PRIVATE)
        questionEditor = questionSharedPreferences.edit()
        mainSharedPreferences = getSharedPreferences("AppLocks", MODE_PRIVATE)
        isReset = intent.getBooleanExtra("reset", false)


        correctAnswer = (questionSharedPreferences.getString(question, "") ?: "").toString()
        setContent {
            if (isReset)
                resetView()
            else setAnswerView()
        }
    }

    @Composable
    fun resetView() {
        var textEditValue by remember { mutableStateOf(TextFieldValue("")) }
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,

            ) {


            Text(
                modifier = Modifier.Companion
                    .padding(top = 30.dp, bottom = 20.dp)
                    .fillMaxWidth(),
                text = question,
                fontSize = 16.sp,

                )
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextField(
                    modifier = Modifier.Companion
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

                    },
                    label = { Text(text = "Answer") },
                    placeholder = { Text(text = "Enter your answer") },
                )
            }
            Button(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(top = 25.dp)
                    .background(Color.Companion.White),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Companion.White),
                border = BorderStroke(2.dp, Color.Companion.Black),
                onClick = {
                    if (textEditValue.text == correctAnswer) {
                        mainSharedPreferences.edit(commit = true) { clear() }
                        CustomToast.Companion.showSuccessToast(
                            baseContext,
                            "Passwords have been reset!"
                        )
                        startActivity(Intent(baseContext, MainActivity::class.java))
                        finish()
                    } else CustomToast.Companion.showErrorToast(
                        baseContext,
                        "Answer isn't correct!"
                    )
                }) {
                Text(
                    color = Color.Companion.Black,
                    text = "DONE"
                )
            }

            Button(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(Color.Companion.White),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Companion.White),
                border = BorderStroke(2.dp, Color.Companion.Black),
                onClick = { finish() }) {
                Text(
                    color = Color.Companion.Black,
                    text = "BACK"
                )
            }
        }

    }

    @Composable
    fun setAnswerView() {
        var currState by remember { mutableIntStateOf(0) }
        var currQuestion by remember { mutableStateOf(questionList[currState]) }
        var textEditValue by remember { mutableStateOf(TextFieldValue("")) }
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(10.dp)
                .padding(top = 80.dp)
                .border(2.dp, Color.Black, shape = RoundedCornerShape(5.dp))
                .height(60.dp),

            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .offset(y = 18.dp),
                text = "Answers to reset the lock",
                fontSize = 22.sp,
                textAlign = TextAlign.Center

            )
        }
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,

            ) {
            Text(
                modifier = Modifier.Companion
                    .padding(top = 30.dp, bottom = 20.dp)
                    .fillMaxWidth(),
                text = currQuestion,
                fontSize = 16.sp,
            )
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextField(
                    modifier = Modifier.Companion
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

                    },
                    label = { Text(text = "Answer") },
                    placeholder = { Text(text = "Enter your answer") },


                    )
            }

            Button(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(top = 25.dp)
                    .background(Color.Companion.White),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Companion.White),
                border = BorderStroke(2.dp, Color.Companion.Black),
                onClick = {

                    if (currState == questionList.size - 1) {
                        setResult(RESULT_OK)
                        questionEditor.putBoolean("isFirstLaunch", false).commit()
                        finish()
                    } else {
                        if (textEditValue.text.isEmpty()) {
                            CustomToast.Companion.showErrorToast(
                                baseContext,
                                "The password can't be empty!"
                            )
                        } else {

                            questionEditor.putString(
                                questionList[currState],
                                textEditValue.text
                            ).commit()
                            currState++
                            currQuestion = questionList[currState]
                            textEditValue = if (questionSharedPreferences.contains(currQuestion)) {
                                textEditValue.copy(
                                    text = questionSharedPreferences.getString(
                                        currQuestion,
                                        ""
                                    ).toString()
                                )
                            } else
                                textEditValue.copy(text = "")
                        }
                    }

                }) {
                Text(
                    color = Color.Companion.Black,
                    text = if (currState < questionList.size - 1) "NEXT" else "DONE"
                )
            }
        }

        var expanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .size(50.dp)
                .border(1.dp, color = Color.Companion.Black)
        ) {
            Box(modifier = Modifier.Companion.offset(x = 5.dp)) {
                IconButton(modifier = Modifier.size(56.dp), onClick = { expanded = true }) {
                    Icon(
                        painter = painterResource(R.drawable.menu_icon),
                        contentDescription = "menu_icon", modifier = Modifier.Companion.size(36.dp)
                    )
                }

                DropdownMenu(
                    modifier = Modifier.Companion
                        .background(color = Color.Companion.White)
                        .border(color = Color.Companion.Black, width = 1.dp),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    for (el in questionList) {
                        DropdownMenuItem(
                            onClick = {
                                currQuestion = el
                                currState = questionList.indexOf(el)
                                textEditValue =
                                    if (questionSharedPreferences.contains(currQuestion)) {
                                        textEditValue.copy(
                                            text = questionSharedPreferences.getString(
                                                currQuestion,
                                                ""
                                            ).toString()
                                        )
                                    } else
                                        textEditValue.copy(text = "")
                            },
                            text = { Text(el) }
                        )
                        if (questionList.last() != el)
                            Divider(color = Color.Companion.Black)
                    }
                }
            }
        }
    }
}