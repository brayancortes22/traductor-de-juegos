package com.bscl.gametranslator.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.bscl.gametranslator.R
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ViewConstructor")
class SnipOverlayView(
    context: Context,
    private val onAreaSelected: (Rect) -> Unit,
    private val onCancelled: () -> Unit
) : View(context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val borderPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.crop_selection_border)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val fillPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.crop_selection_fill)
        style = Paint.Style.FILL
    }

    private var startX = 0f
    private var startY = 0f
    private var currentX = 0f
    private var currentY = 0f
    private var isDrawing = false

    init {
        setBackgroundColor(0x33000000)
    }

    fun attach() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        windowManager.addView(this, params)
    }

    fun detach() {
        if (parent != null) {
            windowManager.removeView(this)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isDrawing) {
            val left = min(startX, currentX)
            val top = min(startY, currentY)
            val right = max(startX, currentX)
            val bottom = max(startY, currentY)

            canvas.drawRect(left, top, right, bottom, fillPaint)
            canvas.drawRect(left, top, right, bottom, borderPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                currentX = event.x
                currentY = event.y
                isDrawing = true
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                currentX = event.x
                currentY = event.y
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP -> {
                isDrawing = false
                val left = min(startX, currentX).toInt()
                val top = min(startY, currentY).toInt()
                val right = max(startX, currentX).toInt()
                val bottom = max(startY, currentY).toInt()

                detach()
                if (right - left > 20 && bottom - top > 20) {
                    onAreaSelected(Rect(left, top, right, bottom))
                } else {
                    onCancelled()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
