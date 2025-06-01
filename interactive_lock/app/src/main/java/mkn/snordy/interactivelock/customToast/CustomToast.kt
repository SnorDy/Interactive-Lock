package mkn.snordy.interactivelock.customToast

import android.content.Context
import es.dmoral.toasty.Toasty
import mkn.snordy.interactivelock.R

class CustomToast {
    companion object {
        fun showSuccessToast(
            context: Context,
            text: String,
        ) {
            Toasty.custom(
                context,
                text,
                R.drawable.success_icon,
                es.dmoral.toasty.R.color.successColor,
                2000,
                true,
                true,
            ).show()
        }

        fun showErrorToast(
            context: Context,
            text: String,
        ) {
            Toasty.custom(
                context,
                text,
                R.drawable.bongo_b,
                es.dmoral.toasty.R.color.errorColor,
                2000,
                true,
                true,
            ).show()
        }

        fun showInfoToast(
            context: Context,
            text: String,
        ) {
            Toasty.custom(
                context,
                text,
                R.drawable.bongo_b,
                R.color.teal_700,
                1000,
                true,
                true,
            ).show()
        }
    }
}
