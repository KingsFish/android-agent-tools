package com.androidagent.tools.tools.ui

import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.tools.core.EnvironmentDetector
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import android.content.Context

class TapToolTest {
    private val tool = TapTool()

    @Test
    fun `validate fails when x is missing`() {
        val result = tool.validate(mapOf("y" to 100))
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate fails when y is missing`() {
        val result = tool.validate(mapOf("x" to 100))
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate fails when both x and y are missing`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate succeeds with x and y`() {
        val result = tool.validate(mapOf("x" to 100, "y" to 200))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `validate accepts integer values`() {
        val result = tool.validate(mapOf("x" to 0, "y" to 0))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute fails when no ROOT and no accessibility service`() {
        val mockContext = mockk<Context>(relaxed = true)

        mockkConstructor(EnvironmentDetector::class)
        every { anyConstructed<EnvironmentDetector>().hasRoot() } returns false

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("x" to 100, "y" to 200))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.ACCESSIBILITY_SERVICE_REQUIRED, (result as ToolResult.Failure).error)
    }
}
