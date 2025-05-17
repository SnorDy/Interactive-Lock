package mkn.snordy.interactivelock.locks

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button


class NoLockActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val isSetPassword = intent.getBooleanExtra("set", false)
        if (isSetPassword){
        setResult(Activity.RESULT_OK, Intent().putExtra("password", "n0"))}
        else setResult(Activity.RESULT_OK)
        finish()
    }
}