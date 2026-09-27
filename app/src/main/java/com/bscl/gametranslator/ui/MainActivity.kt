package com.bscl.gametranslator.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bscl.gametranslator.R
import com.bscl.gametranslator.data.PreferencesManager
import com.bscl.gametranslator.databinding.ActivityMainBinding
import com.bscl.gametranslator.ml.TranslatorEngine
import com.bscl.gametranslator.service.ScreenCaptureService
import com.bscl.gametranslator.util.CrashLogger
import android.content.ClipData
import android.content.ClipboardManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager
    private val translatorEngine = TranslatorEngine()
    private var isServiceRunning = false

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updatePermissionStatus()
    }

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            startCaptureService(result.resultCode, result.data!!)
        } else {
            Toast.makeText(this, "Permiso de captura cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashLogger.init(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setupListeners()
        updatePermissionStatus()
        checkOfflineModel()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun setupListeners() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnPermissionOverlay.setOnClickListener {
            requestOverlayPermission()
        }

        binding.btnDownloadModel.setOnClickListener {
            downloadOfflineModel()
        }

        binding.btnViewLogs.setOnClickListener {
            showLogsDialog()
        }

        binding.btnToggleService.setOnClickListener {
            if (isServiceRunning) {
                stopCaptureService()
            } else {
                initiateServiceStart()
            }
        }
    }

    private fun showLogsDialog() {
        val crashLog = CrashLogger.getLastCrash(this)
        val message = crashLog ?: getString(R.string.logs_empty)

        val builder = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logs_dialog_title)
            .setMessage(message)
            .setPositiveButton("Cerrar", null)

        if (crashLog != null) {
            builder.setNeutralButton("Copiar Log") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("GameTranslator Crash Log", crashLog)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Log copiado al portapapeles", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("Borrar Log") { _, _ ->
                CrashLogger.clearCrashLog(this)
                Toast.makeText(this, "Registro de errores limpiado", Toast.LENGTH_SHORT).show()
            }
        }

        builder.show()
    }

    private fun updatePermissionStatus() {
        val hasOverlay = Settings.canDrawOverlays(this)
        if (hasOverlay) {
            binding.tvOverlayStatus.setText(R.string.status_permission_granted)
            binding.btnPermissionOverlay.visibility = View.GONE
        } else {
            binding.tvOverlayStatus.setText(R.string.status_permission_needed)
            binding.btnPermissionOverlay.visibility = View.VISIBLE
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlayPermissionLauncher.launch(intent)
    }

    private var isDownloadingOfflineModel = false

    private fun checkOfflineModel() {
        val config = preferencesManager.loadConfig()
        lifecycleScope.launch {
            val isDownloaded = translatorEngine.isModelDownloaded(config.targetLanguage)
            if (isDownloaded) {
                binding.tvModelStatus.setText(R.string.status_model_ready)
                binding.btnDownloadModel.setText(R.string.btn_verify_model)
                binding.pbDownloadModel.visibility = View.GONE
            } else {
                binding.btnDownloadModel.setText(R.string.btn_download_model_optional)
                startBackgroundModelDownload()
            }
        }
    }

    private fun startBackgroundModelDownload() {
        if (isDownloadingOfflineModel) return
        isDownloadingOfflineModel = true
        val config = preferencesManager.loadConfig()

        binding.pbDownloadModel.visibility = View.VISIBLE
        binding.tvModelStatus.text = "⬇️ Descargando paquete offline en segundo plano (traducción online activa)..."

        lifecycleScope.launch(Dispatchers.IO) {
            val success = translatorEngine.ensureModelDownloaded(
                config.sourceLanguage,
                config.targetLanguage,
                requireWifi = false
            )
            withContext(Dispatchers.Main) {
                isDownloadingOfflineModel = false
                binding.pbDownloadModel.visibility = View.GONE
                if (success) {
                    binding.tvModelStatus.setText(R.string.status_model_ready)
                    binding.btnDownloadModel.setText(R.string.btn_verify_model)
                    Toast.makeText(this@MainActivity, "✅ Paquete offline listo. Ya puedes traducir sin internet.", Toast.LENGTH_SHORT).show()
                } else {
                    binding.tvModelStatus.setText(R.string.status_model_not_ready)
                }
            }
        }
    }

    private fun downloadOfflineModel() {
        val config = preferencesManager.loadConfig()
        lifecycleScope.launch {
            val alreadyDownloaded = translatorEngine.isModelDownloaded(config.targetLanguage)
            if (alreadyDownloaded) {
                Toast.makeText(this@MainActivity, "✅ El modelo offline ya está instalado y listo para usar.", Toast.LENGTH_SHORT).show()
                binding.tvModelStatus.setText(R.string.status_model_ready)
                binding.btnDownloadModel.setText(R.string.btn_verify_model)
            } else {
                startBackgroundModelDownload()
                Toast.makeText(this@MainActivity, "Descargando modelo de Google ML Kit en segundo plano...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initiateServiceStart() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Concede el permiso de superposición primero", Toast.LENGTH_SHORT).show()
            requestOverlayPermission()
            return
        }

        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjectionLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    private fun startCaptureService(resultCode: Int, data: Intent) {
        val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
            putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
            putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, data)
        }

        ContextCompat.startForegroundService(this, serviceIntent)
        isServiceRunning = true
        binding.btnToggleService.setText(R.string.btn_stop_service)
        binding.btnToggleService.setBackgroundColor(getColor(R.color.accent_red))
        Toast.makeText(this, "¡Servicio activo! Abre LifeAfter", Toast.LENGTH_SHORT).show()
    }

    private fun stopCaptureService() {
        val serviceIntent = Intent(this, ScreenCaptureService::class.java)
        stopService(serviceIntent)
        isServiceRunning = false
        binding.btnToggleService.setText(R.string.btn_start_service)
        binding.btnToggleService.setBackgroundColor(getColor(R.color.primary))
    }

    override fun onDestroy() {
        super.onDestroy()
        translatorEngine.close()
    }
}
