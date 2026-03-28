package com.androidagent.tools.tools.file

import com.androidagent.core.ToolError
import com.androidagent.core.ToolResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File
import com.androidagent.androidapp.AppToolContext

class ReadFileToolTest {

    private val tool = ReadFileTool()
    private val mockContext = mockk<android.content.Context>(relaxed = true)

    @Test
    fun `validate fails when path is missing`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isFailure)
        assertEquals(ToolError.INVALID_PARAMETER, (result as? com.androidagent.core.ValidationResult.Failure)?.error)
    }

    @Test
    fun `validate succeeds with valid path`() {
        val result = tool.validate(mapOf("path" to "/sdcard/test.txt"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute fails when file does not exist`() {
        val result = kotlinx.coroutines.runBlocking {
            tool.execute(AppToolContext(mockContext), mapOf("path" to "/nonexistent/file.txt"))
        }
        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.FILE_NOT_FOUND, (result as ToolResult.Failure).error)
    }

    @Test
    fun `execute succeeds for existing file`() {
        // Create temp file
        val tempFile = File.createTempFile("test", ".txt")
        tempFile.writeText("Hello, World!")

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(AppToolContext(mockContext), mapOf("path" to tempFile.absolutePath))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertEquals("Hello, World!", data["content"])

        tempFile.delete()
    }
}