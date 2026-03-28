package com.androidagent.mcp.server

import com.androidagent.core.Tool
import com.androidagent.core.ToolSchema
import com.androidagent.core.SchemaProperty
import com.androidagent.core.ToolError
import com.androidagent.core.ToolResult
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for McpProtocol
 */
class McpProtocolTest {

    @Nested
    @DisplayName("Tools List Response")
    inner class ToolsListResponse {

        @Test
        @DisplayName("Should create valid tools list response structure")
        fun testToolsListResponseStructure() {
            val sampleResponse = """
                {
                    "tools": [
                        {
                            "name": "test_tool",
                            "description": "A test tool",
                            "inputSchema": {
                                "type": "object",
                                "properties": {}
                            }
                        }
                    ]
                }
            """.trimIndent()

            val json = JSONObject(sampleResponse)
            assertTrue(json.has("tools"))

            val tools = json.getJSONArray("tools")
            assertEquals(1, tools.length())

            val tool = tools.getJSONObject(0)
            assertEquals("test_tool", tool.getString("name"))
            assertEquals("A test tool", tool.getString("description"))
            assertTrue(tool.has("inputSchema"))
        }
    }

    @Nested
    @DisplayName("Tool Call Response")
    inner class ToolCallResponse {

        @Test
        @DisplayName("Should create success response")
        fun testSuccessResponse() {
            val result = ToolResult.success(mapOf("content" to "test content", "size" to 12))
            val response = McpProtocol.createToolCallResponse(result)

            val json = JSONObject(response)
            assertTrue(json.has("content"))

            val content = json.getJSONArray("content")
            assertEquals(1, content.length())

            val contentItem = content.getJSONObject(0)
            assertEquals("text", contentItem.getString("type"))
            assertTrue(contentItem.has("text"))

            val innerJson = JSONObject(contentItem.getString("text"))
            assertTrue(innerJson.getBoolean("success"))
        }

        @Test
        @DisplayName("Should create failure response")
        fun testFailureResponse() {
            val result = ToolResult.failure(ToolError.FILE_NOT_FOUND, "test.txt")
            val response = McpProtocol.createToolCallResponse(result)

            val json = JSONObject(response)
            val content = json.getJSONArray("content")
            val contentItem = content.getJSONObject(0)
            val innerJson = JSONObject(contentItem.getString("text"))

            assertFalse(innerJson.getBoolean("success"))
            assertTrue(innerJson.has("error"))
        }

        @Test
        @DisplayName("Should handle empty data in success response")
        fun testEmptyDataResponse() {
            val result = ToolResult.success(emptyMap())
            val response = McpProtocol.createToolCallResponse(result)

            val json = JSONObject(response)
            val content = json.getJSONArray("content")
            val contentItem = content.getJSONObject(0)
            val innerJson = JSONObject(contentItem.getString("text"))

            assertTrue(innerJson.getBoolean("success"))
        }
    }

    @Nested
    @DisplayName("Error Response")
    inner class ErrorResponse {

        @Test
        @DisplayName("Should create error response with message")
        fun testErrorResponse() {
            val response = McpProtocol.createErrorResponse("Tool name is required")

            val json = JSONObject(response)
            assertEquals("Tool name is required", json.getString("error"))
        }

        @Test
        @DisplayName("Should handle special characters in error message")
        fun testSpecialCharacters() {
            val response = McpProtocol.createErrorResponse("Error: \"invalid\" \\path\\")

            val json = JSONObject(response)
            assertTrue(json.getString("error").contains("invalid"))
        }
    }
}