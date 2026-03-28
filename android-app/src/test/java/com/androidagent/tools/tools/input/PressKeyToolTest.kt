package com.androidagent.tools.tools.input

import com.androidagent.core.ToolError
import com.androidagent.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.androidapp.EnvironmentDetector
import com.androidagent.androidapp.AppToolContext

class PressKeyToolTest {
    private val tool = PressKeyTool()

    @Test
    fun `validate fails when neither key_name nor key_code provided`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate succeeds with key_name`() {
        val result = tool.validate(mapOf("key_name" to "enter"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `validate succeeds with key_code`() {
        val result = tool.validate(mapOf("key_code" to 66))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `validate fails with unknown key_name`() {
        val mockContext = mockk<Context>(relaxed = true)
        mockkConstructor(EnvironmentDetector::class)
        every { anyConstructed<EnvironmentDetector>().hasRoot() } returns false

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(AppToolContext(mockContext), mapOf("key_name" to "unknown_key"))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.INVALID_PARAMETER, (result as ToolResult.Failure).error)
    }

    @Test
    fun `execute fails when no ROOT and no accessibility service`() {
        val mockContext = mockk<Context>(relaxed = true)

        mockkConstructor(EnvironmentDetector::class)
        every { anyConstructed<EnvironmentDetector>().hasRoot() } returns false

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(AppToolContext(mockContext), mapOf("key_code" to 66))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.ACCESSIBILITY_SERVICE_REQUIRED, (result as ToolResult.Failure).error)
    }
}