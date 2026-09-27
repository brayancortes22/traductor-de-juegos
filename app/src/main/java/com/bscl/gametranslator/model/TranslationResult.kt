package com.bscl.gametranslator.model

import android.graphics.Rect

data class DetectedTextBlock(
    val originalText: String,
    val boundingBox: Rect?
)

data class TranslationResult(
    val originalFullText: String,
    val translatedFullText: String,
    val blocks: List<DetectedTextBlock> = emptyList(),
    val isSuccess: Boolean = true,
    val errorMessage: String? = null,
    val executionTimeMs: Long = 0L
)
