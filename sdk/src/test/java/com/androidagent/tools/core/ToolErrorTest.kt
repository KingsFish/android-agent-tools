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
}