package com.androidagent.androidapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * 环境检测器
 */
class EnvironmentDetector(private val context: Context) {

    fun hasCapability(capability: AndroidCapability): Boolean {
        return when (capability) {
            AndroidCapability.ROOT -> hasRoot()
            AndroidCapability.ACCESSIBILITY_SERVICE -> hasAccessibilityService()
            AndroidCapability.MEDIA_PROJECTION -> hasMediaProjection()
            AndroidCapability.OVERLAY -> hasOverlay()
        }
    }

    fun hasRoot(): Boolean {
        return try {
            val proc = java.lang.Runtime.getRuntime().exec("su")
            val outputStream = proc.outputStream
            outputStream.write("exit\n".toByteArray())
            outputStream.flush()
            outputStream.close()
            proc.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun hasAccessibilityService(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val serviceName = "${context.packageName}/com.androidagent.tools.accessibility.AgentAccessibilityService"
        return enabledServices.contains(serviceName)
    }

    fun hasMediaProjection(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    fun hasOverlay(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun requestAccessibilityService() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    val capabilities: Map<AndroidCapability, Boolean>
        get() = AndroidCapability.entries.associateWith { hasCapability(it) }
}
