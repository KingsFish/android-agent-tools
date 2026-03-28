package com.androidagent.tools.tools.appmgmt

import android.content.Context
import android.content.pm.PackageManager
import com.androidagent.core.ToolError
import com.androidagent.core.ToolResult
import com.androidagent.androidapp.AndroidCapability
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import com.androidagent.androidapp.AppToolContext

class ForceStopAppToolTest {
    private val tool = ForceStopAppTool()

    @Test
    fun `validate fails when package_name is missing`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate succeeds with package_name`() {
        val result = tool.validate(mapOf("package_name" to "com.example.app"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute fails when app not found`() {
        val mockContext = mockk<Context>(relaxed = true)
        val mockPackageManager = mockk<PackageManager>()
        val mockToolContext = spyk(AppToolContext(mockContext))

        every { mockContext.packageManager } returns mockPackageManager
        every { mockContext.packageName } returns "com.test"
        every { mockPackageManager.getPackageInfo("com.example.app", 0) } throws
            PackageManager.NameNotFoundException()

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockToolContext, mapOf("package_name" to "com.example.app"))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.APP_NOT_FOUND, (result as ToolResult.Failure).error)
    }

    @Test
    fun `execute fails when no ROOT access`() {
        val mockContext = mockk<Context>(relaxed = true)
        val mockPackageManager = mockk<PackageManager>()
        val mockToolContext = spyk(AppToolContext(mockContext))

        every { mockContext.packageManager } returns mockPackageManager
        every { mockContext.packageName } returns "com.test"
        every { mockPackageManager.getPackageInfo("com.example.app", 0) } returns mockk()
        every { mockToolContext.hasCapability(AndroidCapability.ROOT) } returns false

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockToolContext, mapOf("package_name" to "com.example.app"))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.ROOT_REQUIRED, (result as ToolResult.Failure).error)
    }
}
