package com.androidagent.tools.tools.ui

import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult
import com.androidagent.tools.core.EnvironmentDetector
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import android.content.Context

class SwipeToolTest {
    private val tool = SwipeTool()

    @Test
    fun `validate fails when start_x is missing`() {
        val result = tool.validate(mapOf(
            "start_y" to 100,
            "end_x" to 200,
            "end_y" to 300
        ))
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate fails when start_y is missing`() {
        val result = tool.validate(mapOf(
            "start_x" to 0,
            "end_x" to 200,
            "end_y" to 300
        ))
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate fails when end_x is missing`() {
        val result = tool.validate(mapOf(
            "start_x" to 0,
            "start_y" to 100,
            "end_y" to 300
        ))
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate fails when end_y is missing`() {
        val result = tool.validate(mapOf(
            "start_x" to 0,
            "start_y" to 100,
            "end_x" to 200
        ))
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate succeeds with all required parameters`() {
        val result = tool.validate(mapOf(
            "start_x" to 0,
            "start_y" to 100,
            "end_x" to 200,
            "end_y" to 300
        ))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `validate succeeds with optional duration`() {
        val result = tool.validate(mapOf(
            "start_x" to 0,
            "start_y" to 100,
            "end_x" to 200,
            "end_y" to 300,
            "duration" to 500
        ))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute fails when no ROOT and no accessibility service`() {
        val mockContext = mockk<Context>(relaxed = true)

        mockkConstructor(EnvironmentDetector::class)
        every { anyConstructed<EnvironmentDetector>().hasRoot() } returns false

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf(
                "start_x" to 0,
                "start_y" to 100,
                "end_x" to 200,
                "end_y" to 300
            ))
        }

        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.ACCESSIBILITY_SERVICE_REQUIRED, (result as ToolResult.Failure).error)
    }
}
