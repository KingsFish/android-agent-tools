package com.androidagent.tools.tools.app

import android.content.Intent
import android.content.pm.PackageManager
import com.androidagent.core.ToolError
import com.androidagent.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class LaunchAppToolTest {
    private val tool = LaunchAppTool()

    @Test
    fun `validate fails when package_name is missing`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate succeeds with valid package_name`() {
        val result = tool.validate(mapOf("package_name" to "com.example.app"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute launches app successfully`() {
        val mockContext = mockk<android.content.Context>(relaxed = true)
        val mockPackageManager = mockk<PackageManager>()
        val mockIntent = mockk<Intent>(relaxed = true)

        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getLaunchIntentForPackage("com.example.app") } returns mockIntent
        every { mockContext.startActivity(any()) } returns Unit

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("package_name" to "com.example.app"))
        }

        assertTrue(result is ToolResult.Success)
        verify { mockContext.startActivity(any()) }
    }

    @Test
    fun `execute fails when app not launchable`() {
        val mockContext = mockk<android.content.Context>()
        val mockPackageManager = mockk<PackageManager>()

        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getLaunchIntentForPackage("com.example.app") } returns null

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("package_name" to "com.example.app"))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.APP_NOT_LAUNCHABLE, (result as ToolResult.Failure).error)
    }

    @Test
    fun `execute fails with LAUNCH_FAILED on exception`() {
        val mockContext = mockk<android.content.Context>()
        val mockPackageManager = mockk<PackageManager>()
        val mockIntent = mockk<Intent>(relaxed = true)

        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getLaunchIntentForPackage("com.example.app") } returns mockIntent
        every { mockContext.startActivity(any()) } throws SecurityException("Permission denied")

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("package_name" to "com.example.app"))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.LAUNCH_FAILED, (result as ToolResult.Failure).error)
    }
}