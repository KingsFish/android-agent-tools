package com.androidagent.tools.core

import com.androidagent.core.ToolError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ToolErrorTest {

    @Test
    fun `withContext appends context to message`() {
        val error = ToolError.FILE_NOT_FOUND
        val result = error.withContext("/path/to/file")
        assertEquals("File does not exist: /path/to/file", result)
    }

    @Test
    fun `all error codes have messages`() {
        for (error in ToolError.entries) {
            assertNotNull(error.message)
            assertTrue(error.message.isNotEmpty())
        }
    }
}
