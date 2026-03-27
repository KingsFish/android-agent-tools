package com.androidagent.tools.tools.system

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.androidagent.tools.core.Result
import com.androidagent.tools.core.Tool
import com.androidagent.tools.core.ToolResult
import java.util.Locale
import java.util.TimeZone

class GetDeviceInfoTool : Tool {
    override val name = "get_device_info"
    override val description = "Get device information including model, OS version, screen size, etc."

    override fun validate(params: Map<String, Any?>): Result<Unit> {
        // No parameters required
        return Result.Success(Unit)
    }

    override suspend fun execute(context: Context, params: Map<String, Any?>): ToolResult {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val (screenWidth, screenHeight, densityDpi) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // API 30+ (Android 11+): Use modern API
                val bounds = windowManager.currentWindowMetrics.bounds
                val density = context.resources.displayMetrics.densityDpi
                Triple(bounds.width(), bounds.height(), density)
            } else {
                // API 24-29: Use deprecated API with suppression
                @Suppress("DEPRECATION")
                val display = windowManager.defaultDisplay
                val metrics = DisplayMetrics()
                @Suppress("DEPRECATION")
                display.getMetrics(metrics)
                Triple(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi)
            }

            val deviceInfo = mapOf(
                "brand" to Build.BRAND,
                "model" to Build.MODEL,
                "device" to Build.DEVICE,
                "android_version" to Build.VERSION.RELEASE,
                "sdk_version" to Build.VERSION.SDK_INT,
                "screen_width" to screenWidth,
                "screen_height" to screenHeight,
                "screen_density" to densityDpi,
                "locale" to Locale.getDefault().toString(),
                "timezone" to TimeZone.getDefault().id
            )

            ToolResult.success(deviceInfo)
        } catch (e: Exception) {
            ToolResult.failure(
                com.androidagent.tools.core.ToolError.UNSUPPORTED_OPERATION,
                "Failed to get device info: ${e.message}"
            )
        }
    }
}