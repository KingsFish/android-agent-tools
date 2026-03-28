package com.androidagent.tools.tools.file

import com.androidagent.core.ToolError
import com.androidagent.core.ToolResult
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class DeleteFileToolTest {
    private val tool = DeleteFileTool()
    private val mockContext = mockk<android.content.Context>(relaxed = true)

    @Test
    fun `execute deletes existing file`() {
        val tempFile = File.createTempFile("delete_test", ".txt")
        assertTrue(tempFile.exists())

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("path" to tempFile.absolutePath))
        }

        assertTrue(result is ToolResult.Success)
        assertFalse(tempFile.exists())
    }

    @Test
    fun `execute fails when file does not exist`() {
        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("path" to "/nonexistent/file.txt"))
        }
        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.FILE_NOT_FOUND, (result as ToolResult.Failure).error)
    }
}