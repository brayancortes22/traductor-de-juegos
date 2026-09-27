package com.bscl.gametranslator.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.bscl.gametranslator.R
import com.bscl.gametranslator.model.DetectedTextBlock
import kotlin.math.max

@SuppressLint("ViewConstructor")
class InPlaceTranslationOverlayView(
    context: Context,
    private val blocks: List<DetectedTextBlock>,
    private val onDismiss: () -> Unit
) : View(context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val bgPaint = Paint().apply {
        color = 0xF00A0E17.toInt() // Dark solid overlay to hide English text
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        color = 0xFF00E5FF.toInt() // Cyan accent border
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        isAntiAlias = true
    }

    private val closeBarBgPaint = Paint().apply {
        color = 0xEE1E293B.toInt()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val closeBarTextPaint = TextPaint().apply {
        color = 0xFF38BDF8.toInt()
        textSize = 28f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val textPaint = TextPaint().apply {
        color = 0xFFFFFFFF.toInt()
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    init {
        // Transparent overall background so game remains visible behind overlay pills
        setBackgroundColor(0x1A000000)
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

        // Draw top banner instructing the user
        val screenW = width.toFloat()
        val bannerW = 420f
        val bannerH = 54f
        val bannerLeft = (screenW - bannerW) / 2f
        val bannerTop = 24f
        val bannerRect = RectF(bannerLeft, bannerTop, bannerLeft + bannerW, bannerTop + bannerH)
        canvas.drawRoundRect(bannerRect, 27f, 27f, closeBarBgPaint)
        canvas.drawRoundRect(bannerRect, 27f, 27f, borderPaint)
        canvas.drawText("✕ Toca la pantalla para ocultar", screenW / 2f, bannerTop + 36f, closeBarTextPaint)

        // Draw each translated block exactly over its English coordinates
        val paddingX = 8f
        val paddingY = 4f

        for (block in blocks) {
            val r = block.boundingBox ?: continue
            val translated = block.translatedText.trim()
            if (translated.isEmpty()) continue

            val boxLeft = max(0f, r.left.toFloat() - paddingX)
            val boxTop = max(0f, r.top.toFloat() - paddingY)
            val boxRight = r.right.toFloat() + paddingX
            val boxBottom = r.bottom.toFloat() + paddingY

            val rectF = RectF(boxLeft, boxTop, boxRight, boxBottom)

            // Draw background concealing English text
            canvas.drawRoundRect(rectF, 10f, 10f, bgPaint)
            canvas.drawRoundRect(rectF, 10f, 10f, borderPaint)

            // Calculate optimal font size to fit inside the bounding box
            val availableW = max(10, (rectF.width() - paddingX * 2).toInt())
            val availableH = max(10, (rectF.height() - paddingY * 2).toInt())

            var fontSize = 28f
            textPaint.textSize = fontSize

            var layout = createStaticLayout(translated, availableW)
            while (layout.height > availableH && fontSize > 14f) {
                fontSize -= 2f
                textPaint.textSize = fontSize
                layout = createStaticLayout(translated, availableW)
            }

            canvas.save()
            // Center text vertically inside bounding box
            val textYOffset = (rectF.height() - layout.height) / 2f
            canvas.translate(rectF.left + paddingX, rectF.top + max(0f, textYOffset))
            layout.draw(canvas)
            canvas.restore()
        }
    }

    private fun createStaticLayout(text: String, width: Int): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.0f)
            .setIncludePad(false)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            detach()
            onDismiss()
            return true
        }
        return true
    }
}
