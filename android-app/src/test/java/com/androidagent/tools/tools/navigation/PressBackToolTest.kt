package com.androidagent.tools.tools.navigation

import com.androidagent.core.ToolError
import com.androidagent.core.ToolResult
import com.androidagent.tools.accessibility.AgentAccessibilityService
import com.androidagent.androidapp.EnvironmentDetector
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import android.content.Context
import com.androidagent.androidapp.AppToolContext

class PressBackToolTest {
    private val tool = PressBackTool()

    @Test
    fun `validate always succeeds`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute fails when no ROOT and no accessibility service`() {
        val mockContext = mockk<Context>(relaxed = true)

        mockkConstructor(EnvironmentDetector::class)
        every { anyConstructed<EnvironmentDetector>().hasRoot() } returns false

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(AppToolContext(mockContext), emptyMap())
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.ACCESSIBILITY_SERVICE_REQUIRED, (result as ToolResult.Failure).error)
    }
}