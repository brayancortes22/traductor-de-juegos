package com.bscl.gametranslator.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bscl.gametranslator.R
import com.bscl.gametranslator.data.PreferencesManager
import com.bscl.gametranslator.ml.OcrEngine
import com.bscl.gametranslator.ml.TranslatorEngine
import com.bscl.gametranslator.model.TranslationResult
import com.bscl.gametranslator.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScreenCaptureService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var ocrEngine: OcrEngine
    private lateinit var translatorEngine: TranslatorEngine
    private lateinit var bubbleManager: FloatingBubbleManager
    private lateinit var resultDialogManager: ResultDialogManager

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private var screenWidth = 1080
    private var screenHeight = 1920
    private var screenDensity = 320

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
        ocrEngine = OcrEngine()
        translatorEngine = TranslatorEngine()
        resultDialogManager = ResultDialogManager(this)

        initDisplayMetrics()
        setupNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        bubbleManager = FloatingBubbleManager(
            context = this,
            preferencesManager = preferencesManager,
            onClick = { captureAndTranslateFull() },
            onLongClick = { startSnippingMode() }
        )
        bubbleManager.show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        val resultData = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)

        if (resultCode != 0 && resultData != null && mediaProjection == null) {
            val projectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)
            setupVirtualDisplay()
        }

        return START_STICKY
    }

    private fun initDisplayMetrics() {
        val metrics = resources.displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        screenDensity = metrics.densityDpi
    }

    @SuppressLint("WrongConstant")
    private fun setupVirtualDisplay() {
        val projection = mediaProjection ?: return
        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            2
        )

        virtualDisplay = projection.createVirtualDisplay(
            "GameTranslatorCapture",
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )
    }

    private fun captureCurrentBitmap(): Bitmap? {
        val reader = imageReader ?: return null
        val image = reader.acquireLatestImage() ?: return null
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * screenWidth

        val bitmap = Bitmap.createBitmap(
            screenWidth + rowPadding / pixelStride,
            screenHeight,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()

        return Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
    }

    private fun captureAndTranslateFull() {
        resultDialogManager.showLoading()
        serviceScope.launch(Dispatchers.Default) {
            val bitmap = captureCurrentBitmap()
            if (bitmap == null) {
                withContext(Dispatchers.Main) {
                    resultDialogManager.showResult(
                        TranslationResult("", "", isSuccess = false, errorMessage = "Error capturando pantalla"),
                        false
                    )
                }
                return@launch
            }
            processBitmapAndTranslate(bitmap)
        }
    }

    private fun startSnippingMode() {
        bubbleManager.setVisible(false)
        val snipView = SnipOverlayView(
            context = this,
            onAreaSelected = { rect ->
                bubbleManager.setVisible(true)
                captureAndTranslateCrop(rect)
            },
            onCancelled = {
                bubbleManager.setVisible(true)
            }
        )
        snipView.attach()
    }

    private fun captureAndTranslateCrop(rect: Rect) {
        resultDialogManager.showLoading()
        serviceScope.launch(Dispatchers.Default) {
            val fullBitmap = captureCurrentBitmap() ?: return@launch
            val safeWidth = (rect.width()).coerceAtMost(fullBitmap.width - rect.left)
            val safeHeight = (rect.height()).coerceAtMost(fullBitmap.height - rect.top)

            if (safeWidth <= 0 || safeHeight <= 0) return@launch
            val cropped = Bitmap.createBitmap(fullBitmap, rect.left, rect.top, safeWidth, safeHeight)
            processBitmapAndTranslate(cropped)
        }
    }

    private suspend fun processBitmapAndTranslate(bitmap: Bitmap) {
        val config = preferencesManager.loadConfig()
        val blocks = ocrEngine.recognizeText(bitmap)
        val fullText = blocks.joinToString(" ") { it.originalText }

        if (fullText.isEmpty()) {
            withContext(Dispatchers.Main) {
                resultDialogManager.showResult(
                    TranslationResult("", "", blocks = emptyList()),
                    config.autoCopyToClipboard
                )
            }
            return
        }

        try {
            val translated = translatorEngine.translateText(
                fullText,
                config.sourceLanguage,
                config.targetLanguage
            )
            withContext(Dispatchers.Main) {
                resultDialogManager.showResult(
                    TranslationResult(fullText, translated, blocks),
                    config.autoCopyToClipboard
                )
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                resultDialogManager.showResult(
                    TranslationResult(fullText, "", isSuccess = false, errorMessage = e.message),
                    false
                )
            }
        }
    }

    private fun setupNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Traductor de Juegos",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Servicio de traducción en pantalla"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_desc))
            .setSmallIcon(R.drawable.ic_translate)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        bubbleManager.hide()
        resultDialogManager.dismiss()
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        ocrEngine.close()
        translatorEngine.close()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "game_translator_channel"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
    }
}
