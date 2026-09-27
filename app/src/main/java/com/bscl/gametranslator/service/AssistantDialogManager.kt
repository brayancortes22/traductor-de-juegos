package com.bscl.gametranslator.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import com.bscl.gametranslator.R
import com.bscl.gametranslator.assistant.GameAdvice

class AssistantDialogManager(
    private val context: Context,
    private val onSpeakRequested: (String) -> Unit
) {

    private val themedContext = ContextThemeWrapper(context, R.style.Theme_GameTranslator)
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var dialogView: View? = null

    @SuppressLint("InflateParams")
    fun showAdvice(advice: GameAdvice) {
        dismiss()

        val inflater = LayoutInflater.from(themedContext)
        val view = inflater.inflate(R.layout.view_assistant_dialog, null)
        dialogView = view

        val tvTitle = view.findViewById<TextView>(R.id.tv_advice_title)
        val tvObjective = view.findViewById<TextView>(R.id.tv_advice_objective)
        val tvWhere = view.findViewById<TextView>(R.id.tv_advice_where)
        val tvSteps = view.findViewById<TextView>(R.id.tv_advice_steps)
        val tvProTip = view.findViewById<TextView>(R.id.tv_advice_protip)
        val btnClose = view.findViewById<ImageView>(R.id.btn_assistant_close)
        val btnDismiss = view.findViewById<Button>(R.id.btn_advice_dismiss)
        val btnSpeak = view.findViewById<Button>(R.id.btn_advice_speak)

        tvTitle.text = advice.title
        tvObjective.text = advice.objective
        tvWhere.text = advice.whereToFind
        tvSteps.text = advice.stepByStep
        tvProTip.text = advice.proTip

        btnClose.setOnClickListener { dismiss() }
        btnDismiss.setOnClickListener { dismiss() }
        btnSpeak.setOnClickListener {
            onSpeakRequested(advice.spokenSummary)
        }

        attachToWindow(view)
    }

    private fun attachToWindow(view: View) {
        val displayMetrics = context.resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.82).toInt().coerceAtMost(900)

        val params = WindowManager.LayoutParams(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = 0
            y = 0
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
