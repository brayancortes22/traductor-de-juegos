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
import com.bscl.gametranslator.assistant.AdviceType
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
        val tvLabelObjective = view.findViewById<TextView>(R.id.tv_label_objective)
        val tvAdviceObjective = view.findViewById<TextView>(R.id.tv_advice_objective)

        val tvLabelWhere = view.findViewById<TextView>(R.id.tv_label_where)
        val tvAdviceWhere = view.findViewById<TextView>(R.id.tv_advice_where)

        val tvLabelSearchBring = view.findViewById<TextView>(R.id.tv_label_search_bring)
        val tvAdviceSearchBring = view.findViewById<TextView>(R.id.tv_advice_search_bring)

        val tvLabelSteps = view.findViewById<TextView>(R.id.tv_label_steps)
        val tvAdviceSteps = view.findViewById<TextView>(R.id.tv_advice_steps)

        val tvLabelProTip = view.findViewById<TextView>(R.id.tv_label_protip)
        val tvAdviceProTip = view.findViewById<TextView>(R.id.tv_advice_protip)

        val btnClose = view.findViewById<ImageView>(R.id.btn_assistant_close)
        val btnDismiss = view.findViewById<Button>(R.id.btn_advice_dismiss)
        val btnSpeak = view.findViewById<Button>(R.id.btn_advice_speak)

        // Configurar títulos dinámicos según el tipo de contexto
        when (advice.type) {
            AdviceType.SKILL_TALENT -> {
                tvLabelObjective.text = "💡 ¿Qué es y para qué sirve esta habilidad?"
                tvLabelWhere.text = "📍 Dónde se ubica o desbloquea:"
                tvLabelSearchBring.text = "💎 Costo y requisitos (Puntos / Dólares):"
                tvLabelSteps.text = "🛠️ Cómo mejorarla paso a paso:"
                tvLabelProTip.text = "⚡ ¿Vale la pena subirla? (Prioridad Pro):"
            }
            AdviceType.ITEM_EQUIPMENT -> {
                tvLabelObjective.text = "📦 ¿Qué es y para qué sirve este objeto?"
                tvLabelWhere.text = "📍 Dónde se fabrica o equipa:"
                tvLabelSearchBring.text = "🎒 Materiales o requisitos requeridos:"
                tvLabelSteps.text = "🛠️ Paso a paso para craftearlo o usarlo:"
                tvLabelProTip.text = "⚡ Consejo de durabilidad y uso táctico:"
            }
            AdviceType.MISSION_TACTICAL, AdviceType.GENERAL_INTERFACE -> {
                tvLabelObjective.text = "🎯 ¿Qué debo hacer?"
                tvLabelWhere.text = "📍 ¿A dónde ir?"
                tvLabelSearchBring.text = "🎒 ¿Qué buscar en la zona y qué llevar?"
                tvLabelSteps.text = "🛠️ Paso a paso (1, 2, 3):"
                tvLabelProTip.text = "⚡ Consejo Táctico / Truco de Supervivencia:"
            }
        }

        tvTitle.text = advice.title
        tvAdviceObjective.text = advice.objective
        tvAdviceWhere.text = advice.whereToGo
        tvAdviceSearchBring.text = advice.whatToSearchAndBring
        tvAdviceSteps.text = advice.stepByStep
        tvAdviceProTip.text = advice.proTip

        btnClose.setOnClickListener { dismiss() }
        btnDismiss.setOnClickListener { dismiss() }
        btnSpeak.setOnClickListener {
            onSpeakRequested(advice.spokenSummary)
        }

        attachToWindow(view)
    }

    private fun attachToWindow(view: View) {
        val displayMetrics = context.resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.84).toInt().coerceAtMost(920)
        val maxHeight = (displayMetrics.heightPixels * 0.90).toInt()

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
            height = WindowManager.LayoutParams.WRAP_CONTENT
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
