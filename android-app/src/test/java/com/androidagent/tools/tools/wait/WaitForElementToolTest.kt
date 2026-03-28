package com.androidagent.tools.tools.wait

import com.androidagent.core.ToolError
import com.androidagent.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import android.content.Context
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.androidapp.AppToolContext

class WaitForElementToolTest {
    private val tool = WaitForElementTool()

    @BeforeEach
    fun setUp() {
        mockkObject(AgentAccessibilityService)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(AgentAccessibilityService)
    }

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
        val mockToolContext = spyk(AppToolContext(mockContext))

        every { AgentAccessibilityService.instance } returns null
        every { mockToolContext.getAccessibilityService() } returns null

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockToolContext, mapOf("text" to "Login"))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.ACCESSIBILITY_SERVICE_REQUIRED, (result as ToolResult.Failure).error)
    }
}