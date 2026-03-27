package com.androidagent.mcp.server

import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for ToolSchemaGenerator
 */
class ToolSchemaGeneratorTest {

    @Nested
    @DisplayName("Tool Schema Generation")
    inner class SchemaGeneration {

        @Test
        @DisplayName("Should generate schema for read_file tool")
        fun testReadFileSchema() {
            val schema = ToolSchemaGenerator.generateToolSchema("read_file")

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
            val schema = ToolSchemaGenerator.generateToolSchema("tap")

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
            val schema = ToolSchemaGenerator.generateToolSchema("get_device_info")

            assertEquals("get_device_info", schema["name"])

            val inputSchema = schema["inputSchema"] as Map<*, *>
            val required = inputSchema["required"] as List<*>
            assertTrue(required.isEmpty())
        }

        @Test
        @DisplayName("Should generate schema for check_permissions tool with array parameter")
        fun testCheckPermissionsSchema() {
            val schema = ToolSchemaGenerator.generateToolSchema("check_permissions")

            assertEquals("check_permissions", schema["name"])

            val inputSchema = schema["inputSchema"] as Map<*, *>
            val properties = inputSchema["properties"] as Map<*, *>
            assertTrue(properties.containsKey("permissions"))

            val permProp = properties["permissions"] as Map<*, *>
            assertEquals("array", permProp["type"])
        }

        @Test
        @DisplayName("Should return unknown tool schema for unrecognized tool")
        fun testUnknownToolSchema() {
            val schema = ToolSchemaGenerator.generateToolSchema("nonexistent_tool")

            assertEquals("nonexistent_tool", schema["name"])
            assertEquals("Unknown tool", schema["description"])
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
                val schema = ToolSchemaGenerator.generateToolSchema(toolName)
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
                val schema = ToolSchemaGenerator.generateToolSchema(toolName)
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
                val schema = ToolSchemaGenerator.generateToolSchema(toolName)
                assertNotNull(schema["name"])
                assertNotNull(schema["description"])
                assertNotNull(schema["inputSchema"])
            }
        }
    }
}