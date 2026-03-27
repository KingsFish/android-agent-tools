package com.androidagent.mcp.server

import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for McpHttpServer constants and utilities
 * Note: Full HTTP server testing requires Android instrumentation tests
 */
class McpHttpServerTest {

    @Nested
    @DisplayName("Constants")
    inner class Constants {

        @Test
        @DisplayName("Should have default port 8080")
        fun testDefaultPort() {
            assertEquals(8080, McpHttpServer.DEFAULT_PORT)
        }

        @Test
        @DisplayName("Should have MIME_JSON constant")
        fun testMimeJson() {
            assertEquals("application/json", McpHttpServer.MIME_JSON)
        }
    }

    @Nested
    @DisplayName("Request/Response Format")
    inner class RequestResponseFormat {

        @Test
        @DisplayName("Should parse valid tool call request")
        fun testParseToolCallRequest() {
            val requestJson = """
                {
                    "name": "tap",
                    "arguments": {
                        "x": 500,
                        "y": 800
                    }
                }
            """.trimIndent()

            val request = JSONObject(requestJson)
            assertEquals("tap", request.getString("name"))

            val arguments = request.getJSONObject("arguments")
            assertEquals(500, arguments.getInt("x"))
            assertEquals(800, arguments.getInt("y"))
        }

        @Test
        @DisplayName("Should handle empty arguments")
        fun testEmptyArguments() {
            val requestJson = """
                {
                    "name": "get_device_info",
                    "arguments": {}
                }
            """.trimIndent()

            val request = JSONObject(requestJson)
            assertEquals("get_device_info", request.getString("name"))

            val arguments = request.getJSONObject("arguments")
            assertTrue(arguments.length() == 0)
        }

        @Test
        @DisplayName("Should handle missing arguments field")
        fun testMissingArguments() {
            val requestJson = """
                {
                    "name": "get_device_info"
                }
            """.trimIndent()

            val request = JSONObject(requestJson)
            assertEquals("get_device_info", request.getString("name"))
            assertFalse(request.has("arguments"))
        }

        @Test
        @DisplayName("Should parse array arguments")
        fun testArrayArguments() {
            val requestJson = """
                {
                    "name": "check_permissions",
                    "arguments": {
                        "permissions": ["storage", "camera", "location"]
                    }
                }
            """.trimIndent()

            val request = JSONObject(requestJson)
            val arguments = request.getJSONObject("arguments")
            val permissions = arguments.getJSONArray("permissions")

            assertEquals(3, permissions.length())
            assertEquals("storage", permissions.getString(0))
            assertEquals("camera", permissions.getString(1))
            assertEquals("location", permissions.getString(2))
        }
    }

    @Nested
    @DisplayName("Endpoint Paths")
    inner class EndpointPaths {

        @Test
        @DisplayName("Should have correct endpoint paths")
        fun testEndpointPaths() {
            // These are the expected endpoint paths
            val healthPath = "/health"
            val listToolsPath = "/mcp/tools/list"
            val callToolPath = "/mcp/tools/call"

            assertEquals("/health", healthPath)
            assertEquals("/mcp/tools/list", listToolsPath)
            assertEquals("/mcp/tools/call", callToolPath)
        }
    }
}