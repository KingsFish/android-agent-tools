package com.androidagent.tools.tools.file

import com.androidagent.core.ToolError
import com.androidagent.core.ToolResult
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File
import com.androidagent.androidapp.AppToolContext

class WriteFileToolTest {

    private val tool = WriteFileTool()
    private val mockContext = mockk<android.content.Context>(relaxed = true)

    @Test
    fun `validate fails when path is missing`() {
        val result = tool.validate(mapOf("content" to "test"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate fails when content is missing`() {
        val result = tool.validate(mapOf("path" to "/test.txt"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `validate succeeds with path and content`() {
        val result = tool.validate(mapOf("path" to "/test.txt", "content" to "Hello"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `execute creates file and writes content`() {
        val tempDir = System.getProperty("java.io.tmpdir")
        val testFile = File(tempDir, "write_test_${System.currentTimeMillis()}.txt")

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(AppToolContext(mockContext), mapOf(
                "path" to testFile.absolutePath,
                "content" to "Hello, World!"
            ))
        }

        assertTrue(result is ToolResult.Success)
        assertTrue(testFile.exists())
        assertEquals("Hello, World!", testFile.readText())

        testFile.delete()
    }

    @Test
    fun `execute overwrites existing file`() {
        val tempFile = File.createTempFile("overwrite_test", ".txt")
        tempFile.writeText("Original")

        val result = kotlinx.coroutines.runBlocking {
            tool.execute(AppToolContext(mockContext), mapOf(
                "path" to tempFile.absolutePath,
                "content" to "New Content"
            ))
        }

        assertTrue(result is ToolResult.Success)
        assertEquals("New Content", tempFile.readText())

        tempFile.delete()
    }
}