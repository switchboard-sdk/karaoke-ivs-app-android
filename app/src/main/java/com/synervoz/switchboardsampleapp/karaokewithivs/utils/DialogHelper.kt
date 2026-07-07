package com.synervoz.switchboardsampleapp.karaokewithivs.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.ScrollView
import android.widget.TextView

class DialogHelper {
    companion object {
        // Dialogs need an Activity context for a valid window token; the examples run on the
        // application context, so use the held Activity.
        fun create(context: Context, content: String) {
            var builder = AlertDialog.Builder(ContextHolder.activity)
            builder.setTitle("Missing settings")

            var scrollView = ScrollView(context)
            var textView = TextView(context)
            textView.setPadding(32, 32, 32, 32) // Add padding for aesthetic spacing

            textView.setText(content)
            scrollView.addView(textView)

            builder.setView(scrollView)

            builder.setPositiveButton("Close",
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface, i: Int) {
                        dialogInterface.dismiss()
                    }
                })

            var dialog = builder.create()
            dialog.show()
        }
    }

}