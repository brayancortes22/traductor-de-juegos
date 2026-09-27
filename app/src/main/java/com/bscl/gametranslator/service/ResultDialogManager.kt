package com.bscl.gametranslator.service

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import com.bscl.gametranslator.R
import com.bscl.gametranslator.model.TranslationResult

class ResultDialogManager(private val context: Context) {

    private val themedContext = ContextThemeWrapper(context, R.style.Theme_GameTranslator)
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var dialogView: View? = null

    @SuppressLint("InflateParams")
    fun showLoading() {
        dismiss()
        val inflater = LayoutInflater.from(themedContext)
        val view = inflater.inflate(R.layout.view_result_card, null)
        dialogView = view

        val tvTitle = view.findViewById<TextView>(R.id.tv_card_title)
        val progressBar = view.findViewById<ProgressBar>(R.id.card_progress)
        val tvTranslated = view.findViewById<TextView>(R.id.tv_card_translated_text)
        val btnClose = view.findViewById<ImageButton>(R.id.btn_card_close)
        val btnCopy = view.findViewById<ImageButton>(R.id.btn_card_copy)

        tvTitle.setText(R.string.translating_wait)
        progressBar.visibility = View.VISIBLE
        tvTranslated.text = ""
        btnCopy.visibility = View.GONE

        btnClose.setOnClickListener { dismiss() }

        attachToWindow(view)
    }

    fun showResult(result: TranslationResult, autoCopy: Boolean) {
        val view = dialogView ?: return

        val tvTitle = view.findViewById<TextView>(R.id.tv_card_title)
        val progressBar = view.findViewById<ProgressBar>(R.id.card_progress)
        val tvTranslated = view.findViewById<TextView>(R.id.tv_card_translated_text)
        val tvOriginal = view.findViewById<TextView>(R.id.tv_card_original_text)
        val btnCopy = view.findViewById<ImageButton>(R.id.btn_card_copy)

        progressBar.visibility = View.GONE
        tvTitle.setText(R.string.dialog_result_title)

        if (result.translatedFullText.isNotEmpty()) {
            tvTranslated.text = result.translatedFullText
            tvOriginal.text = result.originalFullText
            tvOriginal.visibility = View.VISIBLE
            btnCopy.visibility = View.VISIBLE

            btnCopy.setOnClickListener {
                copyToClipboard(result.translatedFullText)
            }

            if (autoCopy) {
                copyToClipboard(result.translatedFullText)
            }
        } else {
            tvTranslated.setText(R.string.no_text_detected)
            tvOriginal.visibility = View.GONE
            btnCopy.visibility = View.GONE
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Traducción", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    private fun attachToWindow(view: View) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 100
        }
        windowManager.addView(view, params)
    }

    fun dismiss() {
        dialogView?.let {
            if (it.parent != null) {
                windowManager.removeView(it)
            }
            dialogView = null
        }
    }
}
