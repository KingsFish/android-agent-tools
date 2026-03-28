package com.androidagent.tools.tools.app

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.androidagent.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ListAppsToolTest {
    private val tool = ListAppsTool()

    @Test
    fun `validate succeeds with no parameters`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `validate succeeds with include_system_apps parameter`() {
        val result = tool.validate(mapOf("include_system_apps" to true))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute returns app list`() {
        val mockContext = mockk<android.content.Context>()
        val mockPackageManager = mockk<PackageManager>()

        val mockAppInfo = mockk<ApplicationInfo>()
        mockAppInfo.packageName = "com.example.app"
        mockAppInfo.flags = 0

        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns listOf(mockAppInfo)
        every { mockPackageManager.getApplicationLabel(mockAppInfo) } returns "Example App"
        every { mockContext.packageName } returns "com.test"

        val mockPackageInfo = mockk<android.content.pm.PackageInfo>()
        mockPackageInfo.versionName = "1.0.0"
        every { mockPackageManager.getPackageInfo("com.example.app", 0) } returns mockPackageInfo

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, emptyMap())
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertTrue(data.containsKey("apps"))
    }

    @Test
    fun `execute filters out current app`() {
        val mockContext = mockk<android.content.Context>()
        val mockPackageManager = mockk<PackageManager>()

        val mockAppInfo1 = mockk<ApplicationInfo>()
        mockAppInfo1.packageName = "com.example.app"
        mockAppInfo1.flags = 0

        val mockAppInfo2 = mockk<ApplicationInfo>()
        mockAppInfo2.packageName = "com.test"
        mockAppInfo2.flags = 0

        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns listOf(mockAppInfo1, mockAppInfo2)
        every { mockPackageManager.getApplicationLabel(mockAppInfo1) } returns "Example App"
        every { mockContext.packageName } returns "com.test"

        val mockPackageInfo = mockk<android.content.pm.PackageInfo>()
        mockPackageInfo.versionName = "1.0.0"
        every { mockPackageManager.getPackageInfo("com.example.app", 0) } returns mockPackageInfo

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, emptyMap())
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        @Suppress("UNCHECKED_CAST")
        val apps = data["apps"] as List<Map<String, Any?>>
        assertEquals(1, apps.size)
        assertEquals("com.example.app", apps[0]["package_name"])
    }

    @Test
    fun `execute filters out system apps when include_system_apps is false`() {
        val mockContext = mockk<android.content.Context>()
        val mockPackageManager = mockk<PackageManager>()

        val mockSystemApp = mockk<ApplicationInfo>()
        mockSystemApp.packageName = "com.system.app"
        mockSystemApp.flags = ApplicationInfo.FLAG_SYSTEM

        val mockUserApp = mockk<ApplicationInfo>()
        mockUserApp.packageName = "com.user.app"
        mockUserApp.flags = 0

        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns listOf(mockSystemApp, mockUserApp)
        every { mockPackageManager.getApplicationLabel(mockUserApp) } returns "User App"
        every { mockContext.packageName } returns "com.test"

        val mockPackageInfo = mockk<android.content.pm.PackageInfo>()
        mockPackageInfo.versionName = "1.0.0"
        every { mockPackageManager.getPackageInfo("com.user.app", 0) } returns mockPackageInfo

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("include_system_apps" to false))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        @Suppress("UNCHECKED_CAST")
        val apps = data["apps"] as List<Map<String, Any?>>
        assertEquals(1, apps.size)
        assertEquals("com.user.app", apps[0]["package_name"])
    }

    @Test
    fun `execute includes system apps when include_system_apps is true`() {
        val mockContext = mockk<android.content.Context>()
        val mockPackageManager = mockk<PackageManager>()

        val mockSystemApp = mockk<ApplicationInfo>()
        mockSystemApp.packageName = "com.system.app"
        mockSystemApp.flags = ApplicationInfo.FLAG_SYSTEM

        val mockUserApp = mockk<ApplicationInfo>()
        mockUserApp.packageName = "com.user.app"
        mockUserApp.flags = 0

        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA) } returns listOf(mockSystemApp, mockUserApp)
        every { mockPackageManager.getApplicationLabel(mockSystemApp) } returns "System App"
        every { mockPackageManager.getApplicationLabel(mockUserApp) } returns "User App"
        every { mockContext.packageName } returns "com.test"

        val mockSystemPackageInfo = mockk<android.content.pm.PackageInfo>()
        mockSystemPackageInfo.versionName = "1.0.0"
        every { mockPackageManager.getPackageInfo("com.system.app", 0) } returns mockSystemPackageInfo

        val mockUserPackageInfo = mockk<android.content.pm.PackageInfo>()
        mockUserPackageInfo.versionName = "2.0.0"
        every { mockPackageManager.getPackageInfo("com.user.app", 0) } returns mockUserPackageInfo

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("include_system_apps" to true))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        @Suppress("UNCHECKED_CAST")
        val apps = data["apps"] as List<Map<String, Any?>>
        assertEquals(2, apps.size)
    }
}