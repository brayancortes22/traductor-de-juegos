package com.bscl.gametranslator.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.bscl.gametranslator.R
import com.bscl.gametranslator.data.PreferencesManager
import kotlin.math.abs

class FloatingBubbleManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val onClick: () -> Unit,
    private val onLongClick: () -> Unit
) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var bubbleView: View? = null
    private var params: WindowManager.LayoutParams? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    fun show() {
        if (bubbleView != null) return

        val inflater = LayoutInflater.from(context)
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
        params = layoutParams

        view.setOnTouchListener { _, event ->
            handleTouchEvent(event)
        }

        windowManager.addView(view, layoutParams)
    }

    private fun handleTouchEvent(event: MotionEvent): Boolean {
        val currentParams = params ?: return false
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
                    onClick()
                } else {
                    snapToEdge()
                }
                return true
            }
        }
        return false
    }

    private fun snapToEdge() {
        val currentParams = params ?: return
        val currentView = bubbleView ?: return
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

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
    }

    fun hide() {
        bubbleView?.let {
            windowManager.removeView(it)
            bubbleView = null
            params = null
        }
    }
}
