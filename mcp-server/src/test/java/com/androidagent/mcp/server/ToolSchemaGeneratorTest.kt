package com.androidagent.mcp.server

import com.androidagent.core.Tool
import com.androidagent.core.ToolSchema
import com.androidagent.core.SchemaProperty
import com.androidagent.core.ToolContext
import com.androidagent.core.ToolResult
import com.androidagent.core.ValidationResult
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for ToolSchemaGenerator
 */
class ToolSchemaGeneratorTest {

    // Helper to create mock tools for testing
    private fun createMockTool(
        name: String,
        description: String,
        properties: Map<String, SchemaProperty>,
        required: List<String>
    ): Tool {
        return object : Tool {
            override val name = name
            override val description = description
            override val inputSchema = ToolSchema(
                properties = properties,
                required = required
            )
            override fun validate(params: Map<String, Any?>): ValidationResult = ValidationResult.success()
            override suspend fun execute(context: ToolContext, params: Map<String, Any?>): ToolResult =
                ToolResult.success(emptyMap())
        }
    }

    @Nested
    @DisplayName("Tool Schema Generation")
    inner class SchemaGeneration {

        @Test
        @DisplayName("Should generate schema for read_file tool")
        fun testReadFileSchema() {
            val tool = createMockTool(
                "read_file",
                "Read the content of a file.",
                mapOf(
                    "path" to SchemaProperty(type = "string", description = "File path"),
                    "encoding" to SchemaProperty(type = "string", description = "Encoding", default = "utf-8")
                ),
                listOf("path")
            )
            val schema = ToolSchemaGenerator.generateToolSchema(tool)

            assertEquals("read_file", schema["name"])
            assertTrue(schema["description"].toString().contains("Read the content"))

            val inputSchema = schema["inputSchema"] as Map<*, *>
            assertEquals("object", inputSchema["type"])

            val properties = inputSchema["properties"] as Map<*, *>
            assertTrue(properties.containsKey("path"))
            assertTrue(properties.containsKey("encoding"))

            val required = inputSchema["required"] as List<*>
            assertTrue(required.contains("path"))
        }

        @Test
        @DisplayName("Should generate schema for tap tool")
        fun testTapSchema() {
            val tool = createMockTool(
                "tap",
                "Perform a tap at coordinates.",
                mapOf(
                    "x" to SchemaProperty(type = "integer", description = "X coordinate"),
                    "y" to SchemaProperty(type = "integer", description = "Y coordinate")
                ),
                listOf("x", "y")
            )
            val schema = ToolSchemaGenerator.generateToolSchema(tool)

            assertEquals("tap", schema["name"])

            val inputSchema = schema["inputSchema"] as Map<*, *>
            val properties = inputSchema["properties"] as Map<*, *>
            assertTrue(properties.containsKey("x"))
            assertTrue(properties.containsKey("y"))

            val xProp = properties["x"] as Map<*, *>
            assertEquals("integer", xProp["type"])

            val required = inputSchema["required"] as List<*>
            assertTrue(required.contains("x"))
            assertTrue(required.contains("y"))
        }

        @Test
        @DisplayName("Should generate schema for get_device_info tool with no parameters")
        fun testGetDeviceInfoSchema() {
            val tool = createMockTool(
                "get_device_info",
                "Get device information.",
                emptyMap(),
                emptyList()
            )
            val schema = ToolSchemaGenerator.generateToolSchema(tool)

            assertEquals("get_device_info", schema["name"])

            val inputSchema = schema["inputSchema"] as Map<*, *>
            val required = inputSchema["required"] as List<*>
            assertTrue(required.isEmpty())
        }

        @Test
        @DisplayName("Should generate schema for check_permissions tool with array parameter")
        fun testCheckPermissionsSchema() {
            val tool = createMockTool(
                "check_permissions",
                "Check granted permissions.",
                mapOf(
                    "permissions" to SchemaProperty(
                        type = "array",
                        description = "Permissions to check",
                        items = SchemaProperty(type = "string")
                    )
                ),
                emptyList()
            )
            val schema = ToolSchemaGenerator.generateToolSchema(tool)

            assertEquals("check_permissions", schema["name"])

            val inputSchema = schema["inputSchema"] as Map<*, *>
            val properties = inputSchema["properties"] as Map<*, *>
            assertTrue(properties.containsKey("permissions"))

            val permProp = properties["permissions"] as Map<*, *>
            assertEquals("array", permProp["type"])
        }
    }

    @Nested
    @DisplayName("All Tools Coverage")
    inner class AllToolsCoverage {

        @Test
        @DisplayName("Should generate valid schema for all Tier 1 tools")
        fun testAllTier1Tools() {
            val tier1Tools = listOf(
                "read_file", "write_file", "list_directory", "delete_file", "file_exists",
                "list_apps", "get_app_info", "launch_app",
                "get_device_info", "get_battery_status",
                "check_permissions"
            )

            tier1Tools.forEach { toolName ->
                val tool = createMockTool(toolName, "Test description", emptyMap(), emptyList())
                val schema = ToolSchemaGenerator.generateToolSchema(tool)
                assertNotNull(schema["name"])
                assertNotNull(schema["description"])
                assertNotNull(schema["inputSchema"])
            }
        }

        @Test
        @DisplayName("Should generate valid schema for all Tier 2 tools")
        fun testAllTier2Tools() {
            val tier2Tools = listOf(
                "take_screenshot", "tap", "swipe", "input_text", "get_ui_tree",
                "install_app", "uninstall_app", "force_stop_app"
            )

            tier2Tools.forEach { toolName ->
                val tool = createMockTool(toolName, "Test description", emptyMap(), emptyList())
                val schema = ToolSchemaGenerator.generateToolSchema(tool)
                assertNotNull(schema["name"])
                assertNotNull(schema["description"])
                assertNotNull(schema["inputSchema"])
            }
        }

        @Test
        @DisplayName("Should generate valid schema for all Tier 3 tools")
        fun testAllTier3Tools() {
            val tier3Tools = listOf(
                "press_back", "press_home", "press_recents",
                "press_key", "long_press", "drag",
                "wait_for_ui_stable", "wait_for_element",
                "get_current_app", "is_app_running",
                "get_clipboard", "set_clipboard",
                "click_node_by_text", "click_node_by_id"
            )

            tier3Tools.forEach { toolName ->
                val tool = createMockTool(toolName, "Test description", emptyMap(), emptyList())
                val schema = ToolSchemaGenerator.generateToolSchema(tool)
                assertNotNull(schema["name"])
                assertNotNull(schema["description"])
                assertNotNull(schema["inputSchema"])
            }
        }
    }
}