package com.bscl.gametranslator.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashLogger private constructor(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            saveCrash(context, thread, throwable)
        } catch (_: Exception) {
        } finally {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        private const val TAG = "CrashLogger"
        private const val CRASH_FILE_NAME = "crash_report.log"

        fun init(context: Context) {
            val handler = CrashLogger(context.applicationContext)
            Thread.setDefaultUncaughtExceptionHandler(handler)
        }

        fun log(tag: String, message: String, throwable: Throwable? = null) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.i(tag, message)
            }
        }

        fun saveCrash(context: Context, thread: Thread, throwable: Throwable) {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stackTrace = sw.toString()

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val report = """
                ==============================
                CRASH: $timestamp
                Thread: ${thread.name} (id: ${thread.id})
                Message: ${throwable.message}
                Stacktrace:
                $stackTrace
                ==============================
            """.trimIndent()

            Log.e(TAG, report)

            try {
                val file = File(context.filesDir, CRASH_FILE_NAME)
                file.appendText(report + "\n\n")
            } catch (e: Exception) {
                Log.e(TAG, "Error al escribir crash en archivo", e)
            }
        }

        fun getLastCrash(context: Context): String? {
            return try {
                val file = File(context.filesDir, CRASH_FILE_NAME)
                if (file.exists() && file.length() > 0) file.readText() else null
            } catch (_: Exception) {
                null
            }
        }

        fun clearCrashLog(context: Context) {
            try {
                val file = File(context.filesDir, CRASH_FILE_NAME)
                if (file.exists()) file.delete()
            } catch (_: Exception) {
            }
        }
    }
}
