package com.androidagent.tools.tools.ui

import com.androidagent.core.ToolError
import com.androidagent.core.ToolResult
import com.androidagent.tools.accessibility.AgentAccessibilityService
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
import com.androidagent.androidapp.AppToolContext

class GetUiTreeToolTest {
    private val tool = GetUiTreeTool()

    @BeforeEach
    fun setUp() {
        mockkObject(AgentAccessibilityService)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(AgentAccessibilityService)
    }

    @Test
    fun `validate succeeds with no parameters`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `validate succeeds with optional parameters`() {
        val result = tool.validate(mapOf(
            "max_depth" to 5,
            "include_invisible" to true
        ))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute fails when accessibility service not running`() {
        val mockContext = mockk<Context>(relaxed = true)
        val mockToolContext = spyk(AppToolContext(mockContext))

        every { AgentAccessibilityService.instance } returns null
        every { mockToolContext.getAccessibilityService() } returns null

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockToolContext, emptyMap())
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.ACCESSIBILITY_SERVICE_REQUIRED, (result as ToolResult.Failure).error)
    }

    @Test
    fun `execute succeeds when accessibility service is running`() {
        val mockContext = mockk<Context>(relaxed = true)
        val mockService = mockk<AgentAccessibilityService>()
        val mockToolContext = spyk(AppToolContext(mockContext))

        every { AgentAccessibilityService.instance } returns mockService
        every { mockToolContext.getAccessibilityService() } returns mockService
        every { mockService.getUiTree(any(), any()) } returns mapOf(
            "nodes" to emptyList<Map<String, Any?>>(),
            "package_name" to "com.example",
            "node_count" to 0
        )

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockToolContext, emptyMap())
        }

        assertTrue(result is ToolResult.Success)
    }

    @Test
    fun `execute fails when getUiTree returns null`() {
        val mockContext = mockk<Context>(relaxed = true)
        val mockService = mockk<AgentAccessibilityService>()
        val mockToolContext = spyk(AppToolContext(mockContext))

        every { AgentAccessibilityService.instance } returns mockService
        every { mockToolContext.getAccessibilityService() } returns mockService
        every { mockService.getUiTree(any(), any()) } returns null

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockToolContext, emptyMap())
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.UI_TREE_FAILED, (result as ToolResult.Failure).error)
    }
}
