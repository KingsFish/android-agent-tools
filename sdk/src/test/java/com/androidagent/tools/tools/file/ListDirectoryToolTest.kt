package com.androidagent.tools.tools.file

import com.androidagent.tools.core.ToolError
import com.androidagent.tools.core.ToolResult
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class ListDirectoryToolTest {
    private val tool = ListDirectoryTool()
    private val mockContext = mockk<android.content.Context>(relaxed = true)

    @Test
    fun `validate fails when path is missing`() {
        val result = tool.validate(emptyMap())
        assertTrue(result.isFailure)
    }

    @Test
    fun `execute fails when directory does not exist`() {
        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("path" to "/nonexistent/dir"))
        }
        assertTrue(result is ToolResult.Failure)
        assertEquals(ToolError.DIRECTORY_NOT_FOUND, (result as ToolResult.Failure).error)
    }

    @Test
    fun `execute lists directory contents`() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "list_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        File(tempDir, "file1.txt").createNewFile()
        File(tempDir, "subdir").mkdirs()

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(mockContext, mapOf("path" to tempDir.absolutePath))
        }

        assertTrue(result is ToolResult.Success)
        val entries = (result as ToolResult.Success).data["entries"] as List<*>
        assertEquals(2, entries.size)

        tempDir.deleteRecursively()
    }
}