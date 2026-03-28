package com.androidagent.tools.tools.appmgmt

import android.content.Context
import com.androidagent.core.ToolError
import com.androidagent.core.ToolResult
import com.androidagent.androidapp.EnvironmentDetector
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import com.androidagent.androidapp.AppToolContext

class InstallAppToolTest {
    private val tool = InstallAppTool()

    @Test
    fun `validate fails when apk_path is missing`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate succeeds with apk_path`() {
        val result = tool.validate(mapOf("apk_path" to "/sdcard/app.apk"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute fails when APK file does not exist`() {
        val mockContext = mockk<Context>(relaxed = true)

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(AppToolContext(mockContext), mapOf("apk_path" to "/sdcard/nonexistent.apk"))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.APK_NOT_FOUND, (result as ToolResult.Failure).error)
    }

    @Test
    fun `execute fails when file is not an APK`() {
        val mockContext = mockk<Context>(relaxed = true)

        // Create a temporary non-APK file
        val tempFile = java.io.File.createTempFile("test", ".txt")
        tempFile.deleteOnExit()

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(AppToolContext(mockContext), mapOf("apk_path" to tempFile.absolutePath))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.INVALID_APK, (result as ToolResult.Failure).error)
    }
}
