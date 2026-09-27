package com.bscl.gametranslator.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.bscl.gametranslator.R
import com.bscl.gametranslator.assistant.AiGameAssistantEngine
import com.bscl.gametranslator.data.PreferencesManager
import com.bscl.gametranslator.filter.TextFilterEngine
import com.bscl.gametranslator.glossary.GameGlossary
import com.bscl.gametranslator.ml.OcrEngine
import com.bscl.gametranslator.ml.TranslatorEngine
import com.bscl.gametranslator.model.TranslationMode
import com.bscl.gametranslator.model.TranslationResult
import com.bscl.gametranslator.ui.MainActivity
import com.bscl.gametranslator.util.CrashLogger
import com.bscl.gametranslator.util.ImageHashUtil
import com.bscl.gametranslator.voice.VoiceNarratorEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScreenCaptureService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var ocrEngine: OcrEngine
    private lateinit var translatorEngine: TranslatorEngine
    private lateinit var bubbleManager: FloatingBubbleManager
    private lateinit var resultDialogManager: ResultDialogManager
    private lateinit var assistantDialogManager: AssistantDialogManager
    private lateinit var voiceNarrator: VoiceNarratorEngine
    private lateinit var textFilter: TextFilterEngine
    private lateinit var gameGlossary: GameGlossary
    private lateinit var assistantEngine: AiGameAssistantEngine

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private var screenWidth = 1340
    private var screenHeight = 800
    private var screenDensity = 320

    private var currentMode = TranslationMode.FULL_SCREEN
    private var isRealTimeActive = false
    private var realTimeJob: Job? = null
    private var lastFrameHash = 0L

    override fun onCreate() {
        super.onCreate()
        CrashLogger.init(applicationContext)
        preferencesManager = PreferencesManager(this)
        ocrEngine = OcrEngine()
        translatorEngine = TranslatorEngine()
        resultDialogManager = ResultDialogManager(this)
        voiceNarrator = VoiceNarratorEngine(this)
        textFilter = TextFilterEngine()
        gameGlossary = GameGlossary()
        assistantEngine = AiGameAssistantEngine()

        assistantDialogManager = AssistantDialogManager(this) { spokenSummary ->
            voiceNarrator.speak(spokenSummary, isPriority = true)
        }

        initDisplayMetrics()
        setupNotificationChannel()

        bubbleManager = FloatingBubbleManager(
            context = this,
            preferencesManager = preferencesManager,
            onSelectMode = { mode -> executeMode(mode) },
            onToggleRealTime = { toggleRealTimeMode() },
            onAskAssistant = { triggerAssistantAnalysis() },
            onToggleVoice = { toggleVoiceMode() },
            onToggleFilter = { toggleFilterMode() }
        )
        bubbleManager.show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        val resultData = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)

        if (resultCode != 0 && resultData != null && mediaProjection == null) {
            // Requisito estricto en Android 14 (API 34): Iniciar Foreground Service antes de getMediaProjection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    buildNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                )
            } else {
                startForeground(NOTIFICATION_ID, buildNotification())
            }

            // Obtener MediaProjection con el Foreground Service ya activo
            val projectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val projection = projectionManager.getMediaProjection(resultCode, resultData)
            mediaProjection = projection

            // Registrar callback obligatorio antes de createVirtualDisplay
            projection.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    virtualDisplay?.release()
                    virtualDisplay = null
                    mediaProjection = null
                }
            }, Handler(Looper.getMainLooper()))

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
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)
        virtualDisplay = projection.createVirtualDisplay(
            "GameTranslatorCapture",
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            Handler(Looper.getMainLooper())
        )
    }

    private fun executeMode(mode: TranslationMode) {
        currentMode = mode
        when (mode) {
            TranslationMode.FULL_SCREEN -> captureAndTranslate(null)
            TranslationMode.PARTIAL_CROP -> startSnippingMode()
            TranslationMode.LIFEAFTER_QUESTS,
            TranslationMode.LIFEAFTER_SHOP,
            TranslationMode.LIFEAFTER_CHAT -> {
                val rect = mode.getBoundingRect(screenWidth, screenHeight)
                captureAndTranslate(rect)
            }
        }
    }

    private fun toggleRealTimeMode(): Boolean {
        isRealTimeActive = !isRealTimeActive
        if (isRealTimeActive) {
            Toast.makeText(this, "${getString(R.string.realtime_active_toast)} ${currentMode.displayName}", Toast.LENGTH_SHORT).show()
            startRealTimeAutoScan()
        } else {
            Toast.makeText(this, R.string.realtime_stopped_toast, Toast.LENGTH_SHORT).show()
            realTimeJob?.cancel()
        }
        return isRealTimeActive
    }

    private fun toggleVoiceMode(): Boolean {
        val config = preferencesManager.loadConfig()
        val newState = !config.enableVoiceAssistant
        preferencesManager.saveConfig(config.copy(enableVoiceAssistant = newState))
        if (newState) {
            Toast.makeText(this, R.string.voice_enabled_toast, Toast.LENGTH_SHORT).show()
            voiceNarrator.speak("Copiloto de voz activado. Te asistiré durante la partida.", isPriority = true)
        } else {
            Toast.makeText(this, R.string.voice_disabled_toast, Toast.LENGTH_SHORT).show()
            voiceNarrator.stop()
        }
        return newState
    }

    private fun toggleFilterMode(): Boolean {
        val config = preferencesManager.loadConfig()
        val newState = !config.filterIrrelevantElements
        preferencesManager.saveConfig(config.copy(filterIrrelevantElements = newState))
        val msg = if (newState) R.string.filter_enabled_toast else R.string.filter_disabled_toast
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        return newState
    }

    private fun triggerAssistantAnalysis() {
        serviceScope.launch(Dispatchers.Default) {
            val rect = currentMode.getBoundingRect(screenWidth, screenHeight)
            val bitmap = captureBitmapRegion(rect) ?: return@launch
            val rawBlocks = ocrEngine.recognizeText(bitmap)
            bitmap.recycle()

            val cleanText = textFilter.extractCleanFullText(rawBlocks)
            if (cleanText.isBlank()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ScreenCaptureService, "No se detectó texto o misiones en pantalla", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val config = preferencesManager.loadConfig()
            val advice = assistantEngine.analyzeGameContext(cleanText, config.geminiApiKey)

            withContext(Dispatchers.Main) {
                assistantDialogManager.showAdvice(advice)
                if (config.enableVoiceAssistant) {
                    voiceNarrator.speak(advice.spokenSummary, isPriority = true)
                }
            }
        }
    }

    private fun startRealTimeAutoScan() {
        realTimeJob?.cancel()
        realTimeJob = serviceScope.launch(Dispatchers.Default) {
            while (isActive && isRealTimeActive) {
                val rect = currentMode.getBoundingRect(screenWidth, screenHeight)
                val bitmap = captureBitmapRegion(rect)
                if (bitmap != null) {
                    val currentHash = ImageHashUtil.computeDHash(bitmap)
                    val diff = ImageHashUtil.hammingDistance(lastFrameHash, currentHash)

                    if (diff > 4) {
                        lastFrameHash = currentHash
                        processBitmapAndTranslate(bitmap, isAuto = true)
                    } else {
                        bitmap.recycle()
                    }
                }
                delay(1500)
            }
        }
    }

    private fun captureBitmapRegion(rect: Rect?): Bitmap? {
        val reader = imageReader ?: return null
        val image = reader.acquireLatestImage() ?: return null
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * screenWidth

        val fullBitmap = Bitmap.createBitmap(
            screenWidth + rowPadding / pixelStride,
            screenHeight,
            Bitmap.Config.ARGB_8888
        )
        fullBitmap.copyPixelsFromBuffer(buffer)
        image.close()

        val cleanBitmap = Bitmap.createBitmap(fullBitmap, 0, 0, screenWidth, screenHeight)
        if (cleanBitmap != fullBitmap) fullBitmap.recycle()

        if (rect == null) return cleanBitmap

        val safeW = rect.width().coerceAtMost(screenWidth - rect.left)
        val safeH = rect.height().coerceAtMost(screenHeight - rect.top)
        if (safeW <= 0 || safeH <= 0) return cleanBitmap

        val cropped = Bitmap.createBitmap(cleanBitmap, rect.left, rect.top, safeW, safeH)
        cleanBitmap.recycle()
        return cropped
    }

    private fun captureAndTranslate(rect: Rect?) {
        resultDialogManager.showLoading()
        serviceScope.launch(Dispatchers.Default) {
            val bitmap = captureBitmapRegion(rect) ?: return@launch
            processBitmapAndTranslate(bitmap, isAuto = false)
        }
    }

    private fun startSnippingMode() {
        bubbleManager.setVisible(false)
        val snipView = SnipOverlayView(
            context = this,
            onAreaSelected = { rect ->
                bubbleManager.setVisible(true)
                captureAndTranslate(rect)
            },
            onCancelled = { bubbleManager.setVisible(true) }
        )
        snipView.attach()
    }

    private suspend fun processBitmapAndTranslate(bitmap: Bitmap, isAuto: Boolean) {
        val config = preferencesManager.loadConfig()
        val rawBlocks = ocrEngine.recognizeText(bitmap)
        bitmap.recycle()

        val blocks = if (config.filterIrrelevantElements) {
            textFilter.filterBlocks(rawBlocks)
        } else {
            rawBlocks
        }

        val fullText = blocks.joinToString("\n") { it.originalText }

        if (fullText.isEmpty()) {
            if (!isAuto) {
                withContext(Dispatchers.Main) {
                    resultDialogManager.showResult(TranslationResult("", ""), config.autoCopyToClipboard)
                }
            }
            return
        }

        try {
            var translated = translatorEngine.translateText(fullText, config.sourceLanguage, config.targetLanguage)

            if (config.enableAiMaxGlossary) {
                translated = gameGlossary.applyGlossary(translated)
            }

            withContext(Dispatchers.Main) {
                resultDialogManager.showResult(TranslationResult(fullText, translated, blocks), config.autoCopyToClipboard)

                if (config.enableVoiceAssistant && (currentMode == TranslationMode.LIFEAFTER_QUESTS || isAuto)) {
                    val firstSentence = translated.lines().firstOrNull { it.isNotBlank() } ?: ""
                    if (firstSentence.length in 5..120) {
                        voiceNarrator.speak(firstSentence)
                    }
                }
            }
        } catch (e: Exception) {
            if (!isAuto) {
                withContext(Dispatchers.Main) {
                    resultDialogManager.showResult(TranslationResult(fullText, "", isSuccess = false, errorMessage = e.message), false)
                }
            }
        }
    }

    private fun setupNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Traductor de Juegos", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
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
        isRealTimeActive = false
        realTimeJob?.cancel()
        voiceNarrator.shutdown()
        bubbleManager.hide()
        resultDialogManager.dismiss()
        assistantDialogManager.dismiss()
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
