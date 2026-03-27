package com.androidagent.tools.tools.permission

import android.content.pm.PackageManager
import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CheckPermissionsToolTest {
    private val tool = CheckPermissionsTool()

    @Test
    fun `validate fails when permissions is missing`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate fails when permissions is not array`() {
        val result = tool.validate(mapOf("permissions" to "storage"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate succeeds with valid permissions array`() {
        val result = tool.validate(mapOf("permissions" to listOf("storage", "camera")))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute returns permission status`() {
        val mockContext = mockk<android.content.Context>()

        every { mockContext.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission("android.permission.CAMERA") } returns PackageManager.PERMISSION_DENIED

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("permissions" to listOf("storage", "camera")))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertEquals("granted", data["storage"])
        assertEquals("denied", data["camera"])
    }

    @Test
    fun `execute returns denied when any permission in group is denied`() {
        val mockContext = mockk<android.content.Context>()

        // storage group has READ and WRITE, only READ is granted
        every { mockContext.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") } returns PackageManager.PERMISSION_DENIED

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("permissions" to listOf("storage")))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertEquals("denied", data["storage"])
    }

    @Test
    fun `execute returns unknown for unrecognized permission names`() {
        val mockContext = mockk<android.content.Context>()

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("permissions" to listOf("unknown_permission")))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertEquals("unknown", data["unknown_permission"])
    }

    @Test
    fun `execute handles multiple permissions including unknown`() {
        val mockContext = mockk<android.content.Context>()

        every { mockContext.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission("android.permission.CAMERA") } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") } returns PackageManager.PERMISSION_DENIED
        every { mockContext.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") } returns PackageManager.PERMISSION_GRANTED

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("permissions" to listOf("storage", "camera", "location", "unknown_perm")))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertEquals("granted", data["storage"])
        assertEquals("granted", data["camera"])
        assertEquals("denied", data["location"]) // one denied means group is denied
        assertEquals("unknown", data["unknown_perm"])
    }

    @Test
    fun `execute handles all permission types`() {
        val mockContext = mockk<android.content.Context>()

        // Storage - granted
        every { mockContext.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") } returns PackageManager.PERMISSION_GRANTED

        // Camera - granted
        every { mockContext.checkSelfPermission("android.permission.CAMERA") } returns PackageManager.PERMISSION_GRANTED

        // Location - granted
        every { mockContext.checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") } returns PackageManager.PERMISSION_GRANTED

        // Microphone - granted
        every { mockContext.checkSelfPermission("android.permission.RECORD_AUDIO") } returns PackageManager.PERMISSION_GRANTED

        // Contacts - granted
        every { mockContext.checkSelfPermission("android.permission.READ_CONTACTS") } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission("android.permission.WRITE_CONTACTS") } returns PackageManager.PERMISSION_GRANTED

        // SMS - granted
        every { mockContext.checkSelfPermission("android.permission.SEND_SMS") } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission("android.permission.RECEIVE_SMS") } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission("android.permission.READ_SMS") } returns PackageManager.PERMISSION_GRANTED

        // Phone - granted
        every { mockContext.checkSelfPermission("android.permission.READ_PHONE_STATE") } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.checkSelfPermission("android.permission.CALL_PHONE") } returns PackageManager.PERMISSION_GRANTED

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("permissions" to listOf("storage", "camera", "location", "microphone", "contacts", "sms", "phone")))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertEquals("granted", data["storage"])
        assertEquals("granted", data["camera"])
        assertEquals("granted", data["location"])
        assertEquals("granted", data["microphone"])
        assertEquals("granted", data["contacts"])
        assertEquals("granted", data["sms"])
        assertEquals("granted", data["phone"])
    }
}