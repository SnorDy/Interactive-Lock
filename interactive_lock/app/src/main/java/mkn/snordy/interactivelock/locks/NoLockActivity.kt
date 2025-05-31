package mkn.snordy.interactivelock.locks
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity



class NoLockActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val isSetPassword = intent.getBooleanExtra("set", false)
        if (isSetPassword){
        setResult(RESULT_OK, Intent().putExtra("password", "n0"))}
        else setResult(RESULT_OK)
        finish()
    }
}