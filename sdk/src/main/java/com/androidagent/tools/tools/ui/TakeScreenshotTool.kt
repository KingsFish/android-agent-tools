package com.androidagent.tools.tools.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.*
import java.io.ByteArrayOutputStream

class TakeScreenshotTool : Tool {
    override val name = "take_screenshot"
    override val description = "Capture the current screen."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        return Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        val envDetector = EnvironmentDetector(context)
        if (envDetector.hasRoot()) {
            return executeWithRoot()
        }

        val service = AgentAccessibilityService.instance
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
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "screencap -p"))
            val inputStream = process.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            process.waitFor()

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
