package com.androidagent.tools.tools.ui

import com.androidagent.core.ToolError
import com.androidagent.core.ToolResult
import com.androidagent.tools.core.EnvironmentDetector
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import android.content.Context

class TakeScreenshotToolTest {
    private val tool = TakeScreenshotTool()

    @Test
    fun `validate always succeeds`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `validate ignores any parameters`() {
        val result = tool.validate(mapOf("foo" to "bar"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute fails when no ROOT and no accessibility service`() {
        val mockContext = mockk<Context>(relaxed = true)

        mockkConstructor(EnvironmentDetector::class)
        every { anyConstructed<EnvironmentDetector>().hasRoot() } returns false

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, emptyMap())
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.ACCESSIBILITY_SERVICE_REQUIRED, (result as ToolResult.Failure).error)
    }
}
