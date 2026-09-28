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
import com.bscl.gametranslator.model.DetectedTextBlock
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ViewConstructor")
class InPlaceTranslationOverlayView(
    context: Context,
    initialBlocks: List<DetectedTextBlock>,
    private val isClickThrough: Boolean = false,
    private val onDismiss: () -> Unit
) : View(context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var renderedBlocks: List<RenderPill> = emptyList()

    private val bgPaint = Paint().apply {
        color = 0xEE090D16.toInt() // Dark solid cinematic slate to conceal English text
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        color = 0x8038BDF8.toInt() // Soft cyan/sky border that does not visually clash
        style = Paint.Style.STROKE
        strokeWidth = 2.0f
        isAntiAlias = true
    }

    private val topBannerBgPaint = Paint().apply {
        color = 0xF01E293B.toInt()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val topBannerBorderPaint = Paint().apply {
        color = 0x9000E5FF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val topBannerTextPaint = TextPaint().apply {
        color = 0xFF38BDF8.toInt()
        textSize = 26f
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
        setBackgroundColor(0x00000000) // 100% transparent root background
        processAndMergeBlocks(initialBlocks)
    }

    fun updateBlocks(newBlocks: List<DetectedTextBlock>) {
        processAndMergeBlocks(newBlocks)
        postInvalidate()
    }

    fun attach() {
        val flags = if (isClickThrough) {
            // Modo diálogo pasante: NO intercepta toques, pasan 100% al juego LifeAfter
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        } else {
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
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

    /**
     * Algoritmo anti-colisión: fusiona bloques de texto verticalmente adyacentes o superpuestos
     * para crear subtítulos limpios y unificados, evitando que los recuadros se crucen o pisen.
     */
    private fun processAndMergeBlocks(blocks: List<DetectedTextBlock>) {
        val validBlocks = blocks.filter {
            it.boundingBox != null && it.translatedText.isNotBlank()
        }.sortedBy { it.boundingBox?.top ?: 0 }

        if (validBlocks.isEmpty()) {
            renderedBlocks = emptyList()
            return
        }

        val merged = mutableListOf<RenderPill>()

        for (block in validBlocks) {
            val r = block.boundingBox ?: continue
            val text = block.translatedText.trim()
            if (text.isEmpty()) continue

            val initialRect = RectF(
                max(0f, r.left.toFloat() - 10f),
                max(0f, r.top.toFloat() - 6f),
                r.right.toFloat() + 10f,
                r.bottom.toFloat() + 6f
            )

            // Buscar si colisiona o está muy cerca del bloque anterior
            val last = merged.lastOrNull()
            if (last != null && shouldMerge(last.rect, initialRect)) {
                // Fusión limpia de cajas y textos
                val unionRect = RectF(
                    min(last.rect.left, initialRect.left),
                    min(last.rect.top, initialRect.top),
                    max(last.rect.right, initialRect.right),
                    max(last.rect.bottom, initialRect.bottom)
                )
                val combinedText = "${last.text} $text"
                merged[merged.size - 1] = RenderPill(unionRect, combinedText)
            } else {
                merged.add(RenderPill(initialRect, text))
            }
        }

        renderedBlocks = merged
    }

    private fun shouldMerge(r1: RectF, r2: RectF): Boolean {
        // Si hay intersección directa de rectángulos
        if (RectF.intersects(r1, r2)) return true

        // O si la distancia vertical es mínima (< 16px) y se solapan horizontalmente
        val verticalDist = max(0f, r2.top - r1.bottom)
        val horizontalOverlap = min(r1.right, r2.right) - max(r1.left, r2.left)
        return verticalDist < 16f && horizontalOverlap > 20f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (renderedBlocks.isEmpty()) {
            return
        }

        val screenW = width.toFloat()

        // Banner informativo en la parte superior
        val bannerW = if (isClickThrough) 520f else 430f
        val bannerH = 50f
        val bannerLeft = (screenW - bannerW) / 2f
        val bannerTop = 22f
        val bannerRect = RectF(bannerLeft, bannerTop, bannerLeft + bannerW, bannerTop + bannerH)

        canvas.drawRoundRect(bannerRect, 25f, 25f, topBannerBgPaint)
        canvas.drawRoundRect(bannerRect, 25f, 25f, topBannerBorderPaint)

        val bannerMessage = if (isClickThrough) {
            "💬 Modo Diálogo Activo • Toca el juego para avanzar"
        } else {
            "✕ Toca la pantalla para cerrar"
        }
        canvas.drawText(bannerMessage, screenW / 2f, bannerTop + 34f, topBannerTextPaint)

        // Dibujar cada bloque unificado sin colisiones
        val paddingX = 12f
        val paddingY = 6f

        for (pill in renderedBlocks) {
            val rectF = pill.rect

            // Dibujar fondo oscuro estético que oculta el texto original en inglés
            canvas.drawRoundRect(rectF, 12f, 12f, bgPaint)
            canvas.drawRoundRect(rectF, 12f, 12f, borderPaint)

            val availableW = max(10, (rectF.width() - paddingX * 2).toInt())
            val availableH = max(10, (rectF.height() - paddingY * 2).toInt())

            var fontSize = 28f
            textPaint.textSize = fontSize

            var layout = createStaticLayout(pill.text, availableW)
            while (layout.height > availableH && fontSize > 15f) {
                fontSize -= 2f
                textPaint.textSize = fontSize
                layout = createStaticLayout(pill.text, availableW)
            }

            canvas.save()
            // Centrar texto verticalmente dentro de la pastilla
            val textYOffset = (rectF.height() - layout.height) / 2f
            canvas.translate(rectF.left + paddingX, rectF.top + max(0f, textYOffset))
            layout.draw(canvas)
            canvas.restore()
        }
    }

    private fun createStaticLayout(text: String, width: Int): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.05f)
            .setIncludePad(false)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Si es pasante, los toques nunca llegan aquí debido a FLAG_NOT_TOUCHABLE.
        // Si no es pasante, tocar cierra la superposición.
        if (!isClickThrough && event.action == MotionEvent.ACTION_UP) {
            detach()
            onDismiss()
            return true
        }
        return false
    }

    private data class RenderPill(
        val rect: RectF,
        val text: String
    )
}
