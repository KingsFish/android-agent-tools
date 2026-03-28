package com.androidagent.tools.executor

import com.androidagent.core.Tool
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ToolExecutorTest {

    @Test
    fun `executor registers and retrieves tools`() {
        val executor = ToolExecutor()
        val mockTool = mockk<Tool> {
            every { name } returns "test_tool"
        }

        executor.register(mockTool)

        assertEquals(mockTool, executor.getTool("test_tool"))
        assertNull(executor.getTool("nonexistent"))
    }

    @Test
    fun `executor lists all registered tools`() {
        val executor = ToolExecutor()
        val mockTool1 = mockk<Tool> { every { name } returns "tool1" }
        val mockTool2 = mockk<Tool> { every { name } returns "tool2" }

        executor.register(mockTool1)
        executor.register(mockTool2)

        val tools = executor.listTools()
        assertEquals(2, tools.size)
        assertTrue(tools.contains("tool1"))
        assertTrue(tools.contains("tool2"))
    }

    @Test
    fun `executor overwrites tool with same name`() {
        val executor = ToolExecutor()
        val mockTool1 = mockk<Tool> { every { name } returns "test_tool" }
        val mockTool2 = mockk<Tool> { every { name } returns "test_tool" }

        executor.register(mockTool1)
        executor.register(mockTool2)

        val tools = executor.listTools()
        assertEquals(1, tools.size)
        assertEquals(mockTool2, executor.getTool("test_tool"))
    }

    @Test
    fun `executor returns empty list when no tools registered`() {
        val executor = ToolExecutor()

        val tools = executor.listTools()
        assertTrue(tools.isEmpty())
    }
}