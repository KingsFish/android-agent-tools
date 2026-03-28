package com.androidagent.tools.core

import com.androidagent.core.ToolError
import com.androidagent.core.ToolResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ToolResultTest {

    @Test
    fun `success result has correct structure`() {
        val result = ToolResult.success(mapOf("key" to "value"))
        assertTrue(result is ToolResult.Success)
        
        val json = result.toJson()
        assertTrue(json.getBoolean("success"))
        assertNotNull(json.getJSONObject("data"))
    }

    @Test
    fun `failure result has correct structure`() {
        val result = ToolResult.failure(ToolError.FILE_NOT_FOUND, "/path/to/file")
        assertTrue(result is ToolResult.Failure)
        
        val json = result.toJson()
        assertFalse(json.getBoolean("success"))
        assertNotNull(json.getJSONObject("error"))
        assertEquals("FILE_NOT_FOUND", json.getJSONObject("error").getString("code"))
    }
}
