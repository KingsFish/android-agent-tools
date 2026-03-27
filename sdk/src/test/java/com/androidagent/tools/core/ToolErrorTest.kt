package com.androidagent.tools.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ToolErrorTest {

    @Test
    fun `error codes have correct messages`() {
        assertEquals("Permission not granted", ToolError.PERMISSION_DENIED.message)
        assertEquals("Invalid parameter provided", ToolError.INVALID_PARAMETER.message)
        assertEquals("File does not exist", ToolError.FILE_NOT_FOUND.message)
        assertEquals("Application not found", ToolError.APP_NOT_FOUND.message)
    }

    @Test
    fun `error creates full message with context`() {
        val error = ToolError.FILE_NOT_FOUND
        val fullMessage = error.withContext("/sdcard/test.txt")
        assertEquals("File does not exist: /sdcard/test.txt", fullMessage)
    }

    @Test
    fun `tier 2 environment errors have correct messages`() {
        assertEquals("Root access required", ToolError.ROOT_REQUIRED.message)
        assertEquals("Accessibility service not enabled", ToolError.ACCESSIBILITY_SERVICE_REQUIRED.message)
        assertEquals("Media projection permission not granted", ToolError.MEDIA_PROJECTION_REQUIRED.message)
    }

    @Test
    fun `tier 2 operation errors have correct messages`() {
        assertEquals("Failed to capture screenshot", ToolError.SCREENSHOT_FAILED.message)
        assertEquals("Failed to perform gesture", ToolError.GESTURE_FAILED.message)
        assertEquals("Failed to get UI tree", ToolError.UI_TREE_FAILED.message)
        assertEquals("Failed to install application", ToolError.INSTALL_FAILED.message)
        assertEquals("Failed to uninstall application", ToolError.UNINSTALL_FAILED.message)
        assertEquals("Failed to force stop application", ToolError.FORCE_STOP_FAILED.message)
        assertEquals("APK file does not exist", ToolError.APK_NOT_FOUND.message)
        assertEquals("Invalid or corrupted APK file", ToolError.INVALID_APK.message)
    }
}