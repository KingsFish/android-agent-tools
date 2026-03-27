package com.androidagent.tools.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.json.JSONObject

class ToolResultTest {

    @Test
    fun `success result creates correct JSON`() {
        val result = ToolResult.Success(mapOf(
            "content" to "Hello",
            "size" to 5
        ))
        val json = result.toJson()
        assertTrue(json.getBoolean("success"))
        assertEquals("Hello", json.getJSONObject("data").getString("content"))
    }

    @Test
    fun `failure result creates correct JSON`() {
        val result = ToolResult.Failure(
            ToolError.FILE_NOT_FOUND,
            "/sdcard/test.txt"
        )
        val json = result.toJson()
        assertFalse(json.getBoolean("success"))
        assertEquals("FILE_NOT_FOUND", json.getJSONObject("error").getString("code"))
        assertTrue(json.getJSONObject("error").getString("message").contains("/sdcard/test.txt"))
    }

    @Test
    fun `success from data factory works`() {
        val result = ToolResult.success(mapOf("key" to "value"))
        assertTrue(result is ToolResult.Success)
    }

    @Test
    fun `failure from error factory works`() {
        val result = ToolResult.failure(ToolError.INVALID_PARAMETER, "path")
        assertTrue(result is ToolResult.Failure)
    }
}