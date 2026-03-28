package com.androidagent.tools.tools.file

import com.androidagent.core.ToolResult
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File
import com.androidagent.androidapp.AppToolContext

class FileExistsToolTest {
    private val tool = FileExistsTool()
    private val mockContext = mockk<android.content.Context>(relaxed = true)

    @Test
    fun `execute returns exists true for existing file`() {
        val tempFile = File.createTempFile("exists_test", ".txt")

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(AppToolContext(mockContext), mapOf("path" to tempFile.absolutePath))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertTrue(data["exists"] as Boolean)
        assertEquals("file", data["type"])

        tempFile.delete()
    }

    @Test
    fun `execute returns exists false for nonexistent file`() {
        val result = kotlinx.coroutines.runBlocking {
            tool.execute(AppToolContext(mockContext), mapOf("path" to "/nonexistent/file.txt"))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertFalse(data["exists"] as Boolean)
    }

    @Test
    fun `execute identifies directory type`() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "exists_dir_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(AppToolContext(mockContext), mapOf("path" to tempDir.absolutePath))
        }

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data
        assertTrue(data["exists"] as Boolean)
        assertEquals("directory", data["type"])

        tempDir.delete()
    }
}