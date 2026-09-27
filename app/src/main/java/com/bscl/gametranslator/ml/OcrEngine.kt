package com.bscl.gametranslator.ml

import android.graphics.Bitmap
import com.bscl.gametranslator.model.DetectedTextBlock
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OcrEngine {

    private val recognizer: TextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun recognizeText(bitmap: Bitmap): List<DetectedTextBlock> =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(image)
                .addOnSuccessListener { visionText: Text ->
                    val blocks = mutableListOf<DetectedTextBlock>()
                    for (block in visionText.textBlocks) {
                        val text = block.text.trim()
                        if (text.isNotEmpty()) {
                            blocks.add(
                                DetectedTextBlock(
                                    originalText = text,
                                    boundingBox = block.boundingBox
                                )
                            )
                        }
                    }
                    continuation.resume(blocks)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }

    fun close() {
        recognizer.close()
    }
}
