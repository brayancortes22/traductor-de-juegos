package com.bscl.gametranslator.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.bscl.gametranslator.R
import com.bscl.gametranslator.data.PreferencesManager
import com.bscl.gametranslator.model.TranslationMode
import kotlin.math.abs

class FloatingBubbleManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val onSelectMode: (TranslationMode) -> Unit,
    private val onToggleRealTime: () -> Boolean,
    private val onToggleAutoDialogue: () -> Boolean,
    private val onAskAssistant: () -> Unit,
    private val onExplainScreen: () -> Unit,
    private val onToggleVoice: () -> Boolean,
    private val onToggleFilter: () -> Boolean,
    private val getRealTimeActive: () -> Boolean,
    private val getAutoDialogueActive: () -> Boolean
) {

    private val themedContext = ContextThemeWrapper(context, R.style.Theme_GameTranslator)
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var bubbleView: View? = null
    private var menuView: View? = null
    private var bubbleParams: WindowManager.LayoutParams? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    fun show() {
        if (bubbleView != null) return

        val inflater = LayoutInflater.from(themedContext)
        val view = inflater.inflate(R.layout.view_floating_bubble, null)
        bubbleView = view

        val (savedX, savedY) = preferencesManager.getBubblePosition()
        val config = preferencesManager.loadConfig()
        view.alpha = config.bubbleOpacity

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = savedY
        }
        bubbleParams = layoutParams

        view.setOnTouchListener { _, event ->
            handleTouchEvent(event)
        }

        windowManager.addView(view, layoutParams)
    }

    private fun handleTouchEvent(event: MotionEvent): Boolean {
        val currentParams = bubbleParams ?: return false
        val currentView = bubbleView ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = currentParams.x
                initialY = currentParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = (event.rawX - initialTouchX).toInt()
                val dy = (event.rawY - initialTouchY).toInt()

                if (abs(dx) > 10 || abs(dy) > 10) {
                    isDragging = true
                    hideMenu()
                }

                if (isDragging) {
                    currentParams.x = initialX + dx
                    currentParams.y = initialY + dy
                    windowManager.updateViewLayout(currentView, currentParams)
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    toggleMenu()
                } else {
                    snapToEdge()
                }
                return true
            }
        }
        return false
    }

    @SuppressLint("InflateParams")
    private fun toggleMenu() {
        if (menuView != null) {
            hideMenu()
            return
        }

        val inflater = LayoutInflater.from(themedContext)
        val view = inflater.inflate(R.layout.view_floating_menu, null)
        menuView = view

        val bParams = bubbleParams ?: return
        val screenWidth = context.resources.displayMetrics.widthPixels
        val isLeft = bParams.x < screenWidth / 2

        val menuParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = if (isLeft) bParams.x + 140 else bParams.x - 420
            y = (bParams.y - 80).coerceAtLeast(40)
        }

        view.findViewById<View>(R.id.btn_menu_close).setOnClickListener { hideMenu() }
        view.findViewById<View>(R.id.btn_mode_full).setOnClickListener {
            hideMenu()
            onSelectMode(TranslationMode.FULL_SCREEN)
        }
        view.findViewById<View>(R.id.btn_mode_crop).setOnClickListener {
            hideMenu()
            onSelectMode(TranslationMode.PARTIAL_CROP)
        }
        view.findViewById<View>(R.id.btn_mode_quests).setOnClickListener {
            hideMenu()
            onSelectMode(TranslationMode.LIFEAFTER_QUESTS)
        }
        view.findViewById<View>(R.id.btn_mode_shop).setOnClickListener {
            hideMenu()
            onSelectMode(TranslationMode.LIFEAFTER_SHOP)
        }
        // Auto-Diálogo Toggle
        val updateDialogueUi = { active: Boolean ->
            val tvLabel = view.findViewById<TextView>(R.id.tv_chat_label)
            val ivIcon = view.findViewById<ImageView>(R.id.iv_chat_indicator)
            if (active) {
                tvLabel.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
                ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.accent_green))
                tvLabel.text = "💬 Auto-Diálogo: ACTIVO"
            } else {
                tvLabel.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.text_primary))
                tvLabel.text = "💬 Auto-Diálogo Inteligente"
            }
        }
        updateDialogueUi(getAutoDialogueActive())

        view.findViewById<View>(R.id.btn_mode_chat).setOnClickListener {
            val isActive = onToggleAutoDialogue()
            updateDialogueUi(isActive)
        }

        view.findViewById<View>(R.id.btn_mode_copilot).setOnClickListener {
            hideMenu()
            onAskAssistant()
        }
        view.findViewById<View>(R.id.btn_explain_screen).setOnClickListener {
            hideMenu()
            onExplainScreen()
        }

        // Real-Time Toggle
        val updateRealTimeUi = { active: Boolean ->
            val tvLabel = view.findViewById<TextView>(R.id.tv_realtime_label)
            val ivIcon = view.findViewById<ImageView>(R.id.iv_realtime_indicator)
            if (active) {
                tvLabel.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
                ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.accent_green))
                tvLabel.text = "⚡ Tiempo Real: ACTIVO"
            } else {
                tvLabel.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary))
                tvLabel.text = context.getString(R.string.mode_realtime_toggle)
            }
        }
        updateRealTimeUi(getRealTimeActive())

        view.findViewById<View>(R.id.btn_mode_realtime).setOnClickListener {
            val isActive = onToggleRealTime()
            updateRealTimeUi(isActive)
        }

        // Voice Toggle
        val config = preferencesManager.loadConfig()
        val updateVoiceUi = { active: Boolean ->
            val tvLabel = view.findViewById<TextView>(R.id.tv_voice_label)
            val ivIcon = view.findViewById<ImageView>(R.id.iv_voice_indicator)
            if (active) {
                tvLabel.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
                ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.accent_green))
            } else {
                tvLabel.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary))
            }
        }
        updateVoiceUi(config.enableVoiceAssistant)

        view.findViewById<View>(R.id.btn_toggle_voice).setOnClickListener {
            val isVoiceActive = onToggleVoice()
            updateVoiceUi(isVoiceActive)
        }

        // Filter Toggle
        val updateFilterUi = { active: Boolean ->
            val tvLabel = view.findViewById<TextView>(R.id.tv_filter_label)
            val ivIcon = view.findViewById<ImageView>(R.id.iv_filter_indicator)
            if (active) {
                tvLabel.setTextColor(ContextCompat.getColor(context, R.color.accent_cyan))
                ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.accent_cyan))
            } else {
                tvLabel.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary))
            }
        }
        updateFilterUi(config.filterIrrelevantElements)

        view.findViewById<View>(R.id.btn_toggle_filter).setOnClickListener {
            val isFilterActive = onToggleFilter()
            updateFilterUi(isFilterActive)
        }

        windowManager.addView(view, menuParams)
    }

    fun hideMenu() {
        menuView?.let {
            if (it.parent != null) {
                windowManager.removeView(it)
            }
            menuView = null
        }
    }

    private fun snapToEdge() {
        val currentParams = bubbleParams ?: return
        val currentView = bubbleView ?: return
        val screenWidth = context.resources.displayMetrics.widthPixels

        currentParams.x = if (currentParams.x + (currentView.width / 2) < screenWidth / 2) {
            0
        } else {
            screenWidth - currentView.width
        }

        windowManager.updateViewLayout(currentView, currentParams)
        preferencesManager.saveBubblePosition(currentParams.x, currentParams.y)
    }

    fun setVisible(visible: Boolean) {
        bubbleView?.visibility = if (visible) View.VISIBLE else View.GONE
        if (!visible) hideMenu()
    }

    fun hide() {
        hideMenu()
        bubbleView?.let {
            windowManager.removeView(it)
            bubbleView = null
            bubbleParams = null
        }
    }
}
