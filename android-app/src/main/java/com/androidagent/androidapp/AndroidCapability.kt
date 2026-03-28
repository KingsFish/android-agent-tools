package com.androidagent.androidapp

import com.androidagent.core.Capability

/**
 * Android 设备能力枚举
 *
 * 表示 Android 设备可能具备的各种能力，用于环境检测。
 */
enum class AndroidCapability : Capability {
    /** ROOT 权限 */
    ROOT,
    /** 无障碍服务 */
    ACCESSIBILITY_SERVICE,
    /** 媒体投影权限 */
    MEDIA_PROJECTION,
    /** 悬浮窗权限 */
    OVERLAY
}