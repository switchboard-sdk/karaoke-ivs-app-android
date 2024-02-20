package com.synervoz.switchboardsampleapp.karaokewithivs.utils

import android.content.Context
import android.content.DialogInterface
import android.text.method.LinkMovementMethod
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class DialogHelper {
    companion object {
        fun create(context:Context, content: String) {
            var builder = AlertDialog.Builder(context)
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