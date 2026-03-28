package com.androidagent.tools.tools.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import com.androidagent.core.*
import com.androidagent.androidapp.AndroidCapability
import com.androidagent.androidapp.AppToolContext
import java.io.ByteArrayOutputStream

class TakeScreenshotTool : Tool {
    override val name = "take_screenshot"
    override val description = "Capture the current screen."
    override val inputSchema = ToolSchema.noParams()

    override fun validate(params: Map<String, Any?>): ValidationResult {
        return ValidationResult.success()
    }

    override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult {
        val appContext = context as? AppToolContext
            ?: return ToolResult.failure(ToolError.UNSUPPORTED_OPERATION, "Requires Android context")

        if (context.hasCapability(AndroidCapability.ROOT)) {
            return executeWithRoot()
        }

        val service = appContext.getAccessibilityService()
            ?: return ToolResult.failure(ToolError.ACCESSIBILITY_SERVICE_REQUIRED)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return ToolResult.failure(ToolError.MEDIA_PROJECTION_REQUIRED,
                "Screenshot requires Android 11+ or ROOT access")
        }

        val bitmap = service.takeScreenshot()
            ?: return ToolResult.failure(ToolError.SCREENSHOT_FAILED, "Failed to capture screen")

        return bitmapToResult(bitmap)
    }

    private fun executeWithRoot(): ToolResult {
        return try {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", "screencap -p"))
            val inputStream = proc.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            proc.waitFor()

            if (bitmap != null) {
                bitmapToResult(bitmap)
            } else {
                ToolResult.failure(ToolError.SCREENSHOT_FAILED, "Failed to decode screenshot")
            }
        } catch (e: Exception) {
            ToolResult.failure(ToolError.SCREENSHOT_FAILED, e.message)
        }
    }

    private fun bitmapToResult(bitmap: Bitmap): ToolResult {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

        return ToolResult.success(mapOf(
            "image_base64" to base64,
            "width" to bitmap.width,
            "height" to bitmap.height
        ))
    }
}