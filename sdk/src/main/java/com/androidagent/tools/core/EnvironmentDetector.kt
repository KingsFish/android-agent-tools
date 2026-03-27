package com.androidagent.tools.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

/**
 * 环境检测器
 *
 * 用于检测设备的各种能力，包括 ROOT 权限、无障碍服务、媒体投影和悬浮窗权限。
 * UI 交互工具会根据这些能力选择实现方式。
 */
class EnvironmentDetector(private val context: Context) {

    /**
     * 检查设备是否具备指定能力
     *
     * @param capability 要检查的能力
     * @return 如果具备该能力返回 true，否则返回 false
     */
    fun hasCapability(capability: Capability): Boolean {
        return when (capability) {
            Capability.ROOT -> hasRoot()
            Capability.ACCESSIBILITY_SERVICE -> hasAccessibilityService()
            Capability.MEDIA_PROJECTION -> hasMediaProjection()
            Capability.OVERLAY -> hasOverlay()
        }
    }

    /**
     * 检查设备是否具有 ROOT 权限
     *
     * 通过尝试执行 su 命令来检测 ROOT 权限。
     *
     * @return 如果设备已 ROOT 返回 true，否则返回 false
     */
    fun hasRoot(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = process.outputStream
            outputStream.write("exit\n".toByteArray())
            outputStream.flush()
            outputStream.close()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查无障碍服务是否已启用
     *
     * 检查本应用的无障碍服务是否已在系统设置中启用。
     *
     * @return 如果无障碍服务已启用返回 true，否则返回 false
     */
    fun hasAccessibilityService(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val serviceName = "${context.packageName}/com.androidagent.tools.accessibility.AgentAccessibilityService"
        return enabledServices.contains(serviceName)
    }

    /**
     * 检查是否支持媒体投影
     *
     * 媒体投影从 Android 5.0 (API 21) 开始支持。
     *
     * @return 如果设备支持媒体投影返回 true，否则返回 false
     */
    fun hasMediaProjection(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    /**
     * 检查是否具有悬浮窗权限
     *
     * Android 6.0 (API 23) 及以上需要显式请求悬浮窗权限。
     *
     * @return 如果具有悬浮窗权限返回 true，否则返回 false
     */
    fun hasOverlay(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * 请求打开无障碍服务设置页面
     *
     * 引导用户到系统无障碍设置页面以启用服务。
     */
    fun requestAccessibilityService() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * 请求打开悬浮窗权限设置页面
     *
     * 引导用户到系统悬浮窗权限设置页面。
     */
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

    /**
     * 获取所有能力及其状态
     *
     * @return 能力到其状态的映射
     */
    val capabilities: Map<Capability, Boolean>
        get() = Capability.entries.associateWith { hasCapability(it) }
}