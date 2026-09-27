package com.bscl.gametranslator.util

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

object ImageHashUtil {

    /**
     * Computes a 64-bit difference hash (dHash) from a bitmap.
     * Resizes to 9x8 grayscale and computes horizontal gradient bits.
     */
    fun computeDHash(bitmap: Bitmap): Long {
        val scaled = Bitmap.createScaledBitmap(bitmap, 9, 8, true)
        var hash = 0L

        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val pixelLeft = scaled.getPixel(x, y)
                val pixelRight = scaled.getPixel(x + 1, y)

                val grayLeft = toGrayscale(pixelLeft)
                val grayRight = toGrayscale(pixelRight)

                if (grayLeft > grayRight) {
                    hash = hash or (1L shl (y * 8 + x))
                }
            }
        }

        if (scaled != bitmap) {
            scaled.recycle()
        }

        return hash
    }

    /**
     * Calculates the Hamming distance (number of bit differences) between two hashes.
     * Distance <= 5 indicates images are perceptually nearly identical.
     */
    fun hammingDistance(hash1: Long, hash2: Long): Int {
        return java.lang.Long.bitCount(hash1 xor hash2)
    }

    private fun toGrayscale(color: Int): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }
}
