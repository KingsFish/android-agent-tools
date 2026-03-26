package com.androidagent.tools.tools.app

import android.content.pm.PackageManager
import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class GetAppInfoToolTest {
    private val tool = GetAppInfoTool()

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
    fun `execute fails when app not found`() {
        val mockContext = mockk<android.content.Context>()
        val mockPackageManager = mockk<PackageManager>()

        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackageInfo("com.nonexistent.app", PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA) } throws PackageManager.NameNotFoundException()

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("package_name" to "com.nonexistent.app"))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.APP_NOT_FOUND, (result as ToolResult.Failure).error)
    }

    @Test
    fun `execute returns app info for valid package`() {
        val mockContext = mockk<android.content.Context>()
        val mockPackageManager = mockk<PackageManager>()

        val mockAppInfo = mockk<android.content.pm.ApplicationInfo>()
        mockAppInfo.packageName = "com.example.app"
        mockAppInfo.flags = 0
        mockAppInfo.dataDir = "/data/data/com.example.app"

        val mockPackageInfo = mockk<android.content.pm.PackageInfo>()
        mockPackageInfo.applicationInfo = mockAppInfo
        mockPackageInfo.versionName = "1.0.0"
        mockPackageInfo.firstInstallTime = 1000L
        mockPackageInfo.lastUpdateTime = 2000L
        mockPackageInfo.requestedPermissions = arrayOf("android.permission.INTERNET")

        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackageInfo("com.example.app", PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA) } returns mockPackageInfo
        every { mockPackageManager.getApplicationLabel(mockAppInfo) } returns "Example App"

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("package_name" to "com.example.app"))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertEquals("com.example.app", data["package_name"])
        assertEquals("Example App", data["label"])
        assertEquals("1.0.0", data["version_name"])
        assertEquals(false, data["is_system"])
        assertEquals(1000L, data["install_time"])
        assertEquals(2000L, data["update_time"])
        assertEquals("/data/data/com.example.app", data["data_dir"])
    }

    @Test
    fun `execute identifies system apps correctly`() {
        val mockContext = mockk<android.content.Context>()
        val mockPackageManager = mockk<PackageManager>()

        val mockAppInfo = mockk<android.content.pm.ApplicationInfo>()
        mockAppInfo.packageName = "com.system.app"
        mockAppInfo.flags = android.content.pm.ApplicationInfo.FLAG_SYSTEM
        mockAppInfo.dataDir = "/data/data/com.system.app"

        val mockPackageInfo = mockk<android.content.pm.PackageInfo>()
        mockPackageInfo.applicationInfo = mockAppInfo
        mockPackageInfo.versionName = "1.0.0"
        mockPackageInfo.firstInstallTime = 1000L
        mockPackageInfo.lastUpdateTime = 2000L
        mockPackageInfo.requestedPermissions = null

        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackageInfo("com.system.app", PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA) } returns mockPackageInfo
        every { mockPackageManager.getApplicationLabel(mockAppInfo) } returns "System App"

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("package_name" to "com.system.app"))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertEquals(true, data["is_system"])
    }
}