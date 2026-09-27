package com.bscl.gametranslator.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bscl.gametranslator.data.PreferencesManager
import com.bscl.gametranslator.databinding.ActivitySettingsBinding
import com.bscl.gametranslator.model.AppConfig
import com.bscl.gametranslator.model.SupportedLanguage

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setupSpinners()
        loadCurrentConfig()
        setupListeners()
    }

    private fun setupSpinners() {
        val languageNames = SupportedLanguage.entries.map { it.displayName }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            languageNames
        )

        binding.spSourceLang.adapter = adapter
        binding.spTargetLang.adapter = adapter
    }

    private fun loadCurrentConfig() {
        val config = preferencesManager.loadConfig()

        val sourceIndex = SupportedLanguage.entries.indexOf(config.sourceLanguage)
        if (sourceIndex >= 0) binding.spSourceLang.setSelection(sourceIndex)

        val targetIndex = SupportedLanguage.entries.indexOf(config.targetLanguage)
        if (targetIndex >= 0) binding.spTargetLang.setSelection(targetIndex)

        binding.sliderOpacity.value = (config.bubbleOpacity * 100).coerceIn(20f, 100f)
        binding.switchAutoCopy.isChecked = config.autoCopyToClipboard
    }

    private fun setupListeners() {
        binding.btnSaveSettings.setOnClickListener {
            saveConfig()
        }
    }

    private fun saveConfig() {
        val sourceLang = SupportedLanguage.entries[binding.spSourceLang.selectedItemPosition]
        val targetLang = SupportedLanguage.entries[binding.spTargetLang.selectedItemPosition]
        val opacity = binding.sliderOpacity.value / 100f
        val autoCopy = binding.switchAutoCopy.isChecked

        val newConfig = AppConfig(
            sourceLanguage = sourceLang,
            targetLanguage = targetLang,
            bubbleOpacity = opacity,
            autoCopyToClipboard = autoCopy
        )

        preferencesManager.saveConfig(newConfig)
        Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
        finish()
    }
}
