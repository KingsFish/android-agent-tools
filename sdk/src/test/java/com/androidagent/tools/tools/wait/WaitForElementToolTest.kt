package com.androidagent.tools.tools.wait

import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService

class WaitForElementToolTest {
    private val tool = WaitForElementTool()

    @Test
    fun `validate fails when text is missing`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate fails when text is empty`() {
        val result = tool.validate(mapOf("text" to ""))
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate succeeds with text`() {
        val result = tool.validate(mapOf("text" to "Login"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute fails when no accessibility service`() {
        val mockContext = mockk<Context>(relaxed = true)

        mockkObject(AgentAccessibilityService)
        every { AgentAccessibilityService.instance } returns null

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("text" to "Login"))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.ACCESSIBILITY_SERVICE_REQUIRED, (result as ToolResult.Failure).error)
    }
}